package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.ContactMessageResponse;
import com.portfolio.entity.ContactMessage;
import com.portfolio.service.AdminManagementService;
import com.portfolio.service.EmailNotificationService;
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
@RequestMapping("/api/admin/messages")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Contact Messages", description = "View, mark as read, archive and delete client inquiries")
public class AdminContactMessageController {

    private final AdminManagementService managementService;
    private final EmailNotificationService emailNotificationService;

    public AdminContactMessageController(AdminManagementService managementService,
                                         EmailNotificationService emailNotificationService) {
        this.managementService = managementService;
        this.emailNotificationService = emailNotificationService;
    }

    @GetMapping
    @Operation(summary = "List all contact inquiries")
    public ResponseEntity<ApiResponse<List<ContactMessageResponse>>> getAllMessages() {
        return ResponseEntity.ok(ApiResponse.ok(managementService.getAllMessages()));
    }

    @PostMapping("/test-notification")
    @Operation(summary = "Send a test notification email to verify SMTP delivery")
    public ResponseEntity<ApiResponse<String>> sendTestNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        ContactMessage testMsg = new ContactMessage(
                "Test Visitor (Verification)",
                "test-inquiry@portfolio.com",
                "Quick Response System Verification",
                "This is a verification test to confirm email alerts arrive in your inbox when visitors submit messages.",
                request.getRemoteAddr()
        );
        emailNotificationService.sendContactNotification(testMsg);
        String target = emailNotificationService.getNotificationEmail();
        boolean ready = emailNotificationService.isConfigured();
        String status = ready
                ? "Test alert email dispatched to " + target
                : "Notice: SMTP credentials (SMTP_USERNAME / SMTP_PASSWORD) not configured on Render yet. Please check Render environment.";
        return ResponseEntity.ok(ApiResponse.ok(status, target));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark message as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        managementService.markMessageRead(id, true, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Message marked as read", null));
    }

    @PatchMapping("/{id}/unread")
    @Operation(summary = "Mark message as unread")
    public ResponseEntity<ApiResponse<Void>> markAsUnread(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        managementService.markMessageRead(id, false, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Message marked as unread", null));
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive message")
    public ResponseEntity<ApiResponse<Void>> archiveMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        managementService.archiveMessage(id, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Message archived", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete message")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        managementService.deleteMessage(id, userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Message deleted successfully", null));
    }
}
