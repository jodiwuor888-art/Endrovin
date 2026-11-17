package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.SkillCategoryDto;
import com.portfolio.dto.SkillDto;
import com.portfolio.service.AdminManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Skills & Categories", description = "Manage technical skills and categorization")
public class AdminSkillController {

    private final AdminManagementService managementService;

    public AdminSkillController(AdminManagementService managementService) {
        this.managementService = managementService;
    }

    @GetMapping("/skill-categories")
    @Operation(summary = "Get all categories with their associated skills")
    public ResponseEntity<ApiResponse<List<SkillCategoryDto>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(managementService.getAllCategoriesWithSkills()));
    }

    @PostMapping("/skill-categories")
    @Operation(summary = "Create a skill category")
    public ResponseEntity<ApiResponse<SkillCategoryDto>> createCategory(
            @Valid @RequestBody SkillCategoryDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        SkillCategoryDto created = managementService.createCategory(dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.created("Category created successfully", created));
    }

    @DeleteMapping("/skill-categories/{id}")
    @Operation(summary = "Delete a skill category and its skills")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable @NonNull Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        managementService.deleteCategory(id, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Category deleted successfully", null));
    }

    @GetMapping("/skills")
    @Operation(summary = "Get flat list of all skills")
    public ResponseEntity<ApiResponse<List<SkillDto>>> getSkills() {
        return ResponseEntity.ok(ApiResponse.ok(managementService.getAllSkills()));
    }

    @PostMapping("/skills")
    @Operation(summary = "Create a new technical skill")
    public ResponseEntity<ApiResponse<SkillDto>> createSkill(
            @Valid @RequestBody SkillDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        SkillDto created = managementService.createSkill(dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.created("Skill created successfully", created));
    }

    @PutMapping("/skills/{id}")
    @Operation(summary = "Update an existing skill")
    public ResponseEntity<ApiResponse<SkillDto>> updateSkill(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody SkillDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        SkillDto updated = managementService.updateSkill(id, dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Skill updated successfully", updated));
    }

    @DeleteMapping("/skills/{id}")
    @Operation(summary = "Delete a skill")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(
            @PathVariable @NonNull Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        managementService.deleteSkill(id, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Skill deleted successfully", null));
    }
}
