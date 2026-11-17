package com.portfolio.dto;

import jakarta.validation.constraints.NotBlank;

public class CertificateDto {
    private Long id;

    @NotBlank(message = "Certificate title is required")
    private String certificateTitle;

    @NotBlank(message = "Issuing organization is required")
    private String issuingOrganization;

    @NotBlank(message = "Issue date is required")
    private String issueDate;

    private String expirationDate;
    private String credentialId;
    private String credentialUrl;
    private String certificateFileUrl;
    private int displayOrder = 0;
    private String status = "PUBLISHED";

    public CertificateDto() {
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
}
