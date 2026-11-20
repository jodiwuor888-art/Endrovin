package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "third_party_processors")
public class ThirdPartyProcessor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "processor_id")
    private Long id;

    @Column(name = "provider_name", nullable = false, length = 100)
    private String providerName; // e.g. "Render Cloud", "AWS RDS", "Vercel Inc.", "Cloudflare"

    @Column(name = "purpose", nullable = false, length = 200)
    private String purpose; // e.g. "Backend API hosting & deployment"

    @Column(name = "data_categories", nullable = false, length = 250)
    private String dataCategories; // e.g. "IP address, request logs, database storage"

    @Column(name = "processing_location", length = 100)
    private String processingLocation; // e.g. "United States / Frankfurt (EU)"

    @Column(name = "privacy_policy_url", length = 500)
    private String privacyPolicyUrl;

    @Column(name = "dpa_status", length = 50)
    private String dpaStatus = "SIGNED"; // "SIGNED", "STANDARD_CONTRACTUAL_CLAUSES", "EXEMPT"

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "consent_category", length = 50)
    private String consentCategory = "NECESSARY"; // "NECESSARY", "FUNCTIONAL", "ANALYTICS", "MARKETING"

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt = LocalDateTime.now();

    public ThirdPartyProcessor() {
    }

    public ThirdPartyProcessor(String providerName, String purpose, String dataCategories, String processingLocation, String privacyPolicyUrl, String dpaStatus, boolean enabled, String consentCategory) {
        this.providerName = providerName;
        this.purpose = purpose;
        this.dataCategories = dataCategories;
        this.processingLocation = processingLocation;
        this.privacyPolicyUrl = privacyPolicyUrl;
        this.dpaStatus = dpaStatus;
        this.enabled = enabled;
        this.consentCategory = consentCategory;
        this.lastReviewedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDataCategories() {
        return dataCategories;
    }

    public void setDataCategories(String dataCategories) {
        this.dataCategories = dataCategories;
    }

    public String getProcessingLocation() {
        return processingLocation;
    }

    public void setProcessingLocation(String processingLocation) {
        this.processingLocation = processingLocation;
    }

    public String getPrivacyPolicyUrl() {
        return privacyPolicyUrl;
    }

    public void setPrivacyPolicyUrl(String privacyPolicyUrl) {
        this.privacyPolicyUrl = privacyPolicyUrl;
    }

    public String getDpaStatus() {
        return dpaStatus;
    }

    public void setDpaStatus(String dpaStatus) {
        this.dpaStatus = dpaStatus;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getConsentCategory() {
        return consentCategory;
    }

    public void setConsentCategory(String consentCategory) {
        this.consentCategory = consentCategory;
    }

    public LocalDateTime getLastReviewedAt() {
        return lastReviewedAt;
    }

    public void setLastReviewedAt(LocalDateTime lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }
}
