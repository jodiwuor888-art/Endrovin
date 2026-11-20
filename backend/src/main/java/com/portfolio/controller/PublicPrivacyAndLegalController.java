package com.portfolio.controller;

import com.portfolio.dto.*;
import com.portfolio.security.IpAddressExtractor;
import com.portfolio.service.AnalyticsService;
import com.portfolio.service.LegalAndPrivacyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Public Privacy, Legal & Analytics", description = "Public privacy policy, terms, cookie consent recording, and first-party event tracking")
public class PublicPrivacyAndLegalController {

    private final LegalAndPrivacyService legalAndPrivacyService;
    private final AnalyticsService analyticsService;
    private final IpAddressExtractor ipExtractor;

    public PublicPrivacyAndLegalController(LegalAndPrivacyService legalAndPrivacyService,
                                           AnalyticsService analyticsService,
                                           IpAddressExtractor ipExtractor) {
        this.legalAndPrivacyService = legalAndPrivacyService;
        this.analyticsService = analyticsService;
        this.ipExtractor = ipExtractor;
    }

    @GetMapping("/legal/privacy-policy")
    @Operation(summary = "Get the current published Privacy Policy")
    public ResponseEntity<ApiResponse<LegalDocumentDto>> getPrivacyPolicy() {
        return ResponseEntity.ok(ApiResponse.ok(legalAndPrivacyService.getPublishedDocument("PRIVACY_POLICY")));
    }

    @GetMapping("/legal/terms")
    @Operation(summary = "Get the current published Terms of Service")
    public ResponseEntity<ApiResponse<LegalDocumentDto>> getTerms() {
        return ResponseEntity.ok(ApiResponse.ok(legalAndPrivacyService.getPublishedDocument("TERMS_AND_CONDITIONS")));
    }

    @PostMapping("/privacy/consent")
    @Operation(summary = "Record visitor cookie consent choices compliant with Kenya Data Protection Act 2019")
    public ResponseEntity<ApiResponse<String>> recordConsent(
            @RequestBody CookieConsentDto dto,
            HttpServletRequest request) {
        String ip = ipExtractor.extract(request);
        legalAndPrivacyService.recordCookieConsent(dto, ip);
        return ResponseEntity.ok(ApiResponse.ok("Consent recorded successfully", "OK"));
    }

    @PostMapping("/privacy/request")
    @Operation(summary = "Submit a public Data Subject Access, Correction, or Deletion Request (DSAR)")
    public ResponseEntity<ApiResponse<String>> submitDataRequest(
            @RequestBody DataSubjectRequestDto dto,
            HttpServletRequest request) {
        String ip = ipExtractor.extract(request);
        legalAndPrivacyService.submitPublicDataRequest(dto, ip);
        return ResponseEntity.ok(ApiResponse.ok("Your privacy request has been received and will be processed within statutory timeframes.", "SUBMITTED"));
    }

    @PostMapping("/analytics/event")
    @Operation(summary = "Record first-party analytics event (page_view, project_view, etc.)")
    public ResponseEntity<ApiResponse<Void>> recordAnalyticsEvent(
            @RequestBody AnalyticsEventRequest req,
            HttpServletRequest request) {
        String ip = ipExtractor.extract(request);
        analyticsService.recordEvent(req, ip);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
