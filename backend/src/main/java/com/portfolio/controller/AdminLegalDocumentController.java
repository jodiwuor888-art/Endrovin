package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.LegalDocumentDto;
import com.portfolio.security.IpAddressExtractor;
import com.portfolio.service.LegalAndPrivacyService;
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
@RequestMapping("/api/admin/legal")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Legal Documents", description = "Privacy Policy, Terms & Conditions, and Disclaimers management")
public class AdminLegalDocumentController {

    private final LegalAndPrivacyService legalService;
    private final IpAddressExtractor ipAddressExtractor;

    public AdminLegalDocumentController(LegalAndPrivacyService legalService, IpAddressExtractor ipAddressExtractor) {
        this.legalService = legalService;
        this.ipAddressExtractor = ipAddressExtractor;
    }

    @GetMapping("/documents")
    @Operation(summary = "Get all legal documents")
    public ResponseEntity<ApiResponse<List<LegalDocumentDto>>> getAllDocuments() {
        return ResponseEntity.ok(ApiResponse.ok(legalService.getAllLegalDocuments()));
    }

    @PostMapping("/documents")
    @Operation(summary = "Create or update legal document")
    public ResponseEntity<ApiResponse<LegalDocumentDto>> saveDocument(
            @RequestBody LegalDocumentDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        String ip = ipAddressExtractor.extract(request);
        return ResponseEntity.ok(ApiResponse.ok("Legal document saved successfully", legalService.saveLegalDocument(dto, username, ip)));
    }
}
