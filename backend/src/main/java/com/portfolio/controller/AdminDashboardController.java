package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.AuditLogDto;
import com.portfolio.dto.DashboardStatsResponse;
import com.portfolio.service.AdminManagementService;
import com.portfolio.service.DashboardStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dashboard", description = "Dashboard overview and real SQL Server statistics")
public class AdminDashboardController {

    private final DashboardStatsService statsService;
    private final AdminManagementService managementService;

    public AdminDashboardController(DashboardStatsService statsService, AdminManagementService managementService) {
        this.statsService = statsService;
        this.managementService = managementService;
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
}
