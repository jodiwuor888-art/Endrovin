package com.portfolio.controller;

import com.portfolio.dto.*;
import com.portfolio.security.IpAddressExtractor;
import com.portfolio.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication & Provisioning", description = "Secure administrator authentication and one-time setup")
public class AuthController {

    private final AuthService authService;
    private final IpAddressExtractor ipAddressExtractor;

    public AuthController(AuthService authService, IpAddressExtractor ipAddressExtractor) {
        this.authService = authService;
        this.ipAddressExtractor = ipAddressExtractor;
    }

    @GetMapping("/status")
    @Operation(summary = "Check if first administrator setup is needed")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getSetupStatus() {
        boolean setupNeeded = authService.isFirstAdminSetupNeeded();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("setupNeeded", setupNeeded)));
    }

    @PostMapping("/setup-first-admin")
    @Operation(summary = "Secure one-time first administrator provisioning (permanently locks after 1st admin)")
    public ResponseEntity<ApiResponse<AuthResponse>> setupFirstAdmin(
            @Valid @RequestBody FirstAdminRequest request,
            HttpServletRequest httpRequest) {

        String ip = ipAddressExtractor.extract(httpRequest);
        AuthResponse response = authService.setupFirstAdmin(request, ip);
        return ResponseEntity.ok(ApiResponse.created("Administrator account created successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Administrator login with rate-limiting, audit logging, and WebAuthn MFA challenge")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest) {

        String ip = ipAddressExtractor.extract(httpRequest);
        String origin = httpRequest.getHeader("Origin");
        AuthResponse response = authService.login(request, ip, origin);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/request-password-reset")
    @Operation(summary = "Email a username and one-time password reset link")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest) {

        authService.requestPasswordReset(request.getEmail(), ipAddressExtractor.extract(httpRequest));
        return ResponseEntity.ok(ApiResponse.ok(
                "If an administrator account matches that email, recovery instructions will be sent.", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset an administrator password with a one-time email token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody CompletePasswordResetRequest request,
            HttpServletRequest httpRequest) {

        authService.resetPassword(request, ipAddressExtractor.extract(httpRequest));
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully. You can now sign in.", null));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for logged-in administrator")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest request,
            HttpServletRequest httpRequest) {

        String ip = ipAddressExtractor.extract(httpRequest);
        authService.changePassword(userDetails.getUsername(), request, ip);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }
}
