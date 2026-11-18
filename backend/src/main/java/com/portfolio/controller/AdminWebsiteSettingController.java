package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.BatchSettingsUpdateRequest;
import com.portfolio.dto.SettingsAuditTrailDto;
import com.portfolio.dto.WebsiteSettingDto;
import com.portfolio.service.AdminManagementService;
import com.portfolio.service.WebsiteSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.portfolio.controller.PublishVersionController;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Web Settings", description = "Website global configuration, theme appearance, SEO, security and maintenance settings")
@SuppressWarnings("null")
public class AdminWebsiteSettingController {

    private final WebsiteSettingsService settingsService;
    private final AdminManagementService managementService;

    public AdminWebsiteSettingController(WebsiteSettingsService settingsService,
                                         AdminManagementService managementService) {
        this.settingsService = settingsService;
        this.managementService = managementService;
    }

    @GetMapping
    @Operation(summary = "List all settings organized by the 13 categories with system defaults")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.ok(settingsService.getAllSettingsCategorized()));
    }

    @PostMapping("/batch")
    @Operation(summary = "Batch save and update multiple settings across all categories")
    public ResponseEntity<ApiResponse<Void>> batchSaveSettings(
            @RequestBody(required = false) BatchSettingsUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        String ip = getClientIp(httpRequest);
        if (request != null && request.getSettings() != null) {
            settingsService.batchSaveSettings(request.getSettings(), userDetails.getUsername(), ip);
        }
        // Bump publish version so public portal detects changes immediately
        PublishVersionController.VERSION.set(System.currentTimeMillis());
        return ResponseEntity.ok(ApiResponse.ok("Settings saved successfully to database", null));
    }

    @PostMapping("/publish")
    @Operation(summary = "Publish current configuration changes to public website")
    public ResponseEntity<ApiResponse<Void>> publishSettings(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        String ip = getClientIp(httpRequest);
        settingsService.publishSettings(userDetails.getUsername(), ip);
        return ResponseEntity.ok(ApiResponse.ok("Settings published and active on public portfolio", null));
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset all website settings to factory defaults")
    public ResponseEntity<ApiResponse<Void>> resetToDefaults(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        String ip = getClientIp(httpRequest);
        settingsService.resetToDefaults(userDetails.getUsername(), ip);
        return ResponseEntity.ok(ApiResponse.ok("All website settings have been reset to system defaults", null));
    }

    @GetMapping("/export")
    @Operation(summary = "Export complete configuration as a downloadable JSON backup")
    @SuppressWarnings("null")
    public ResponseEntity<byte[]> exportSettings(
            @RequestParam(required = false, defaultValue = "false") boolean includeSecrets) {

        String json = settingsService.exportSettingsJson(includeSecrets);
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"portfolio-settings-backup.json\"")
                .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .body(bytes);
    }

    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Import and apply configuration from a valid JSON backup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importSettings(
            @RequestBody String jsonContent,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        String ip = getClientIp(httpRequest);
        int count = settingsService.importSettingsJson(jsonContent, userDetails.getUsername(), ip);
        return ResponseEntity.ok(ApiResponse.ok("Imported and applied " + count + " settings successfully",
                Map.of("importedCount", count)));
    }

    @GetMapping("/audit")
    @Operation(summary = "Retrieve audit trail of recent website settings modifications")
    public ResponseEntity<ApiResponse<List<SettingsAuditTrailDto>>> getSettingsAudit() {
        return ResponseEntity.ok(ApiResponse.ok(settingsService.getSettingsAuditTrail()));
    }

    @DeleteMapping("/audit/{id}")
    @Operation(summary = "Delete a settings audit trail entry by ID")
    public ResponseEntity<ApiResponse<Void>> deleteSettingsAuditEntry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String username = userDetails != null ? userDetails.getUsername() : "admin";
        managementService.deleteAuditLog(id, username, getClientIp(request));
        return ResponseEntity.ok(ApiResponse.ok("Settings audit entry deleted successfully", null));
    }

    @PostMapping
    @Operation(summary = "Save or update a single website setting (backward compatibility)")
    public ResponseEntity<ApiResponse<WebsiteSettingDto>> saveSingleSetting(
            @Valid @RequestBody WebsiteSettingDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        WebsiteSettingDto saved = managementService.saveSetting(dto, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Setting saved successfully", saved));
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
