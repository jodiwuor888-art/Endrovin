package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_devices")
public class AdminDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Admin admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credential_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private AdminPasskeyCredential credential;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName; // e.g. "ThinkPad Laptop - Windows Hello", "Android Smartphone", "iPhone"

    @Column(name = "device_type", nullable = false, length = 50)
    private String deviceType; // LAPTOP_WINDOWS_HELLO, ANDROID_PHONE, IPHONE_BIOMETRIC, SECURITY_KEY

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    public AdminDevice() {
    }

    public AdminDevice(Admin admin, AdminPasskeyCredential credential, String deviceName, String deviceType) {
        this.admin = admin;
        this.credential = credential;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.registeredAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getDeviceId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public AdminPasskeyCredential getCredential() {
        return credential;
    }

    public void setCredential(AdminPasskeyCredential credential) {
        this.credential = credential;
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

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
