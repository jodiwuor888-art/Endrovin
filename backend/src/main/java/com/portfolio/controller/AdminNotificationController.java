package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.entity.SystemNotification;
import com.portfolio.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Notifications", description = "In-app notifications and alerts")
public class AdminNotificationController {

    private final NotificationService notificationService;
    private final com.portfolio.service.EmailNotificationService emailNotificationService;

    public AdminNotificationController(NotificationService notificationService,
                                       com.portfolio.service.EmailNotificationService emailNotificationService) {
        this.notificationService = notificationService;
        this.emailNotificationService = emailNotificationService;
    }

    @GetMapping("/smtp-diagnostics")
    @Operation(summary = "Check live SMTP connectivity, port accessibility, and test real-time notification delivery (Admin only)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkSmtpStatus() {
        return ResponseEntity.ok(ApiResponse.ok(emailNotificationService.runDiagnostics()));
    }

    @GetMapping
    @Operation(summary = "Get recent notifications and unread count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotifications() {
        Map<String, Object> res = new HashMap<>();
        res.put("unreadCount", notificationService.getUnreadCount());
        res.put("notifications", notificationService.getRecentNotifications());
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark single notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }
}
