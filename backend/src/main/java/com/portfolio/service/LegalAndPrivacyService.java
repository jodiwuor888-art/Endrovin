package com.portfolio.service;

import com.portfolio.dto.*;
import com.portfolio.entity.*;
import com.portfolio.exception.BadRequestException;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LegalAndPrivacyService {

    private final LegalDocumentRepository legalDocumentRepository;
    private final CookieConsentRecordRepository cookieConsentRepository;
    private final DataSubjectRequestRepository dataSubjectRequestRepository;
    private final ThirdPartyProcessorRepository thirdPartyProcessorRepository;
    private final DataBreachIncidentRepository dataBreachIncidentRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final SecureRandom secureRandom = new SecureRandom();

    public LegalAndPrivacyService(LegalDocumentRepository legalDocumentRepository,
                                  CookieConsentRecordRepository cookieConsentRepository,
                                  DataSubjectRequestRepository dataSubjectRequestRepository,
                                  ThirdPartyProcessorRepository thirdPartyProcessorRepository,
                                  DataBreachIncidentRepository dataBreachIncidentRepository,
                                  AuditLogService auditLogService,
                                  NotificationService notificationService) {
        this.legalDocumentRepository = legalDocumentRepository;
        this.cookieConsentRepository = cookieConsentRepository;
        this.dataSubjectRequestRepository = dataSubjectRequestRepository;
        this.thirdPartyProcessorRepository = thirdPartyProcessorRepository;
        this.dataBreachIncidentRepository = dataBreachIncidentRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    // ==========================================
    // 1. LEGAL DOCUMENTS (Privacy & Terms)
    // ==========================================
    @Transactional(readOnly = true)
    public List<LegalDocumentDto> getAllLegalDocuments() {
        return legalDocumentRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::mapToLegalDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LegalDocumentDto getPublishedDocument(String documentType) {
        return legalDocumentRepository.findFirstByDocumentTypeAndStatusOrderByEffectiveDateDesc(documentType, "PUBLISHED")
                .map(this::mapToLegalDto)
                .orElse(getDefaultDocument(documentType));
    }

    @Transactional
    public LegalDocumentDto saveLegalDocument(LegalDocumentDto dto, String username, String ip) {
        LegalDocument doc;
        if (dto.getId() != null) {
            doc = legalDocumentRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Legal document not found: " + dto.getId()));
        } else {
            doc = new LegalDocument();
            doc.setDocumentType(dto.getDocumentType());
            doc.setCreatedBy(username);
        }
        doc.setTitle(dto.getTitle());
        doc.setVersion(dto.getVersion() != null ? dto.getVersion() : "1.0");
        doc.setContent(dto.getContent());
        doc.setStatus(dto.getStatus() != null ? dto.getStatus() : "PUBLISHED");
        if (dto.getEffectiveDate() != null) {
            doc.setEffectiveDate(dto.getEffectiveDate());
        }

        LegalDocument saved = legalDocumentRepository.save(doc);
        auditLogService.logAction(username, "SAVE_LEGAL_DOC", "LegalDocuments", String.valueOf(saved.getId()),
                "Saved " + saved.getDocumentType() + " version " + saved.getVersion(), ip);
        return mapToLegalDto(saved);
    }

    // ==========================================
    // 2. COOKIE CONSENT
    // ==========================================
    @Transactional
    public CookieConsentRecord recordCookieConsent(CookieConsentDto dto, String ip) {
        String uuid = dto.getConsentUuid();
        if (uuid == null || uuid.isBlank()) {
            uuid = UUID.randomUUID().toString();
        }
        String ipHash = hashIp(ip);

        CookieConsentRecord record = cookieConsentRepository.findByConsentUuid(uuid)
                .orElse(new CookieConsentRecord());

        record.setConsentUuid(uuid);
        record.setIpHash(ipHash);
        record.setNecessary(true);
        record.setFunctional(dto.isFunctional());
        record.setAnalytics(dto.isAnalytics());
        record.setMarketing(dto.isMarketing());
        record.setDecision(dto.getDecision() != null ? dto.getDecision() : "CUSTOM");
        record.setPolicyVersion(dto.getPolicyVersion() != null ? dto.getPolicyVersion() : "1.0");

        return cookieConsentRepository.save(record);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getConsentStatistics() {
        Map<String, Object> stats = new HashMap<>();
        long total = cookieConsentRepository.count();
        long acceptedAll = cookieConsentRepository.countByDecision("ACCEPT_ALL");
        long rejectedOptional = cookieConsentRepository.countByDecision("REJECT_OPTIONAL");
        long custom = cookieConsentRepository.countByDecision("CUSTOM");

        stats.put("totalRecords", total);
        stats.put("acceptedAll", acceptedAll);
        stats.put("rejectedOptional", rejectedOptional);
        stats.put("customPreferences", custom);
        return stats;
    }

    // ==========================================
    // 3. DATA SUBJECT REQUESTS (DSAR)
    // ==========================================
    @Transactional
    public DataSubjectRequest submitPublicDataRequest(DataSubjectRequestDto dto, String ip) {
        if (dto == null || dto.getRequesterName() == null || dto.getRequesterName().isBlank()
                || dto.getRequesterEmail() == null || dto.getRequesterEmail().isBlank()
                || dto.getDetails() == null || dto.getDetails().isBlank()) {
            throw new BadRequestException("Name, email address, and request details are required to submit a privacy request.");
        }

        String name = dto.getRequesterName().trim();
        if (name.length() > 100) name = name.substring(0, 100);

        String email = dto.getRequesterEmail().trim().toLowerCase();
        if (email.length() > 120) email = email.substring(0, 120);

        String type = dto.getRequestType() != null ? dto.getRequestType().trim().toUpperCase() : "ACCESS";
        if (type.length() > 50) type = type.substring(0, 50);

        String details = dto.getDetails().trim();
        if (details.length() > 5000) details = details.substring(0, 5000);

        String ticket = "DSAR-" + System.currentTimeMillis() + "-" + (1000 + secureRandom.nextInt(9000));
        DataSubjectRequest req = new DataSubjectRequest(
                ticket,
                name,
                email,
                type,
                details,
                ip
        );
        DataSubjectRequest saved = dataSubjectRequestRepository.save(req);
        notificationService.createNotification(
                "New Privacy Request (" + saved.getRequestType() + ")",
                "Request from " + saved.getRequesterName() + " (" + saved.getRequesterEmail() + ")",
                "PRIVACY_REQUEST",
                "privacy"
        );
        return saved;
    }

    @Transactional(readOnly = true)
    public List<DataSubjectRequestDto> getAllDataRequests() {
        return dataSubjectRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDsarDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DataSubjectRequestDto updateDataRequestStatus(Long id, String status, String adminNotes, String username, String ip) {
        DataSubjectRequest req = dataSubjectRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));

        req.setStatus(status);
        if (adminNotes != null) req.setAdminNotes(adminNotes);
        if ("COMPLETED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status)) {
            req.setResolvedAt(LocalDateTime.now());
        }
        DataSubjectRequest saved = dataSubjectRequestRepository.save(req);
        auditLogService.logAction(username, "UPDATE_DSAR", "DataSubjectRequests", String.valueOf(id),
                "Updated DSAR " + saved.getTicketNumber() + " status to " + status, ip);
        return mapToDsarDto(saved);
    }

    // ==========================================
    // 4. THIRD-PARTY PROCESSORS
    // ==========================================
    @Transactional(readOnly = true)
    public List<ThirdPartyProcessorDto> getAllProcessors() {
        return thirdPartyProcessorRepository.findAllByOrderByProviderNameAsc().stream()
                .map(this::mapToProcessorDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ThirdPartyProcessorDto saveProcessor(ThirdPartyProcessorDto dto, String username, String ip) {
        ThirdPartyProcessor p;
        if (dto.getId() != null) {
            p = thirdPartyProcessorRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Processor not found: " + dto.getId()));
        } else {
            p = new ThirdPartyProcessor();
        }
        p.setProviderName(dto.getProviderName());
        p.setPurpose(dto.getPurpose());
        p.setDataCategories(dto.getDataCategories());
        p.setProcessingLocation(dto.getProcessingLocation());
        p.setPrivacyPolicyUrl(dto.getPrivacyPolicyUrl());
        p.setDpaStatus(dto.getDpaStatus() != null ? dto.getDpaStatus() : "SIGNED");
        p.setEnabled(dto.isEnabled());
        p.setConsentCategory(dto.getConsentCategory() != null ? dto.getConsentCategory() : "NECESSARY");
        p.setLastReviewedAt(LocalDateTime.now());

        ThirdPartyProcessor saved = thirdPartyProcessorRepository.save(p);
        auditLogService.logAction(username, "SAVE_PROCESSOR", "ThirdPartyProcessors", String.valueOf(saved.getId()),
                "Saved processor: " + saved.getProviderName(), ip);
        return mapToProcessorDto(saved);
    }

    @Transactional
    public void deleteProcessor(Long id, String username, String ip) {
        thirdPartyProcessorRepository.deleteById(id);
        auditLogService.logAction(username, "DELETE_PROCESSOR", "ThirdPartyProcessors", String.valueOf(id), "Deleted processor ID " + id, ip);
    }

    // ==========================================
    // 5. DATA BREACH INCIDENTS
    // ==========================================
    @Transactional(readOnly = true)
    public List<DataBreachIncidentDto> getAllBreaches() {
        return dataBreachIncidentRepository.findAllByOrderByDiscoveryDateDesc().stream()
                .map(this::mapToBreachDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DataBreachIncidentDto recordBreach(DataBreachIncidentDto dto, String username, String ip) {
        DataBreachIncident incident;
        if (dto.getId() != null) {
            incident = dataBreachIncidentRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Breach record not found: " + dto.getId()));
        } else {
            incident = new DataBreachIncident();
        }
        incident.setIncidentTitle(dto.getIncidentTitle());
        if (dto.getDiscoveryDate() != null) incident.setDiscoveryDate(dto.getDiscoveryDate());
        incident.setAffectedSystems(dto.getAffectedSystems());
        incident.setDataCategoriesAffected(dto.getDataCategoriesAffected());
        incident.setRiskLevel(dto.getRiskLevel() != null ? dto.getRiskLevel() : "LOW");
        incident.setContainmentMeasures(dto.getContainmentMeasures());
        incident.setInvestigationSummary(dto.getInvestigationSummary());
        incident.setRegulatoryNotificationRequired(dto.isRegulatoryNotificationRequired());
        incident.setStatus(dto.getStatus() != null ? dto.getStatus() : "RESOLVED");
        if ("RESOLVED".equalsIgnoreCase(incident.getStatus())) {
            incident.setResolvedAt(LocalDateTime.now());
        }

        DataBreachIncident saved = dataBreachIncidentRepository.save(incident);
        auditLogService.logAction(username, "RECORD_BREACH", "DataBreachIncidents", String.valueOf(saved.getId()),
                "Recorded breach incident: " + saved.getIncidentTitle(), ip);
        return mapToBreachDto(saved);
    }

    // ==========================================
    // MAPPINGS & HELPERS
    // ==========================================
    private LegalDocumentDto mapToLegalDto(LegalDocument d) {
        LegalDocumentDto dto = new LegalDocumentDto();
        dto.setId(d.getId());
        dto.setDocumentType(d.getDocumentType());
        dto.setTitle(d.getTitle());
        dto.setVersion(d.getVersion());
        dto.setEffectiveDate(d.getEffectiveDate());
        dto.setContent(d.getContent());
        dto.setStatus(d.getStatus());
        dto.setCreatedBy(d.getCreatedBy());
        dto.setUpdatedAt(d.getUpdatedAt());
        return dto;
    }

    private DataSubjectRequestDto mapToDsarDto(DataSubjectRequest r) {
        DataSubjectRequestDto dto = new DataSubjectRequestDto();
        dto.setId(r.getId());
        dto.setTicketNumber(r.getTicketNumber());
        dto.setRequesterName(r.getRequesterName());
        dto.setRequesterEmail(r.getRequesterEmail());
        dto.setRequestType(r.getRequestType());
        dto.setDetails(r.getDetails());
        dto.setStatus(r.getStatus());
        dto.setAdminNotes(r.getAdminNotes());
        dto.setAssignedTo(r.getAssignedTo());
        dto.setDeadlineDate(r.getDeadlineDate());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setResolvedAt(r.getResolvedAt());
        return dto;
    }

    private ThirdPartyProcessorDto mapToProcessorDto(ThirdPartyProcessor p) {
        ThirdPartyProcessorDto dto = new ThirdPartyProcessorDto();
        dto.setId(p.getId());
        dto.setProviderName(p.getProviderName());
        dto.setPurpose(p.getPurpose());
        dto.setDataCategories(p.getDataCategories());
        dto.setProcessingLocation(p.getProcessingLocation());
        dto.setPrivacyPolicyUrl(p.getPrivacyPolicyUrl());
        dto.setDpaStatus(p.getDpaStatus());
        dto.setEnabled(p.isEnabled());
        dto.setConsentCategory(p.getConsentCategory());
        dto.setLastReviewedAt(p.getLastReviewedAt());
        return dto;
    }

    private DataBreachIncidentDto mapToBreachDto(DataBreachIncident b) {
        DataBreachIncidentDto dto = new DataBreachIncidentDto();
        dto.setId(b.getId());
        dto.setIncidentTitle(b.getIncidentTitle());
        dto.setDiscoveryDate(b.getDiscoveryDate());
        dto.setAffectedSystems(b.getAffectedSystems());
        dto.setDataCategoriesAffected(b.getDataCategoriesAffected());
        dto.setRiskLevel(b.getRiskLevel());
        dto.setContainmentMeasures(b.getContainmentMeasures());
        dto.setInvestigationSummary(b.getInvestigationSummary());
        dto.setRegulatoryNotificationRequired(b.isRegulatoryNotificationRequired());
        dto.setStatus(b.getStatus());
        dto.setCreatedAt(b.getCreatedAt());
        dto.setResolvedAt(b.getResolvedAt());
        return dto;
    }

    private LegalDocumentDto getDefaultDocument(String type) {
        LegalDocumentDto d = new LegalDocumentDto();
        d.setDocumentType(type);
        d.setVersion("1.0");
        d.setEffectiveDate(LocalDateTime.now());
        d.setStatus("PUBLISHED");

        if ("PRIVACY_POLICY".equalsIgnoreCase(type)) {
            d.setTitle("Privacy Policy");
            d.setContent("## Privacy Policy\n\nThis portfolio values and protects your privacy in accordance with the Kenya Data Protection Act, 2019 and global privacy standards.\n\n### 1. Information We Collect\nWe collect information you explicitly provide via our inquiry form (name, email address, message) and anonymized usage analytics where consented.\n\n### 2. How We Use Information\nData is solely used to communicate with you regarding software opportunities, project consultations, and professional inquiries.\n\n### 3. Your Rights\nYou have the right to request access, correction, or deletion of your personal records at any time.");
        } else {
            d.setTitle("Terms of Service");
            d.setContent("## Terms of Service\n\n### 1. Acceptance of Terms\nBy accessing and using this professional portfolio website, you agree to be bound by these terms.\n\n### 2. Intellectual Property\nAll project demonstrations, source code designs, and original portfolio content are the intellectual property of Joash Odiwuor unless otherwise noted.\n\n### 3. Professional Disclaimer\nProject demonstrations and code repositories are presented for portfolio evaluation purposes.");
        }
        return d;
    }

    private String hashIp(String ip) {
        if (ip == null || ip.isBlank()) return "anonymous";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(ip.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (Exception e) {
            return "hash-err";
        }
    }
}
