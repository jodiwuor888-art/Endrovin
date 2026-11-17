package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.ExperienceDto;
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
@RequestMapping("/api/admin/experience")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Experience", description = "Professional and work experience management")
public class AdminExperienceController {

    private final AdminManagementService managementService;

    public AdminExperienceController(AdminManagementService managementService) {
        this.managementService = managementService;
    }

    @GetMapping
    @Operation(summary = "List all experience records")
    public ResponseEntity<ApiResponse<List<ExperienceDto>>> getAllExperience() {
        return ResponseEntity.ok(ApiResponse.ok(managementService.getAllExperience()));
    }

    @PostMapping
    @Operation(summary = "Create an experience record")
    public ResponseEntity<ApiResponse<ExperienceDto>> createExperience(
            @Valid @RequestBody ExperienceDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        ExperienceDto created = managementService.createExperience(dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.created("Experience record created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an experience record")
    public ResponseEntity<ApiResponse<ExperienceDto>> updateExperience(
            @PathVariable Long id,
            @Valid @RequestBody ExperienceDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        ExperienceDto updated = managementService.updateExperience(id, dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Experience record updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an experience record")
    public ResponseEntity<ApiResponse<Void>> deleteExperience(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        managementService.deleteExperience(id, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Experience record deleted successfully", null));
    }
}
