package com.portfolio.service;

import com.portfolio.dto.AdminDeviceDto;
import com.portfolio.dto.AuditLogDto;
import com.portfolio.dto.SecurityCenterSummaryDto;
import com.portfolio.entity.Admin;
import com.portfolio.entity.AdminAuthenticationLog;
import com.portfolio.entity.AdminDevice;
import com.portfolio.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecurityCenterService {

    private final AdminDeviceRepository adminDeviceRepository;
    private final AdminPasskeyCredentialRepository passkeyRepository;
    private final AdminAuthenticationLogRepository authLogRepository;
    private final AuditLogRepository auditLogRepository;
    private final AdminRepository adminRepository;
    private final AuditLogService auditLogService;

    public SecurityCenterService(AdminDeviceRepository adminDeviceRepository,
                                 AdminPasskeyCredentialRepository passkeyRepository,
                                 AdminAuthenticationLogRepository authLogRepository,
                                 AuditLogRepository auditLogRepository,
                                 AdminRepository adminRepository,
                                 AuditLogService auditLogService) {
        this.adminDeviceRepository = adminDeviceRepository;
        this.passkeyRepository = passkeyRepository;
        this.authLogRepository = authLogRepository;
        this.auditLogRepository = auditLogRepository;
        this.adminRepository = adminRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public SecurityCenterSummaryDto getSecurityOverview() {
        SecurityCenterSummaryDto dto = new SecurityCenterSummaryDto();

        int passkeysCount = (int) passkeyRepository.count();
        int devicesCount = (int) adminDeviceRepository.countByRevokedAtIsNull();
        long failedLogins = authLogRepository.countByAuthenticationStatusAndEventTimestampAfter("FAILED", LocalDateTime.now().minusHours(24));
        long totalLogs = auditLogRepository.count();

        // Calculate security score
        int score = 70;
        if (passkeysCount > 0) score += 20; // MFA configured
        if (failedLogins == 0) score += 10; else score -= (int) Math.min(20, failedLogins * 5);
        dto.setSecurityScore(Math.max(10, Math.min(100, score)));

        dto.setMfaEnforced(passkeysCount > 0);
        dto.setEnrolledPasskeysCount(passkeysCount);
        dto.setActiveDevicesCount(devicesCount);
        dto.setFailedLoginsLast24h(failedLogins);
        dto.setTotalAuditLogs(totalLogs);

        List<AdminDeviceDto> devices = adminDeviceRepository.findByRevokedAtIsNullOrderByLastUsedAtDesc().stream().map(d -> {
            AdminDeviceDto dev = new AdminDeviceDto();
            dev.setId(d.getId());
            dev.setDeviceName(d.getDeviceName());
            dev.setDeviceType(d.getDeviceType());
            dev.setRegisteredAt(d.getRegisteredAt());
            dev.setLastUsedAt(d.getLastUsedAt());
            return dev;
        }).collect(Collectors.toList());
        dto.setActiveDevices(devices);

        List<AuditLogDto> logs = auditLogRepository.findTop50ByOrderByTimestampDesc().stream().map(l -> {
            AuditLogDto log = new AuditLogDto();
            log.setId(l.getId());
            log.setAdminUsername(l.getAdminUsername());
            log.setAction(l.getAction());
            log.setEntityName(l.getEntityName());
            log.setEntityId(l.getEntityId());
            log.setDetails(l.getDetails());
            log.setIpAddress(l.getIpAddress());
            log.setTimestamp(l.getTimestamp());
            return log;
        }).collect(Collectors.toList());
        dto.setRecentSecurityEvents(logs);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<AdminAuthenticationLog> getAuthenticationLogs() {
        return authLogRepository.findTop50ByOrderByEventTimestampDesc();
    }

    @Transactional
    public void revokeDevice(Long deviceId, String username, String ip) {
        adminDeviceRepository.findById(deviceId).ifPresent(d -> {
            d.setRevokedAt(LocalDateTime.now());
            adminDeviceRepository.save(d);
            auditLogService.logAction(username, "REVOKE_DEVICE", "AdminDevices", String.valueOf(deviceId), "Revoked device: " + d.getDeviceName(), ip);
        });
    }

    @Transactional
    public void revokeAllAdminSessions(String username, String ip) {
        adminRepository.findByUsername(username).ifPresent(admin -> {
            admin.setTokensInvalidBefore(LocalDateTime.now());
            adminRepository.save(admin);
            auditLogService.logAction(username, "REVOKE_ALL_SESSIONS", "Admins", String.valueOf(admin.getId()), "Emergency session revocation triggered - all prior JWTs invalidated", ip);
        });
    }
}
