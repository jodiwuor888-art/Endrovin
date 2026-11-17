package com.portfolio.service;

import com.portfolio.config.PortfolioProperties;
import com.portfolio.dto.*;
import com.portfolio.entity.Admin;
import com.portfolio.entity.Role;
import com.portfolio.exception.BadRequestException;
import com.portfolio.exception.UnauthorizedException;
import com.portfolio.repository.AdminRepository;
import com.portfolio.repository.RoleRepository;
import com.portfolio.security.AdminUserDetails;
import com.portfolio.security.JwtTokenProvider;
import com.portfolio.security.LoginRateLimiterService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AdminRepository adminRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final LoginRateLimiterService rateLimiterService;
    private final AuditLogService auditLogService;
    private final EmailNotificationService emailNotificationService;
    private final PortfolioProperties properties;
    private final com.portfolio.repository.AdminPasskeyCredentialRepository credentialRepository;
    private final com.portfolio.security.WebAuthnService webAuthnService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AdminRepository adminRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       LoginRateLimiterService rateLimiterService,
                       AuditLogService auditLogService,
                       EmailNotificationService emailNotificationService,
                       PortfolioProperties properties,
                       com.portfolio.repository.AdminPasskeyCredentialRepository credentialRepository,
                       com.portfolio.security.WebAuthnService webAuthnService) {
        this.adminRepository = adminRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.rateLimiterService = rateLimiterService;
        this.auditLogService = auditLogService;
        this.emailNotificationService = emailNotificationService;
        this.properties = properties;
        this.credentialRepository = credentialRepository;
        this.webAuthnService = webAuthnService;
    }

    public boolean isFirstAdminSetupNeeded() {
        return adminRepository.count() == 0;
    }

    @Transactional
    public void requestPasswordReset(String email, String ipAddress) {
        String rateLimitKey = "password-reset:" + (ipAddress == null ? "unknown" : ipAddress);
        if (rateLimiterService.isBlocked(rateLimitKey)) {
            auditLogService.logAction("anonymous", "PASSWORD_RESET_THROTTLED", "Admins", null,
                    "Password recovery request throttled", ipAddress);
            return;
        }
        rateLimiterService.recordFailedAttempt(rateLimitKey);

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        Admin admin = adminRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (admin == null || !admin.isActive()) {
            auditLogService.logAction("anonymous", "PASSWORD_RESET_REQUESTED", "Admins", null,
                    "Password recovery requested", ipAddress);
            return;
        }

        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        admin.setPasswordResetToken(hashResetToken(rawToken));
        admin.setPasswordResetExpiry(LocalDateTime.now().plusMinutes(15));
        adminRepository.save(admin);

        String resetUrl = UriComponentsBuilder.fromUriString(java.util.Objects.requireNonNull(
                        properties.getSecurity().getPasswordResetUrl(), "Password reset URL must be configured"))
                .fragment("resetToken=" + rawToken)
                .build()
                .encode()
                .toUriString();

        if (!emailNotificationService.sendPasswordResetEmail(admin, resetUrl)) {
            admin.setPasswordResetToken(null);
            admin.setPasswordResetExpiry(null);
            adminRepository.save(admin);
        }

        auditLogService.logAction("anonymous", "PASSWORD_RESET_REQUESTED", "Admins",
                String.valueOf(admin.getId()), "Password recovery requested", ipAddress);
    }

    @Transactional
    public void resetPassword(CompletePasswordResetRequest request, String ipAddress) {
        Admin admin = adminRepository.findByPasswordResetToken(hashResetToken(request.getToken()))
                .orElseThrow(() -> new BadRequestException("Reset link is invalid or expired."));

        if (admin.getPasswordResetExpiry() == null
                || !admin.getPasswordResetExpiry().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Reset link is invalid or expired.");
        }

        admin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        admin.setPasswordResetToken(null);
        admin.setPasswordResetExpiry(null);
        adminRepository.save(admin);
        rateLimiterService.resetAttempts(admin.getUsername());

        auditLogService.logAction(admin.getUsername(), "PASSWORD_RESET_COMPLETED", "Admins",
                String.valueOf(admin.getId()), "Password reset completed", ipAddress);
    }

    private String hashResetToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    @Transactional
    public AuthResponse setupFirstAdmin(FirstAdminRequest request, String ipAddress) {
        if (adminRepository.count() > 0) {
            throw new BadRequestException("Administrator provisioning is already completed. Setup is permanently locked.");
        }

        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "System Super Administrator with full privileges")));

        Admin admin = new Admin(
                request.getUsername().trim(),
                request.getEmail().trim(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim()
        );
        admin.setRoles(Collections.singleton(adminRole));
        adminRepository.save(admin);

        auditLogService.logAction(
                admin.getUsername(),
                "PROVISION_FIRST_ADMIN",
                "Admins",
                String.valueOf(admin.getId()),
                "First administrator account provisioned securely",
                ipAddress
        );

        // Authenticate and issue token directly
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String jwt = tokenProvider.generateToken(authentication);
        return new AuthResponse(
                jwt,
                admin.getId(),
                admin.getUsername(),
                admin.getEmail(),
                admin.getFullName(),
                Set.of("ROLE_ADMIN")
        );
    }

    @Transactional
    public AuthResponse login(AuthRequest request, String ipAddress) {
        return login(request, ipAddress, null);
    }

    @Transactional
    public AuthResponse login(AuthRequest request, String ipAddress, String origin) {
        String username = request.getUsername().trim();

        // 1. Check rate limiter
        if (rateLimiterService.isBlocked(username)) {
            int remaining = rateLimiterService.getRemainingLockoutMinutes(username);
            auditLogService.logAction(username, "LOGIN_BLOCKED", "Admins", null, "Account temporarily locked due to multiple failed attempts", ipAddress);
            throw new UnauthorizedException("Account is temporarily locked due to multiple failed login attempts. Please try again in " + remaining + " minute(s).");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );

            // Reset rate limiter upon success
            rateLimiterService.resetAttempts(username);

            AdminUserDetails userDetails = (AdminUserDetails) authentication.getPrincipal();
            Admin admin = userDetails.getAdmin();

            // Check if admin has enrolled biometric/passkey credentials
            long enrolledPasskeys = credentialRepository.countByAdminAndRevokedAtIsNull(admin);
            if (enrolledPasskeys > 0) {
                // Issue temporary pending-MFA token and fetch passkey authentication options
                String pendingAuthToken = tokenProvider.generatePendingMfaToken(admin);
                PasskeyAuthenticationOptionsResponse passkeyOptions = webAuthnService.getAuthenticationOptions(admin, origin);

                auditLogService.logAction(username, "MFA_CHALLENGE_ISSUED", "Admins", String.valueOf(admin.getId()), "Biometric MFA challenge presented", ipAddress);

                return AuthResponse.mfaRequired(
                        pendingAuthToken,
                        admin.getAdminId(),
                        admin.getUsername(),
                        admin.getEmail(),
                        passkeyOptions
                );
            }

            // Direct login for admins without enrolled passkeys (or initial setup)
            admin.setLastLoginAt(LocalDateTime.now());
            admin.setFailedLoginAttempts(0);
            admin.setLockoutUntil(null);
            adminRepository.save(admin);

            String jwt = tokenProvider.generateToken(authentication);

            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(Collectors.toSet());

            auditLogService.logAction(username, "LOGIN_SUCCESS", "Admins", String.valueOf(admin.getId()), "Successful administrator authentication", ipAddress);

            return new AuthResponse(
                    jwt,
                    admin.getId(),
                    admin.getUsername(),
                    admin.getEmail(),
                    admin.getFullName(),
                    roles
            );

        } catch (BadCredentialsException ex) {
            rateLimiterService.recordFailedAttempt(username);
            auditLogService.logAction(username, "LOGIN_FAILED", "Admins", null, "Failed login attempt: invalid credentials", ipAddress);
            throw new UnauthorizedException("Invalid username or password.");
        }
    }

    @Transactional
    public AuthResponse completePasskeyLogin(String username, PasskeyAuthenticationVerifyRequest request, String origin, String ipAddress) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Administrator not found for verification"));

        // Cryptographically verify the WebAuthn / Passkey signature & challenge
        webAuthnService.verifyAuthentication(admin, request, origin, ipAddress);

        // Update login metadata
        admin.setLastLoginAt(LocalDateTime.now());
        admin.setFailedLoginAttempts(0);
        admin.setLockoutUntil(null);
        adminRepository.save(admin);

        String jwt = tokenProvider.generateTokenForAdmin(admin);
        Set<String> roles = admin.getRoles().stream()
                .map(r -> "ROLE_" + r.getRoleName())
                .collect(Collectors.toSet());

        auditLogService.logAction(username, "LOGIN_SUCCESS_MFA", "Admins", String.valueOf(admin.getId()), "Successful biometric WebAuthn authentication", ipAddress);

        return new AuthResponse(
                jwt,
                admin.getAdminId(),
                admin.getUsername(),
                admin.getEmail(),
                admin.getFullName(),
                roles
        );
    }

    @Transactional
    public AuthResponse completeRecoveryCodeLogin(String username, String recoveryCode, String ipAddress) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Administrator not found for recovery"));

        // Verify single-use hashed recovery code
        webAuthnService.verifyRecoveryCode(admin, recoveryCode, ipAddress);

        admin.setLastLoginAt(LocalDateTime.now());
        admin.setFailedLoginAttempts(0);
        admin.setLockoutUntil(null);
        adminRepository.save(admin);

        String jwt = tokenProvider.generateTokenForAdmin(admin);
        Set<String> roles = admin.getRoles().stream()
                .map(r -> "ROLE_" + r.getRoleName())
                .collect(Collectors.toSet());

        auditLogService.logAction(username, "LOGIN_SUCCESS_RECOVERY", "Admins", String.valueOf(admin.getId()), "Successful emergency recovery authentication", ipAddress);

        return new AuthResponse(
                jwt,
                admin.getAdminId(),
                admin.getUsername(),
                admin.getEmail(),
                admin.getFullName(),
                roles
        );
    }

    @Transactional
    public void changePassword(String username, PasswordChangeRequest request, String ipAddress) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Admin not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), admin.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect.");
        }

        admin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        admin.setPasswordResetToken(null);
        admin.setPasswordResetExpiry(null);
        adminRepository.save(admin);

        auditLogService.logAction(username, "PASSWORD_CHANGED", "Admins", String.valueOf(admin.getId()), "Password changed successfully", ipAddress);
    }
}
