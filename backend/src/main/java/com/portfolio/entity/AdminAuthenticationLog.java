package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_authentication_logs")
public class AdminAuthenticationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "authentication_method", nullable = false, length = 50)
    private String authenticationMethod; // "PASSWORD", "WEBAUTHN_BIOMETRIC", "RECOVERY_CODE"

    @Column(name = "device_information", length = 255)
    private String deviceInformation;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "authentication_status", nullable = false, length = 50)
    private String authenticationStatus; // "SUCCESS", "FAILED", "PENDING_MFA"

    @Column(name = "event_timestamp", nullable = false, updatable = false)
    private LocalDateTime eventTimestamp = LocalDateTime.now();

    public AdminAuthenticationLog() {
    }

    public AdminAuthenticationLog(Long adminId, String method, String deviceInfo, String ip, String status) {
        this.adminId = adminId;
        this.authenticationMethod = method;
        this.deviceInformation = deviceInfo;
        this.ipAddress = ip;
        this.authenticationStatus = status;
        this.eventTimestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public String getDeviceInformation() {
        return deviceInformation;
    }

    public void setDeviceInformation(String deviceInformation) {
        this.deviceInformation = deviceInformation;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getAuthenticationStatus() {
        return authenticationStatus;
    }

    public void setAuthenticationStatus(String authenticationStatus) {
        this.authenticationStatus = authenticationStatus;
    }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
}
