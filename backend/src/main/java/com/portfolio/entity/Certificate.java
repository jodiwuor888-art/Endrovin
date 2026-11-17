package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CertificateID")
    private Long id;

    @Column(name = "CertificateTitle", nullable = false, length = 200)
    private String certificateTitle;

    @Column(name = "IssuingOrganization", nullable = false, length = 200)
    private String issuingOrganization;

    @Column(name = "IssueDate", nullable = false, length = 50)
    private String issueDate;

    @Column(name = "ExpirationDate", length = 50)
    private String expirationDate;

    @Column(name = "CredentialId", length = 100)
    private String credentialId;

    @Column(name = "CredentialUrl", length = 500)
    private String credentialUrl;

    @Column(name = "CertificateFileUrl", length = 500)
    private String certificateFileUrl;

    @Column(name = "DisplayOrder", nullable = false)
    private int displayOrder = 0;

    @Column(name = "Status", nullable = false, length = 20)
    private String status = "PUBLISHED";

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Certificate() {
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCertificateTitle() {
        return certificateTitle;
    }

    public void setCertificateTitle(String certificateTitle) {
        this.certificateTitle = certificateTitle;
    }

    public String getIssuingOrganization() {
        return issuingOrganization;
    }

    public void setIssuingOrganization(String issuingOrganization) {
        this.issuingOrganization = issuingOrganization;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getCredentialUrl() {
        return credentialUrl;
    }

    public void setCredentialUrl(String credentialUrl) {
        this.credentialUrl = credentialUrl;
    }

    public String getCertificateFileUrl() {
        return certificateFileUrl;
    }

    public void setCertificateFileUrl(String certificateFileUrl) {
        this.certificateFileUrl = certificateFileUrl;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
