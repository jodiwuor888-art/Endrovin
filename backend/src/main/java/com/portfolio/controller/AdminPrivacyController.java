package com.portfolio.controller;

import com.portfolio.dto.*;
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
import java.util.Map;

@RestController
@RequestMapping("/api/admin/privacy")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Privacy & Compliance", description = "Cookie consent records, DSAR requests, third-party processors, breach log")
public class AdminPrivacyController {

    private final LegalAndPrivacyService privacyService;
    private final IpAddressExtractor ipAddressExtractor;

    public AdminPrivacyController(LegalAndPrivacyService privacyService, IpAddressExtractor ipAddressExtractor) {
        this.privacyService = privacyService;
        this.ipAddressExtractor = ipAddressExtractor;
    }

    @GetMapping({"/consent-stats", "/dashboard"})
    @Operation(summary = "Get aggregated cookie consent statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConsentStats() {
        return ResponseEntity.ok(ApiResponse.ok(privacyService.getConsentStatistics()));
    }

    @GetMapping("/requests")
    @Operation(summary = "Get all Data Subject Access/Deletion Requests (DSAR)")
    public ResponseEntity<ApiResponse<List<DataSubjectRequestDto>>> getAllRequests() {
        return ResponseEntity.ok(ApiResponse.ok(privacyService.getAllDataRequests()));
    }

    @PutMapping("/requests/{id}/status")
    @Operation(summary = "Update status of a Data Subject Request")
    public ResponseEntity<ApiResponse<DataSubjectRequestDto>> updateRequestStatus(
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        String ip = ipAddressExtractor.extract(request);
        String finalStatus = (status != null && !status.isBlank()) ? status : (payload != null ? payload.get("status") : null);
        String notes = payload != null ? payload.get("adminNotes") : null;
        return ResponseEntity.ok(ApiResponse.ok("Request updated", privacyService.updateDataRequestStatus(id, finalStatus, notes, username, ip)));
    }

    @GetMapping("/processors")
    @Operation(summary = "Get all registered third-party data processors")
    public ResponseEntity<ApiResponse<List<ThirdPartyProcessorDto>>> getAllProcessors() {
        return ResponseEntity.ok(ApiResponse.ok(privacyService.getAllProcessors()));
    }

    @PostMapping("/processors")
    @Operation(summary = "Save or update third-party processor record")
    public ResponseEntity<ApiResponse<ThirdPartyProcessorDto>> saveProcessor(
            @RequestBody ThirdPartyProcessorDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        String ip = ipAddressExtractor.extract(request);
        return ResponseEntity.ok(ApiResponse.ok("Processor saved", privacyService.saveProcessor(dto, username, ip)));
    }

    @DeleteMapping("/processors/{id}")
    @Operation(summary = "Delete a third-party processor record")
    public ResponseEntity<ApiResponse<Void>> deleteProcessor(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        String ip = ipAddressExtractor.extract(request);
        privacyService.deleteProcessor(id, username, ip);
        return ResponseEntity.ok(ApiResponse.ok("Processor deleted successfully", null));
    }

    @GetMapping("/breaches")
    @Operation(summary = "Get data breach incident register")
    public ResponseEntity<ApiResponse<List<DataBreachIncidentDto>>> getAllBreaches() {
        return ResponseEntity.ok(ApiResponse.ok(privacyService.getAllBreaches()));
    }

    @PostMapping("/breaches")
    @Operation(summary = "Record or update data breach incident")
    public ResponseEntity<ApiResponse<DataBreachIncidentDto>> recordBreach(
            @RequestBody DataBreachIncidentDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        String ip = ipAddressExtractor.extract(request);
        return ResponseEntity.ok(ApiResponse.ok("Breach incident recorded", privacyService.recordBreach(dto, username, ip)));
    }
}
