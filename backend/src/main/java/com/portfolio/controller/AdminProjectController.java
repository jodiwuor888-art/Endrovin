package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.ProjectDto;
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
@RequestMapping("/api/admin/projects")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Projects", description = "Project portfolio management with CRUD operations")
public class AdminProjectController {

    private final AdminManagementService managementService;

    public AdminProjectController(AdminManagementService managementService) {
        this.managementService = managementService;
    }

    @GetMapping
    @Operation(summary = "List all projects (all states)")
    public ResponseEntity<ApiResponse<List<ProjectDto>>> getAllProjects() {
        return ResponseEntity.ok(ApiResponse.ok(managementService.getAllProjects()));
    }

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ApiResponse<ProjectDto>> createProject(
            @Valid @RequestBody ProjectDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        ProjectDto created = managementService.createProject(dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.created("Project created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing project")
    public ResponseEntity<ApiResponse<ProjectDto>> updateProject(
            @PathVariable long id,
            @Valid @RequestBody ProjectDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        ProjectDto updated = managementService.updateProject(id, dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Project updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        managementService.deleteProject(id, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Project deleted successfully", null));
    }
}
