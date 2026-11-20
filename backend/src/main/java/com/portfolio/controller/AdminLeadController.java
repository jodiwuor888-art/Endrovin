package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.ContactMessageResponse;
import com.portfolio.service.AdminManagementService;
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
@RequestMapping("/api/admin/leads")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin CRM & Leads", description = "Lightweight CRM pipeline, client inquiries, quotations, follow-ups")
public class AdminLeadController {

    private final AdminManagementService managementService;

    public AdminLeadController(AdminManagementService managementService) {
        this.managementService = managementService;
    }

    @GetMapping
    @Operation(summary = "Get all CRM leads and inquiries")
    public ResponseEntity<ApiResponse<List<ContactMessageResponse>>> getAllLeads() {
        return ResponseEntity.ok(ApiResponse.ok(managementService.getAllMessages()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update client lead pipeline status, notes, budget, and follow-up date")
    public ResponseEntity<ApiResponse<ContactMessageResponse>> updateLead(
            @PathVariable Long id,
            @RequestBody ContactMessageResponse payload,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        String username = userDetails != null ? userDetails.getUsername() : "admin";
        String ip = getClientIp(request);
        return ResponseEntity.ok(ApiResponse.ok("Lead updated successfully",
                managementService.updateLeadDetails(id, payload, username, ip)));
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
