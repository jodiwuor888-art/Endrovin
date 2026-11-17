
<?php
require_once "db.php";

if (!isset($conn) || $conn === false) {
    exit("Database connection is not available.");
}

$username = "admin";
$password = "Admin@123"; // Replace with a secure password

$hash = password_hash($password, PASSWORD_DEFAULT);

$sql = "INSERT INTO Admins (Username, PasswordHash)
        VALUES (?, ?)";

if (!function_exists('sqlsrv_query')) {
    exit("The PHP SQLSRV extension is not installed or enabled.");
}

$sqlsrvQuery = 'sqlsrv_query';
$stmt = $sqlsrvQuery($conn, $sql, [$username, $hash]);

if ($stmt === false) {
    if (function_exists('sqlsrv_errors')) {
        $sqlsrvErrors = 'sqlsrv_errors';
        error_log(print_r($sqlsrvErrors(), true));
    }
    exit("Could not create admin. Check the server log.");
}

echo "Admin account created.";
?>
