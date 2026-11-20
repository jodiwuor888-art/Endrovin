package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_breach_incidents")
public class DataBreachIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incident_id")
    private Long id;

    @Column(name = "incident_title", nullable = false, length = 200)
    private String incidentTitle;

    @Column(name = "discovery_date", nullable = false)
    private LocalDateTime discoveryDate = LocalDateTime.now();

    @Column(name = "affected_systems", nullable = false, length = 255)
    private String affectedSystems;

    @Column(name = "data_categories_affected", nullable = false, length = 255)
    private String dataCategoriesAffected;

    @Column(name = "risk_level", nullable = false, length = 30)
    private String riskLevel = "LOW"; // "LOW", "MEDIUM", "HIGH", "CRITICAL"

    @Lob
    @Column(name = "containment_measures", columnDefinition = "LONGTEXT")
    private String containmentMeasures;

    @Lob
    @Column(name = "investigation_summary", columnDefinition = "LONGTEXT")
    private String investigationSummary;

    @Column(name = "regulatory_notification_required", nullable = false)
    private boolean regulatoryNotificationRequired = false;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "RESOLVED"; // "OPEN", "INVESTIGATING", "CONTAINED", "RESOLVED"

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public DataBreachIncident() {
    }

    public DataBreachIncident(String incidentTitle, LocalDateTime discoveryDate, String affectedSystems, String dataCategoriesAffected, String riskLevel, String containmentMeasures, String investigationSummary, boolean regulatoryNotificationRequired, String status) {
        this.incidentTitle = incidentTitle;
        this.discoveryDate = discoveryDate != null ? discoveryDate : LocalDateTime.now();
        this.affectedSystems = affectedSystems;
        this.dataCategoriesAffected = dataCategoriesAffected;
        this.riskLevel = riskLevel;
        this.containmentMeasures = containmentMeasures;
        this.investigationSummary = investigationSummary;
        this.regulatoryNotificationRequired = regulatoryNotificationRequired;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIncidentTitle() {
        return incidentTitle;
    }

    public void setIncidentTitle(String incidentTitle) {
        this.incidentTitle = incidentTitle;
    }

    public LocalDateTime getDiscoveryDate() {
        return discoveryDate;
    }

    public void setDiscoveryDate(LocalDateTime discoveryDate) {
        this.discoveryDate = discoveryDate;
    }

    public String getAffectedSystems() {
        return affectedSystems;
    }

    public void setAffectedSystems(String affectedSystems) {
        this.affectedSystems = affectedSystems;
    }

    public String getDataCategoriesAffected() {
        return dataCategoriesAffected;
    }

    public void setDataCategoriesAffected(String dataCategoriesAffected) {
        this.dataCategoriesAffected = dataCategoriesAffected;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getContainmentMeasures() {
        return containmentMeasures;
    }

    public void setContainmentMeasures(String containmentMeasures) {
        this.containmentMeasures = containmentMeasures;
    }

    public String getInvestigationSummary() {
        return investigationSummary;
    }

    public void setInvestigationSummary(String investigationSummary) {
        this.investigationSummary = investigationSummary;
    }

    public boolean isRegulatoryNotificationRequired() {
        return regulatoryNotificationRequired;
    }

    public void setRegulatoryNotificationRequired(boolean regulatoryNotificationRequired) {
        this.regulatoryNotificationRequired = regulatoryNotificationRequired;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
