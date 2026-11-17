package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.SocialLinkDto;
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
@RequestMapping("/api/admin/social-links")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Social Links", description = "Social media links management")
public class AdminSocialLinkController {

    private final AdminManagementService managementService;

    public AdminSocialLinkController(AdminManagementService managementService) {
        this.managementService = managementService;
    }

    @GetMapping
    @Operation(summary = "List all social links")
    public ResponseEntity<ApiResponse<List<SocialLinkDto>>> getAllSocialLinks() {
        return ResponseEntity.ok(ApiResponse.ok(managementService.getAllSocialLinks()));
    }

    @PostMapping
    @Operation(summary = "Create a social link")
    public ResponseEntity<ApiResponse<SocialLinkDto>> createSocialLink(
            @Valid @RequestBody SocialLinkDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        SocialLinkDto created = managementService.createSocialLink(dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.created("Social link created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a social link")
    public ResponseEntity<ApiResponse<SocialLinkDto>> updateSocialLink(
            @PathVariable Long id,
            @Valid @RequestBody SocialLinkDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        SocialLinkDto updated = managementService.updateSocialLink(id, dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Social link updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a social link")
    public ResponseEntity<ApiResponse<Void>> deleteSocialLink(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        managementService.deleteSocialLink(id, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Social link deleted successfully", null));
    }
}
