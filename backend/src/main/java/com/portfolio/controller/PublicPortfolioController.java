package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.ContactMessageRequest;
import com.portfolio.dto.ContactMessageResponse;
import com.portfolio.dto.PublicPortfolioResponse;
import com.portfolio.security.IpAddressExtractor;
import com.portfolio.service.PublicPortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Public Portfolio", description = "Public portfolio modules and contact inquiries")
public class PublicPortfolioController {

    private final PublicPortfolioService publicPortfolioService;
    private final com.portfolio.service.WebsiteSettingsService settingsService;
    private final com.portfolio.service.EmailNotificationService emailNotificationService;
    private final IpAddressExtractor ipExtractor;

    public PublicPortfolioController(PublicPortfolioService publicPortfolioService,
                                   com.portfolio.service.WebsiteSettingsService settingsService,
                                   com.portfolio.service.EmailNotificationService emailNotificationService,
                                   IpAddressExtractor ipExtractor) {
        this.publicPortfolioService = publicPortfolioService;
        this.settingsService = settingsService;
        this.emailNotificationService = emailNotificationService;
        this.ipExtractor = ipExtractor;
    }

    @GetMapping("/smtp-status")
    @Operation(summary = "Check live SMTP connectivity, port accessibility, and test real-time notification delivery")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> checkSmtpStatus() {
        return ResponseEntity.ok(ApiResponse.ok(emailNotificationService.runDiagnostics()));
    }

    @GetMapping("/settings")
    @Operation(summary = "Get public-facing website settings (theme appearance, SEO metadata, section visibility, and branding)")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> getPublicSettings() {
        return ResponseEntity.ok(ApiResponse.ok(settingsService.getPublicSettings()));
    }

    @GetMapping("/portfolio")
    @Operation(summary = "Get full published portfolio content from SQL Server database")
    public ResponseEntity<ApiResponse<PublicPortfolioResponse>> getPortfolio() {
        PublicPortfolioResponse response = publicPortfolioService.getCompletePublicPortfolio();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/contact")
    @Operation(summary = "Submit contact inquiry with validation and spam protection")
    public ResponseEntity<ApiResponse<ContactMessageResponse>> submitContact(
            @Valid @RequestBody ContactMessageRequest request,
            HttpServletRequest httpRequest) {

        String ip = ipExtractor.extract(httpRequest);
        ContactMessageResponse response = publicPortfolioService.submitContactMessage(request, ip);
        return ResponseEntity.ok(ApiResponse.ok("Thank you! Your message has been received.", response));
    }

    @GetMapping("/testimonials")
    @Operation(summary = "Get list of approved client recommendations and testimonials")
    public ResponseEntity<ApiResponse<java.util.List<com.portfolio.dto.PublicTestimonialResponse>>> getTestimonials() {
        return ResponseEntity.ok(ApiResponse.ok(publicPortfolioService.getApprovedPublicTestimonials()));
    }

    @PostMapping("/testimonials")
    @Operation(summary = "Publicly submit client recommendation (saved as PENDING for admin review)")
    public ResponseEntity<ApiResponse<String>> submitTestimonial(
            @Valid @RequestBody com.portfolio.dto.PublicTestimonialSubmissionRequest request,
            HttpServletRequest httpRequest) {

        String ip = ipExtractor.extract(httpRequest);
        String message = publicPortfolioService.submitPublicRecommendation(request, ip);
        return ResponseEntity.ok(ApiResponse.ok(message, message));
    }

    @GetMapping("/testimonials/captcha")
    @Operation(summary = "Get a fresh math challenge for recommendation form human verification")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> getCaptcha() {
        return ResponseEntity.ok(ApiResponse.ok(publicPortfolioService.generateCaptcha()));
    }

}
