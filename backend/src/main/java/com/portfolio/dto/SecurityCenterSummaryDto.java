package com.portfolio.dto;

import java.util.List;

public class SecurityCenterSummaryDto {
    private int securityScore; // e.g. 95/100
    private boolean mfaEnforced;
    private int enrolledPasskeysCount;
    private int activeDevicesCount;
    private long failedLoginsLast24h;
    private long totalAuditLogs;
    private List<AdminDeviceDto> activeDevices;
    private List<AuditLogDto> recentSecurityEvents;

    public SecurityCenterSummaryDto() {}

    public int getSecurityScore() { return securityScore; }
    public void setSecurityScore(int securityScore) { this.securityScore = securityScore; }
    public boolean isMfaEnforced() { return mfaEnforced; }
    public void setMfaEnforced(boolean mfaEnforced) { this.mfaEnforced = mfaEnforced; }
    public int getEnrolledPasskeysCount() { return enrolledPasskeysCount; }
    public void setEnrolledPasskeysCount(int enrolledPasskeysCount) { this.enrolledPasskeysCount = enrolledPasskeysCount; }
    public int getActiveDevicesCount() { return activeDevicesCount; }
    public void setActiveDevicesCount(int activeDevicesCount) { this.activeDevicesCount = activeDevicesCount; }
    public long getFailedLoginsLast24h() { return failedLoginsLast24h; }
    public void setFailedLoginsLast24h(long failedLoginsLast24h) { this.failedLoginsLast24h = failedLoginsLast24h; }
    public long getTotalAuditLogs() { return totalAuditLogs; }
    public void setTotalAuditLogs(long totalAuditLogs) { this.totalAuditLogs = totalAuditLogs; }
    public List<AdminDeviceDto> getActiveDevices() { return activeDevices; }
    public void setActiveDevices(List<AdminDeviceDto> activeDevices) { this.activeDevices = activeDevices; }
    public List<AuditLogDto> getRecentSecurityEvents() { return recentSecurityEvents; }
    public void setRecentSecurityEvents(List<AuditLogDto> recentSecurityEvents) { this.recentSecurityEvents = recentSecurityEvents; }
}
