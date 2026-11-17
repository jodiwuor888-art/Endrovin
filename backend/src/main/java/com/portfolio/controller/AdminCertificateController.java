package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.CertificateDto;
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
@RequestMapping("/api/admin/certificates")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Certificates", description = "Certifications and licenses management")
public class AdminCertificateController {

    private final AdminManagementService managementService;

    public AdminCertificateController(AdminManagementService managementService) {
        this.managementService = managementService;
    }

    @GetMapping
    @Operation(summary = "List all certificates")
    public ResponseEntity<ApiResponse<List<CertificateDto>>> getAllCertificates() {
        return ResponseEntity.ok(ApiResponse.ok(managementService.getAllCertificates()));
    }

    @PostMapping
    @Operation(summary = "Create a certificate")
    public ResponseEntity<ApiResponse<CertificateDto>> createCertificate(
            @Valid @RequestBody CertificateDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        CertificateDto created = managementService.createCertificate(dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.created("Certificate created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a certificate")
    public ResponseEntity<ApiResponse<CertificateDto>> updateCertificate(
            @PathVariable("id") long certificateId,
            @Valid @RequestBody CertificateDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        CertificateDto updated = managementService.updateCertificate(certificateId, dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Certificate updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a certificate")
    public ResponseEntity<ApiResponse<Void>> deleteCertificate(
            @PathVariable("id") long certificateId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        managementService.deleteCertificate(certificateId, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.<Void>ok("Certificate deleted successfully", null));
    }
}
