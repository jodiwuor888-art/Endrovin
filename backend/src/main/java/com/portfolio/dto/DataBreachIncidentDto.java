package com.portfolio.dto;

import java.time.LocalDateTime;

public class DataBreachIncidentDto {
    private Long id;
    private String incidentTitle;
    private LocalDateTime discoveryDate;
    private String affectedSystems;
    private String dataCategoriesAffected;
    private String riskLevel;
    private String containmentMeasures;
    private String investigationSummary;
    private boolean regulatoryNotificationRequired;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public DataBreachIncidentDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIncidentTitle() { return incidentTitle; }
    public void setIncidentTitle(String incidentTitle) { this.incidentTitle = incidentTitle; }
    public LocalDateTime getDiscoveryDate() { return discoveryDate; }
    public void setDiscoveryDate(LocalDateTime discoveryDate) { this.discoveryDate = discoveryDate; }
    public String getAffectedSystems() { return affectedSystems; }
    public void setAffectedSystems(String affectedSystems) { this.affectedSystems = affectedSystems; }
    public String getDataCategoriesAffected() { return dataCategoriesAffected; }
    public void setDataCategoriesAffected(String dataCategoriesAffected) { this.dataCategoriesAffected = dataCategoriesAffected; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getContainmentMeasures() { return containmentMeasures; }
    public void setContainmentMeasures(String containmentMeasures) { this.containmentMeasures = containmentMeasures; }
    public String getInvestigationSummary() { return investigationSummary; }
    public void setInvestigationSummary(String investigationSummary) { this.investigationSummary = investigationSummary; }
    public boolean isRegulatoryNotificationRequired() { return regulatoryNotificationRequired; }
    public void setRegulatoryNotificationRequired(boolean regulatoryNotificationRequired) { this.regulatoryNotificationRequired = regulatoryNotificationRequired; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
