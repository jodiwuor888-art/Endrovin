package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.AuditLogDto;
import com.portfolio.dto.DashboardStatsResponse;
import com.portfolio.security.IpAddressExtractor;
import com.portfolio.service.AdminManagementService;
import com.portfolio.service.DashboardStatsService;
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
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dashboard", description = "Dashboard overview, statistics and audit logs")
public class AdminDashboardController {

    private final DashboardStatsService statsService;
    private final AdminManagementService managementService;
    private final IpAddressExtractor ipAddressExtractor;

    public AdminDashboardController(DashboardStatsService statsService,
                                    AdminManagementService managementService,
                                    IpAddressExtractor ipAddressExtractor) {
        this.statsService = statsService;
        this.managementService = managementService;
        this.ipAddressExtractor = ipAddressExtractor;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get genuine SQL Server counts (zero if no records)")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        DashboardStatsResponse stats = statsService.getStatistics();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get 50 most recent security and administrative audit logs")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getAuditLogs() {
        List<AuditLogDto> logs = managementService.getRecentAuditLogs();
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }

    @DeleteMapping("/audit-logs/{id}")
    @Operation(summary = "Delete an audit log entry by ID")
    public ResponseEntity<ApiResponse<Void>> deleteAuditLog(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String username = userDetails != null ? userDetails.getUsername() : "admin";
        String ip = ipAddressExtractor.extract(request);
        managementService.deleteAuditLog(id, username, ip);
        return ResponseEntity.ok(ApiResponse.ok("Audit log entry deleted successfully", null));
    }

    @DeleteMapping("/audit-logs")
    @Operation(summary = "Clear all audit log entries")
    public ResponseEntity<ApiResponse<Void>> clearAllAuditLogs(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String username = userDetails != null ? userDetails.getUsername() : "admin";
        String ip = ipAddressExtractor.extract(request);
        managementService.clearAllAuditLogs(username, ip);
        return ResponseEntity.ok(ApiResponse.ok("All audit logs cleared successfully", null));
    }
}
