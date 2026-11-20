package com.portfolio.dto;

import java.time.LocalDateTime;

public class LegalDocumentDto {
    private Long id;
    private String documentType;
    private String title;
    private String version;
    private LocalDateTime effectiveDate;
    private String content;
    private String status;
    private String createdBy;
    private LocalDateTime updatedAt;

    public LegalDocumentDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public LocalDateTime getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDateTime effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
