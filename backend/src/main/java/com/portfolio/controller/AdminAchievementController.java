package com.portfolio.controller;

import com.portfolio.dto.AchievementDto;
import com.portfolio.dto.ApiResponse;
import com.portfolio.service.AdminManagementService;
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

@RestController
@RequestMapping("/api/admin/achievements")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Achievements", description = "Honors, awards and achievements management")
public class AdminAchievementController {

    private final AdminManagementService managementService;

    public AdminAchievementController(AdminManagementService managementService) {
        this.managementService = managementService;
    }

    @GetMapping
    @Operation(summary = "List all achievements")
    public ResponseEntity<ApiResponse<List<AchievementDto>>> getAllAchievements() {
        return ResponseEntity.ok(ApiResponse.ok(managementService.getAllAchievements()));
    }

    @PostMapping
    @Operation(summary = "Create an achievement")
    public ResponseEntity<ApiResponse<AchievementDto>> createAchievement(
            @Valid @RequestBody AchievementDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        AchievementDto created = managementService.createAchievement(dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.created("Achievement created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an achievement")
    public ResponseEntity<ApiResponse<AchievementDto>> updateAchievement(
            @PathVariable Long id,
            @Valid @RequestBody AchievementDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        AchievementDto updated = managementService.updateAchievement(id, dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Achievement updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an achievement")
    public ResponseEntity<ApiResponse<Void>> deleteAchievement(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        managementService.deleteAchievement(id, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Achievement deleted successfully", null));
    }
}
