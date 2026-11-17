package com.portfolio.service;

import com.portfolio.entity.AuditLog;
import com.portfolio.repository.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async
    public void logAction(String username, String action, String entityName, String entityId, String details, String ipAddress) {
        try {
            AuditLog log = new AuditLog(
                    username != null ? username : "SYSTEM",
                    action,
                    entityName,
                    entityId,
                    details,
                    ipAddress
            );
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);
        } catch (Exception e) {
            // Log failure should not break main business flow
        }
    }
}
