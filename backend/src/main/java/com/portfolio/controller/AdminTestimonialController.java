package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.TestimonialDto;
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
import java.util.Map;

@RestController
@RequestMapping("/api/admin/testimonials")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Testimonials", description = "Testimonial review, approval workflow and management")
public class AdminTestimonialController {

    private final AdminManagementService managementService;

    public AdminTestimonialController(AdminManagementService managementService) {
        this.managementService = managementService;
    }

    @GetMapping
    @Operation(summary = "List testimonials filtered by status and search query")
    public ResponseEntity<ApiResponse<List<TestimonialDto>>> getTestimonials(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(ApiResponse.ok(managementService.getTestimonialsFiltered(status, query)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full testimonial details by ID")
    public ResponseEntity<ApiResponse<TestimonialDto>> getTestimonialById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(managementService.getTestimonialById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a testimonial manually by admin")
    public ResponseEntity<ApiResponse<TestimonialDto>> createTestimonial(
            @Valid @RequestBody TestimonialDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        TestimonialDto created = managementService.createTestimonial(dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.created("Testimonial created successfully", created));
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve testimonial for public display (requires consentToPublish)")
    public ResponseEntity<ApiResponse<TestimonialDto>> approveTestimonial(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        TestimonialDto updated = managementService.updateApprovalStatus(id, "APPROVED", userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Testimonial approved successfully", updated));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject testimonial with optional reason")
    public ResponseEntity<ApiResponse<TestimonialDto>> rejectTestimonial(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String reason = (body != null && body.containsKey("reason")) ? body.get("reason") : "Does not meet publication guidelines";
        TestimonialDto updated = managementService.rejectTestimonial(id, reason, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Testimonial rejected", updated));
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive testimonial")
    public ResponseEntity<ApiResponse<TestimonialDto>> archiveTestimonial(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        TestimonialDto updated = managementService.updateApprovalStatus(id, "ARCHIVED", userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Testimonial archived successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a testimonial permanently")
    public ResponseEntity<ApiResponse<Void>> deleteTestimonial(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        managementService.deleteTestimonial(id, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Testimonial deleted successfully", null));
    }
}
