[CmdletBinding()]
param(
    [uri]$HealthUri = 'http://localhost:8080/actuator/health',
    [ValidateSet('mysql', 'prod')]
    [string]$DatabaseProfile = 'mysql',
    [ValidateRange(1, 60)]
    [int]$CheckIntervalSeconds = 10,
    [ValidateRange(10, 600)]
    [int]$StartupGraceSeconds = 120,
    [ValidateRange(1, 10)]
    [int]$FailedChecksBeforeRestart = 3,
    [switch]$Restart
)

$ErrorActionPreference = 'Stop'
$backendDirectory = Join-Path $PSScriptRoot 'backend'
$updatedJarPath = Join-Path $backendDirectory 'target\portfolio-management-system-recovery.jar'
$standardJarPath = Join-Path $backendDirectory 'target\portfolio-management-system-1.0.0.jar'
$jarPath = if (Test-Path -LiteralPath $updatedJarPath -PathType Leaf) {
    $updatedJarPath
} else {
    $standardJarPath
}
$logDirectory = Join-Path $backendDirectory 'logs'

if (-not (Test-Path -LiteralPath $jarPath -PathType Leaf)) {
    throw "Backend JAR not found at '$jarPath'. Build the backend before running this script."
}

$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $java) {
    throw 'Java was not found on PATH. Install the project JDK and reopen PowerShell.'
}

New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null

function Test-BackendHealth {
    try {
        $response = Invoke-WebRequest -Uri $HealthUri -TimeoutSec 5 -UseBasicParsing
        if ($response.StatusCode -eq 200) {
            $health = $response.Content | ConvertFrom-Json
            if ($health.status -and $health.status -ne 'UP') {
                $script:lastHealthError = "Health status is '$($health.status)'"
                return $false
            }
            $script:lastHealthError = ''
            return $true
        }
        $script:lastHealthError = "HTTP $($response.StatusCode)"
        return $false
    } catch {
        # Fallback to public status endpoint if actuator health returns 401/403 or is restricted
        try {
            $fallbackUri = "http://$($HealthUri.Host):$($HealthUri.Port)/api/auth/status"
            $fallbackResp = Invoke-WebRequest -Uri $fallbackUri -TimeoutSec 3 -UseBasicParsing
            if ($fallbackResp.StatusCode -eq 200) {
                $script:lastHealthError = ''
                return $true
            }
        } catch {}
        $script:lastHealthError = $_.Exception.Message
        return $false
    }
}

function Get-BackendListenerProcess {
    $listener = Get-NetTCPConnection -LocalPort $HealthUri.Port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if (-not $listener) {
        return $null
    }

    return Get-Process -Id $listener.OwningProcess -ErrorAction SilentlyContinue
}

function Start-BackendProcess {
    $timestamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
    $stdoutLog = Join-Path $logDirectory "backend-$timestamp.out.log"
    $stderrLog = Join-Path $logDirectory "backend-$timestamp.err.log"
    $arguments = @('-jar', ('"{0}"' -f $jarPath), "--spring.profiles.active=$DatabaseProfile")

    Write-Host "Starting backend from '$jarPath' with the '$DatabaseProfile' database profile..."
    Write-Host "Logs: $logDirectory"
    return Start-Process -FilePath $java.Source `
        -ArgumentList $arguments `
        -WorkingDirectory $backendDirectory `
        -RedirectStandardOutput $stdoutLog `
        -RedirectStandardError $stderrLog `
        -PassThru
}

$serverProcess = $null
if (Test-BackendHealth) {
    $serverProcess = Get-BackendListenerProcess
    if (-not $serverProcess -or $serverProcess.ProcessName -notin @('java', 'javaw')) {
        throw "The health endpoint is already UP, but its Java process could not be identified. No second backend was started."
    }

    if ($Restart) {
        $processDetails = Get-CimInstance Win32_Process -Filter "ProcessId=$($serverProcess.Id)" -ErrorAction SilentlyContinue
        if (-not $processDetails -or $processDetails.CommandLine -notmatch 'portfolio-management-system(-recovery|-[\d.]+)?\.jar') {
            throw "The healthy service on port $($HealthUri.Port) is not a recognized portfolio JAR. Stop it manually before restarting this backend."
        }

        Write-Host "Restarting the portfolio backend (PID $($serverProcess.Id)) with the current JAR..."
        Stop-Process -Id $serverProcess.Id -Force
        $stopDeadline = (Get-Date).AddSeconds(30)
        do {
            Start-Sleep -Seconds 1
            $remainingListener = Get-BackendListenerProcess
        } while ($remainingListener -and (Get-Date) -lt $stopDeadline)

        if ($remainingListener) {
            throw "Port $($HealthUri.Port) is still occupied by PID $($remainingListener.Id); backend restart aborted."
        }

        # Synchronize recovery JAR with latest entity & security classes
        $updateJarScript = Join-Path $PSScriptRoot 'update_jar.ps1'
        if (Test-Path -LiteralPath $updateJarScript) {
            try {
                Write-Host "Synchronizing compiled classes into backend JAR..."
                & powershell -ExecutionPolicy Bypass -File $updateJarScript
            } catch {
                Write-Warning "JAR sync notice: $_"
            }
        }

        $serverProcess = Start-BackendProcess
    } else {
        Write-Host "Backend is already healthy (PID $($serverProcess.Id)); monitoring it. Use -Restart to load the current JAR."
    }
} else {
    $existingListener = Get-BackendListenerProcess
    if ($existingListener) {
        throw "Port $($HealthUri.Port) is already used by process '$($existingListener.ProcessName)' (PID $($existingListener.Id)), but the backend health endpoint is not UP."
    }

    $serverProcess = Start-BackendProcess
}

$consecutiveFailures = 0
$lastHealthState = $false
$startupDeadline = (Get-Date).AddSeconds($StartupGraceSeconds)

Write-Host "Monitoring $HealthUri every $CheckIntervalSeconds seconds. Press Ctrl+C to stop monitoring."

while ($true) {
    Start-Sleep -Seconds $CheckIntervalSeconds
    $serverProcess.Refresh()

    if ($serverProcess.HasExited) {
        Write-Warning "Backend process exited with code $($serverProcess.ExitCode); restarting."
        Start-Sleep -Seconds 5
        $serverProcess = Start-BackendProcess
        $consecutiveFailures = 0
        $lastHealthState = $false
        $startupDeadline = (Get-Date).AddSeconds($StartupGraceSeconds)
        continue
    }

    if (Test-BackendHealth) {
        $consecutiveFailures = 0
        if (-not $lastHealthState) {
            Write-Host "Backend is healthy (PID $($serverProcess.Id))."
        }
        $lastHealthState = $true
        continue
    }

    $lastHealthState = $false
    $consecutiveFailures++
    Write-Warning "Backend health check failed ($consecutiveFailures/$FailedChecksBeforeRestart): $script:lastHealthError"

    if ((Get-Date) -ge $startupDeadline -and $consecutiveFailures -ge $FailedChecksBeforeRestart) {
        Write-Warning "Backend did not recover; stopping PID $($serverProcess.Id) and restarting it."
        Stop-Process -Id $serverProcess.Id -Force
        Start-Sleep -Seconds 5
        $serverProcess = Start-BackendProcess
        $consecutiveFailures = 0
        $startupDeadline = (Get-Date).AddSeconds($StartupGraceSeconds)
    }
}
