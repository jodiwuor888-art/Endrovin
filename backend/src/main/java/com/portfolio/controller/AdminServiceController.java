package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.ServiceDto;
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
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/services")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Services", description = "Services offered management")
public class AdminServiceController {

    private final AdminManagementService managementService;

    public AdminServiceController(AdminManagementService managementService) {
        this.managementService = managementService;
    }

    @GetMapping
    @Operation(summary = "List all services")
    public ResponseEntity<ApiResponse<List<ServiceDto>>> getAllServices() {
        return ResponseEntity.ok(ApiResponse.ok(managementService.getAllServices()));
    }

    @PostMapping
    @Operation(summary = "Create a service")
    public ResponseEntity<ApiResponse<ServiceDto>> createService(
            @Valid @RequestBody ServiceDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        ServiceDto created = managementService.createService(dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.created("Service created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a service")
    public ResponseEntity<ApiResponse<ServiceDto>> updateService(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody ServiceDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        ServiceDto updated = managementService.updateService(id, dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Service updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a service")
    public ResponseEntity<ApiResponse<Void>> deleteService(
            @PathVariable @NonNull Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        managementService.deleteService(Objects.requireNonNull(id), userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Service deleted successfully", null));
    }
}
