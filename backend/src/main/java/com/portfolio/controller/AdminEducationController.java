package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.EducationDto;
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
@RequestMapping("/api/admin/education")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Education", description = "Education and qualifications management")
public class AdminEducationController {

    private final AdminManagementService managementService;

    public AdminEducationController(AdminManagementService managementService) {
        this.managementService = managementService;
    }

    @GetMapping
    @Operation(summary = "List all education records")
    public ResponseEntity<ApiResponse<List<EducationDto>>> getAllEducation() {
        return ResponseEntity.ok(ApiResponse.ok(managementService.getAllEducation()));
    }

    @PostMapping
    @Operation(summary = "Create an education record")
    public ResponseEntity<ApiResponse<EducationDto>> createEducation(
            @Valid @RequestBody EducationDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        EducationDto created = managementService.createEducation(dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.created("Education record created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an education record")
    public ResponseEntity<ApiResponse<EducationDto>> updateEducation(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody EducationDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        EducationDto updated = managementService.updateEducation(id, dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Education record updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an education record")
    public ResponseEntity<ApiResponse<Void>> deleteEducation(
            @PathVariable @NonNull Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        managementService.deleteEducation(id, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Education record deleted successfully", null));
    }
}
