package com.portfolio.dto;

import java.time.LocalDateTime;

public class AdminDeviceDto {
    private Long id;
    private String credentialId;
    private String deviceName;
    private String deviceType;
    private LocalDateTime registeredAt;
    private LocalDateTime lastUsedAt;
    private boolean currentDevice;

    public AdminDeviceDto() {
    }

    public AdminDeviceDto(Long id, String credentialId, String deviceName, String deviceType,
                          LocalDateTime registeredAt, LocalDateTime lastUsedAt, boolean currentDevice) {
        this.id = id;
        this.credentialId = credentialId;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.registeredAt = registeredAt;
        this.lastUsedAt = lastUsedAt;
        this.currentDevice = currentDevice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public boolean isCurrentDevice() {
        return currentDevice;
    }

    public void setCurrentDevice(boolean currentDevice) {
        this.currentDevice = currentDevice;
    }
}
