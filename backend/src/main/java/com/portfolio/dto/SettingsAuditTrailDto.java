package com.portfolio.dto;

import java.time.LocalDateTime;

public class SettingsAuditTrailDto {

    private Long id;
    private String action;
    private String settingKey;
    private String details;
    private String changedBy;
    private String ipAddress;
    private LocalDateTime timestamp;

    public SettingsAuditTrailDto() {
    }

    public SettingsAuditTrailDto(Long id, String action, String settingKey, String details, String changedBy, String ipAddress, LocalDateTime timestamp) {
        this.id = id;
        this.action = action;
        this.settingKey = settingKey;
        this.details = details;
        this.changedBy = changedBy;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
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
