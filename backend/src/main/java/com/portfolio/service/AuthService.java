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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);

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

        String normalizedInput = email.trim().toLowerCase(Locale.ROOT);
        Admin admin = adminRepository.findByEmailIgnoreCase(normalizedInput)
                .or(() -> adminRepository.findByUsernameIgnoreCase(normalizedInput))
                .orElse(null);
        if (admin == null || !admin.isActive()) {
            auditLogService.logAction("anonymous", "PASSWORD_RESET_REQUESTED", "Admins", null,
                    "Password recovery requested for non-matching input: " + normalizedInput, ipAddress);
            return;
        }

        // Generate a 6-digit numeric reset code
        int codeNum = 100000 + secureRandom.nextInt(900000);
        String resetCode = String.valueOf(codeNum);

        admin.setPasswordResetToken(hashResetToken(resetCode));
        admin.setPasswordResetExpiry(LocalDateTime.now().plusMinutes(15));
        adminRepository.save(admin);

        String resetUrl = UriComponentsBuilder.fromUriString(java.util.Objects.requireNonNull(
                        properties.getSecurity().getPasswordResetUrl(), "Password reset URL must be configured"))
                .fragment("resetToken=" + resetCode)
                .build()
                .encode()
                .toUriString();

        if (!emailNotificationService.sendPasswordResetEmail(admin, resetCode, resetUrl)) {
            log.warn("[PASSWORD RESET] Failed to send email with code to {}", admin.getEmail());
        }

        auditLogService.logAction("anonymous", "PASSWORD_RESET_REQUESTED", "Admins",
                String.valueOf(admin.getId()), "Password recovery code generated for " + admin.getUsername(), ipAddress);
    }

    @Transactional
    public void resetPassword(CompletePasswordResetRequest request, String ipAddress) {
        String cleanToken = request.getToken() != null ? request.getToken().trim().replace(" ", "").replace("-", "") : "";
        Admin admin = adminRepository.findByPasswordResetToken(hashResetToken(cleanToken))
                .orElseThrow(() -> new BadRequestException("Reset code is invalid or expired. Please request a new code."));

        if (admin.getPasswordResetExpiry() == null
                || !admin.getPasswordResetExpiry().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Reset code is invalid or expired. Please request a new code.");
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

            // Enforce MFA if admin has registered WebAuthn / FIDO2 passkeys
            var passkeys = credentialRepository.findByAdminAndRevokedAtIsNull(admin);
            if (!passkeys.isEmpty()) {
                String pendingToken = tokenProvider.generatePendingMfaToken(admin);
                PasskeyAuthenticationOptionsResponse options = webAuthnService.getAuthenticationOptions(admin, origin);
                auditLogService.logAction(username, "LOGIN_PASSWORD_VERIFIED", "Admins", String.valueOf(admin.getId()), "Password verified; prompting for second-factor WebAuthn passkey", ipAddress);
                return AuthResponse.mfaRequired(pendingToken, admin.getId(), admin.getUsername(), admin.getEmail(), options);
            }

            // Direct password login when no passkey has been enrolled yet (first-time setup state)
            admin.setLastLoginAt(LocalDateTime.now());
            admin.setFailedLoginAttempts(0);
            admin.setLockoutUntil(null);
            adminRepository.save(admin);

            String jwt = tokenProvider.generateToken(authentication);

            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(Collectors.toSet());

            auditLogService.logAction(username, "LOGIN_SUCCESS", "Admins", String.valueOf(admin.getId()), "Successful administrator authentication via password", ipAddress);

            // Dispatch instant admin login security email & phone notification
            emailNotificationService.sendAdminLoginNotification(admin, "Password Authentication", ipAddress, null);

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
                .or(() -> adminRepository.findByEmailIgnoreCase(username))
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

        // Dispatch instant admin login security email & phone notification
        emailNotificationService.sendAdminLoginNotification(admin, "Biometric / WebAuthn Passkey (Fingerprint)", ipAddress, null);

        return new AuthResponse(
                jwt,
                admin.getAdminId(),
                admin.getUsername(),
                admin.getEmail(),
                admin.getFullName(),
                roles
        );
    }

    // In-memory store for emergency 6-digit access codes (expires in 10 minutes)
    private final Map<String, EmergencyOtpEntry> emergencyOtpStore = new ConcurrentHashMap<>();

    public record EmergencyOtpEntry(String code, LocalDateTime expiresAt, String username) {}

    public void requestEmergencyEmailOtp(String username, String ipAddress) {
        if (username == null || username.isBlank()) {
            throw new BadRequestException("Username or email is required to request an emergency access passcode");
        }
        Admin admin = adminRepository.findByUsername(username.trim())
                .or(() -> adminRepository.findByEmailIgnoreCase(username.trim()))
                .orElseThrow(() -> new UnauthorizedException("Administrator account not found"));

        int randomPin = 100000 + secureRandom.nextInt(900000);
        String otpCode = String.valueOf(randomPin);

        emergencyOtpStore.put(admin.getUsername().toLowerCase(),
                new EmergencyOtpEntry(otpCode, LocalDateTime.now().plusMinutes(10), admin.getUsername()));

        emailNotificationService.sendEmergencyEmailOtp(admin, otpCode, ipAddress);
        auditLogService.logAction(admin.getUsername(), "EMERGENCY_OTP_REQUESTED", "Admins",
                String.valueOf(admin.getId()), "Emergency one-time access passcode dispatched to email", ipAddress);
    }

    @Transactional
    public AuthResponse completeEmergencyEmailOtpLogin(String username, String otpCode, String ipAddress, String userAgent) {
        if (username == null || username.isBlank() || otpCode == null || otpCode.isBlank()) {
            throw new BadRequestException("Username and emergency access passcode are required");
        }
        Admin admin = adminRepository.findByUsername(username.trim())
                .or(() -> adminRepository.findByEmailIgnoreCase(username.trim()))
                .orElseThrow(() -> new UnauthorizedException("Administrator account not found"));

        EmergencyOtpEntry entry = emergencyOtpStore.get(admin.getUsername().toLowerCase());
        if (entry == null || entry.expiresAt().isBefore(LocalDateTime.now()) || !entry.code().equals(otpCode.trim())) {
            auditLogService.logAction(admin.getUsername(), "EMERGENCY_OTP_FAILED", "Admins",
                    String.valueOf(admin.getId()), "Invalid or expired emergency OTP attempt", ipAddress);
            throw new UnauthorizedException("Invalid or expired emergency access passcode. Please request a new one.");
        }

        // Single-use: consume immediately
        emergencyOtpStore.remove(admin.getUsername().toLowerCase());

        admin.setLastLoginAt(LocalDateTime.now());
        admin.setFailedLoginAttempts(0);
        admin.setLockoutUntil(null);
        adminRepository.save(admin);

        String jwt = tokenProvider.generateTokenForAdmin(admin);
        Set<String> roles = admin.getRoles().stream()
                .map(r -> "ROLE_" + r.getRoleName())
                .collect(Collectors.toSet());

        auditLogService.logAction(admin.getUsername(), "LOGIN_SUCCESS_EMERGENCY_OTP", "Admins",
                String.valueOf(admin.getId()), "Successful emergency email OTP login", ipAddress);

        emailNotificationService.sendAdminLoginNotification(admin, "Emergency Email One-Time Passcode", ipAddress, userAgent);

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

        // Dispatch instant admin login security email & phone notification
        emailNotificationService.sendAdminLoginNotification(admin, "Emergency Single-Use Recovery Code", ipAddress, null);

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
