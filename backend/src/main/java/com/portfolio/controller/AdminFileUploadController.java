package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.entity.UploadedFile;
import com.portfolio.service.AuditLogService;
import com.portfolio.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/upload")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin File Upload", description = "Secure multi-part file uploads for images, CV, and certificates")
public class AdminFileUploadController {

    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;

    public AdminFileUploadController(FileStorageService fileStorageService, AuditLogService auditLogService) {
        this.fileStorageService = fileStorageService;
        this.auditLogService = auditLogService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload image or CV file with validation and SQL metadata storage")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "PROJECT_IMAGE") String category,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String username = userDetails.getUsername();
        UploadedFile saved = fileStorageService.storeFile(file, category.toUpperCase(), username);

        auditLogService.logAction(
                username,
                "FILE_UPLOAD",
                "UploadedFiles",
                String.valueOf(saved.getId()),
                "Uploaded file: " + saved.getOriginalFileName() + " (" + saved.getFileCategory() + ")",
                request.getRemoteAddr()
        );

        Map<String, Object> data = Map.of(
                "fileId", saved.getId(),
                "originalFileName", saved.getOriginalFileName(),
                "storedFileName", saved.getStoredFileName(),
                "fileUrl", saved.getStoragePath(),
                "fileCategory", saved.getFileCategory(),
                "fileSize", saved.getFileSize()
        );

        return ResponseEntity.ok(ApiResponse.ok("File uploaded successfully", data));
    }
}
