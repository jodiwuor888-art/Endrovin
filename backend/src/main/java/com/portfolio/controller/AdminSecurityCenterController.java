package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.SecurityCenterSummaryDto;
import com.portfolio.entity.AdminAuthenticationLog;
import com.portfolio.security.IpAddressExtractor;
import com.portfolio.service.SecurityCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/security-center")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Security Control Centre", description = "MFA status, registered devices, login history, and emergency session revocation")
public class AdminSecurityCenterController {

    private final SecurityCenterService securityCenterService;
    private final IpAddressExtractor ipExtractor;

    public AdminSecurityCenterController(SecurityCenterService securityCenterService,
                                         IpAddressExtractor ipExtractor) {
        this.securityCenterService = securityCenterService;
        this.ipExtractor = ipExtractor;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get comprehensive security score, active devices, and MFA status")
    public ResponseEntity<ApiResponse<SecurityCenterSummaryDto>> getSecurityOverview() {
        return ResponseEntity.ok(ApiResponse.ok(securityCenterService.getSecurityOverview()));
    }

    /** Alias for dashboard JS compatibility */
    @GetMapping("/summary")
    @Operation(summary = "Alias for /overview — returns security posture summary")
    public ResponseEntity<ApiResponse<SecurityCenterSummaryDto>> getSecuritySummary() {
        return ResponseEntity.ok(ApiResponse.ok(securityCenterService.getSecurityOverview()));
    }

    @GetMapping("/login-history")
    @Operation(summary = "Get recent authentication attempts (successful, failed, WebAuthn)")
    public ResponseEntity<ApiResponse<List<AdminAuthenticationLog>>> getLoginHistory() {
        return ResponseEntity.ok(ApiResponse.ok(securityCenterService.getAuthenticationLogs()));
    }

    @DeleteMapping("/devices/{id}")
    @Operation(summary = "Revoke access for a specific registered device")
    public ResponseEntity<ApiResponse<Void>> revokeDevice(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        String ip = ipExtractor.extract(request);
        securityCenterService.revokeDevice(id, username, ip);
        return ResponseEntity.ok(ApiResponse.ok("Device revoked successfully", null));
    }

    @PostMapping("/revoke-all-sessions")
    @Operation(summary = "Emergency session revocation: invalidates all previously issued JWT tokens immediately")
    public ResponseEntity<ApiResponse<Void>> revokeAllSessions(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        String ip = ipExtractor.extract(request);
        securityCenterService.revokeAllAdminSessions(username, ip);
        return ResponseEntity.ok(ApiResponse.ok("All administrative sessions successfully revoked", null));
    }

    /** Alias used by admin dashboard JS — same action as revoke-all-sessions */
    @PostMapping("/emergency-revoke-all")
    @Operation(summary = "Emergency alias: same as revoke-all-sessions")
    public ResponseEntity<ApiResponse<Void>> emergencyRevokeAll(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        return revokeAllSessions(userDetails, request);
    }
}
