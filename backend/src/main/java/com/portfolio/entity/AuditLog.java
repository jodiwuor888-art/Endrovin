package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AuditLogs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogID")
    private Long id;

    @Column(name = "AdminUsername", nullable = false, length = 50)
    private String adminUsername;

    @Column(name = "Action", nullable = false, length = 50)
    private String action; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, FAILED_LOGIN, UPLOAD

    @Column(name = "EntityName", nullable = false, length = 50)
    private String entityName;

    @Column(name = "EntityId", length = 50)
    private String entityId;

    @Lob
    @Column(name = "Details")
    private String details;

    @Column(name = "IpAddress", length = 50)
    private String ipAddress;

    @Column(name = "Timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public AuditLog() {
    }

    public AuditLog(String adminUsername, String action, String entityName, String entityId, String details, String ipAddress) {
        this.adminUsername = adminUsername;
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.details = details;
        this.ipAddress = ipAddress;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
