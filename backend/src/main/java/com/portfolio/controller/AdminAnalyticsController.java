package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.AnalyticsSummaryResponse;
import com.portfolio.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Analytics", description = "First-party website traffic, page views, and conversion analytics")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    public AdminAnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get aggregated analytics metrics by period (today, yesterday, 7d, 30d, 90d)")
    public ResponseEntity<ApiResponse<AnalyticsSummaryResponse>> getSummary(
            @RequestParam(defaultValue = "30d") String period) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getSummary(period)));
    }
}
