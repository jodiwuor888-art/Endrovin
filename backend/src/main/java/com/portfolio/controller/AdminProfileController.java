package com.portfolio.controller;

import com.portfolio.dto.AboutInformationDto;
import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.PersonalProfileDto;
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

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Profile & About", description = "Personal profile and biography management")
public class AdminProfileController {

    private final AdminManagementService managementService;

    public AdminProfileController(AdminManagementService managementService) {
        this.managementService = managementService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get personal profile information")
    public ResponseEntity<ApiResponse<PersonalProfileDto>> getProfile() {
        PersonalProfileDto profile = managementService.getProfile();
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PostMapping("/profile")
    @Operation(summary = "Save or update personal profile information")
    public ResponseEntity<ApiResponse<PersonalProfileDto>> saveProfile(
            @Valid @RequestBody PersonalProfileDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        PersonalProfileDto saved = managementService.saveProfile(dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", saved));
    }

    @GetMapping("/about")
    @Operation(summary = "Get about me information")
    public ResponseEntity<ApiResponse<AboutInformationDto>> getAbout() {
        AboutInformationDto about = managementService.getAbout();
        return ResponseEntity.ok(ApiResponse.ok(about));
    }

    @PostMapping("/about")
    @Operation(summary = "Save or update about me information")
    public ResponseEntity<ApiResponse<AboutInformationDto>> saveAbout(
            @Valid @RequestBody AboutInformationDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        AboutInformationDto saved = managementService.saveAbout(dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("About section updated successfully", saved));
    }
}
