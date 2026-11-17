package com.portfolio.controller;

import com.portfolio.dto.*;
import com.portfolio.entity.Admin;
import com.portfolio.exception.BadRequestException;
import com.portfolio.exception.UnauthorizedException;
import com.portfolio.repository.AdminPasskeyCredentialRepository;
import com.portfolio.repository.AdminRepository;
import com.portfolio.entity.AdminPasskeyCredential;
import com.portfolio.security.JwtTokenProvider;
import com.portfolio.security.WebAuthnService;
import com.portfolio.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Biometric & Passkey Authentication", description = "WebAuthn / FIDO2 passkey registration, biometric login, and device management")
@SuppressWarnings("null")
public class AdminPasskeyController {

    private final AuthService authService;
    private final WebAuthnService webAuthnService;
    private final AdminRepository adminRepository;
    private final AdminPasskeyCredentialRepository credentialRepository;
    private final JwtTokenProvider tokenProvider;

    public AdminPasskeyController(AuthService authService,
                                  WebAuthnService webAuthnService,
                                  AdminRepository adminRepository,
                                  AdminPasskeyCredentialRepository credentialRepository,
                                  JwtTokenProvider tokenProvider) {
        this.authService = authService;
        this.webAuthnService = webAuthnService;
        this.adminRepository = adminRepository;
        this.credentialRepository = credentialRepository;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Primary or alias admin login endpoint (password first step)
     */
    @PostMapping("/login")
    @Operation(summary = "Step 1: Administrator password authentication (prompts MFA if passkey is registered)")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest) {

        String ip = getClientIp(httpRequest);
        String origin = getOrigin(httpRequest);
        AuthResponse response = authService.login(request, ip, origin);
        if (response.isMfaRequired()) {
            return ResponseEntity.ok(ApiResponse.ok("Biometric verification required", response));
        }
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    /**
     * WebAuthn Authentication: Get assertion options (challenge, RP ID, allowed credentials)
     */
    @RequestMapping(value = "/passkey/authentication/options", method = {RequestMethod.POST, RequestMethod.GET})
    @Operation(summary = "Get WebAuthn assertion challenge and credential options for biometric login")
    public ResponseEntity<ApiResponse<PasskeyAuthenticationOptionsResponse>> getAuthenticationOptions(
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        String username = resolveUsername(body, userDetails, httpRequest);
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Administrator not found: " + username));

        String origin = getOrigin(httpRequest);
        PasskeyAuthenticationOptionsResponse options = webAuthnService.getAuthenticationOptions(admin, origin);
        return ResponseEntity.ok(ApiResponse.ok(options));
    }

    /**
     * WebAuthn Authentication: Verify passkey / biometric assertion (Step 2 or Direct Passwordless)
     */
    @PostMapping("/passkey/authentication/verify")
    @Operation(summary = "Step 2: Verify WebAuthn / Biometric assertion signature and issue full JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyAuthentication(
            @Valid @RequestBody PasskeyAuthenticationVerifyRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        String username = null;
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            username = request.getUsername().trim();
        } else if (userDetails != null && userDetails.getUsername() != null && !userDetails.getUsername().isBlank()) {
            username = userDetails.getUsername();
        } else if (request.getPendingAuthToken() != null && !request.getPendingAuthToken().isBlank()) {
            try {
                username = tokenProvider.getUsernameFromJwt(request.getPendingAuthToken());
            } catch (Exception ignored) {}
        }

        if (username == null || username.isBlank()) {
            String fromToken = resolveUsernameFromToken(httpRequest);
            if (fromToken != null && !fromToken.isBlank()) {
                username = fromToken;
            } else if (request.getId() != null) {
                // Discover authenticating administrator directly from registered passkey credential
                Optional<AdminPasskeyCredential> credOpt = credentialRepository.findByCredentialIdAndRevokedAtIsNull(request.getId());
                if (credOpt.isPresent() && credOpt.get().getAdmin() != null) {
                    username = credOpt.get().getAdmin().getUsername();
                }
            }
        }

        if (username == null || username.isBlank()) {
            throw new BadRequestException("Username or pending authorization session required");
        }

        String ip = getClientIp(httpRequest);
        String origin = getOrigin(httpRequest);

        AuthResponse authResponse = authService.completePasskeyLogin(username, request, origin, ip);
        return ResponseEntity.ok(ApiResponse.ok("Biometric authentication successful", authResponse));
    }

    /**
     * WebAuthn Emergency Recovery Code verification
     */
    @PostMapping("/passkey/recovery-verify")
    @Operation(summary = "Emergency access fallback using single-use hashed recovery code")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyRecoveryCode(
            @Valid @RequestBody PasskeyRecoveryVerifyRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        String username = userDetails != null ? userDetails.getUsername() : request.getUsername();
        if (username == null || username.isBlank()) {
            if (request.getPendingAuthToken() != null && !request.getPendingAuthToken().isBlank()) {
                try {
                    username = tokenProvider.getUsernameFromJwt(request.getPendingAuthToken());
                } catch (Exception ignored) {}
            }
        }
        if (username == null || username.isBlank()) {
            username = resolveUsernameFromToken(httpRequest);
        }
        if (username == null || username.isBlank()) {
            throw new BadRequestException("Username must be provided for recovery code verification");
        }

        String ip = getClientIp(httpRequest);
        AuthResponse authResponse = authService.completeRecoveryCodeLogin(username, request.getRecoveryCode(), ip);
        return ResponseEntity.ok(ApiResponse.ok("Recovery authentication successful", authResponse));
    }

    /**
     * WebAuthn Registration: Request registration challenge and options
     */
    @RequestMapping(value = "/passkey/registration/options", method = {RequestMethod.POST, RequestMethod.GET})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Request WebAuthn registration options to enroll new biometric passkey/device")
    public ResponseEntity<ApiResponse<PasskeyRegistrationOptionsResponse>> getRegistrationOptions(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        Admin admin = adminRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));

        String origin = getOrigin(httpRequest);
        PasskeyRegistrationOptionsResponse options = webAuthnService.getRegistrationOptions(admin, origin);
        return ResponseEntity.ok(ApiResponse.ok(options));
    }

    /**
     * WebAuthn Registration: Verify and enroll new passkey credential & device
     */
    @PostMapping("/passkey/registration/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify WebAuthn attestation, register passkey credential, and generate recovery codes")
    public ResponseEntity<ApiResponse<RecoveryCodesResponse>> verifyRegistration(
            @Valid @RequestBody PasskeyRegistrationVerifyRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        Admin admin = adminRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));

        String ip = getClientIp(httpRequest);
        String origin = getOrigin(httpRequest);

        RecoveryCodesResponse response = webAuthnService.verifyRegistration(admin, request, origin, ip);
        return ResponseEntity.ok(ApiResponse.ok("Passkey registered successfully", response));
    }

    /**
     * List all enrolled passkey devices for authenticated administrator
     */
    @GetMapping("/passkeys")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all enrolled passkeys and trusted devices")
    public ResponseEntity<ApiResponse<List<AdminDeviceDto>>> getPasskeys(
            @AuthenticationPrincipal UserDetails userDetails) {

        Admin admin = adminRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));

        List<AdminDeviceDto> devices = webAuthnService.getAdminDevices(admin);
        return ResponseEntity.ok(ApiResponse.ok(devices));
    }

    /**
     * Revoke an enrolled passkey credential
     */
    @DeleteMapping("/passkeys/{credentialId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Revoke an enrolled passkey credential")
    public ResponseEntity<ApiResponse<Void>> revokePasskey(
            @PathVariable String credentialId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        Admin admin = adminRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));

        String ip = getClientIp(httpRequest);
        webAuthnService.revokePasskey(admin, credentialId, ip);
        return ResponseEntity.ok(ApiResponse.ok("Passkey revoked successfully", null));
    }

    /**
     * Regenerate new emergency recovery codes
     */
    @PostMapping("/passkeys/recovery-codes/regenerate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Regenerate single-use emergency recovery codes")
    public ResponseEntity<ApiResponse<RecoveryCodesResponse>> regenerateRecoveryCodes(
            @AuthenticationPrincipal UserDetails userDetails) {

        Admin admin = adminRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));

        List<String> codes = webAuthnService.generateRecoveryCodes(admin);
        return ResponseEntity.ok(ApiResponse.ok("Recovery codes regenerated successfully", new RecoveryCodesResponse(codes)));
    }

    /* ==========================================================================
       HELPERS
       ========================================================================== */

    private String resolveUsername(Map<String, String> body, UserDetails userDetails, HttpServletRequest httpRequest) {
        if (userDetails != null && userDetails.getUsername() != null && !userDetails.getUsername().isBlank()) {
            return userDetails.getUsername();
        }
        if (body != null && body.containsKey("username") && !body.get("username").isBlank()) {
            return body.get("username").trim();
        }
        String fromToken = resolveUsernameFromToken(httpRequest);
        if (fromToken != null && !fromToken.isBlank()) {
            return fromToken;
        }
        throw new BadRequestException("Username or pending authorization session required");
    }

    private String resolveUsernameFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                return tokenProvider.getUsernameFromJwt(token);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getOrigin(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin == null || origin.isBlank()) {
            origin = request.getHeader("Referer");
        }
        return origin;
    }
}
