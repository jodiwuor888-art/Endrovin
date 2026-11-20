package com.portfolio.dto;

import java.time.LocalDateTime;

public class ThirdPartyProcessorDto {
    private Long id;
    private String providerName;
    private String purpose;
    private String dataCategories;
    private String processingLocation;
    private String privacyPolicyUrl;
    private String dpaStatus;
    private boolean enabled;
    private String consentCategory;
    private LocalDateTime lastReviewedAt;

    public ThirdPartyProcessorDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getDataCategories() { return dataCategories; }
    public void setDataCategories(String dataCategories) { this.dataCategories = dataCategories; }
    public String getProcessingLocation() { return processingLocation; }
    public void setProcessingLocation(String processingLocation) { this.processingLocation = processingLocation; }
    public String getPrivacyPolicyUrl() { return privacyPolicyUrl; }
    public void setPrivacyPolicyUrl(String privacyPolicyUrl) { this.privacyPolicyUrl = privacyPolicyUrl; }
    public String getDpaStatus() { return dpaStatus; }
    public void setDpaStatus(String dpaStatus) { this.dpaStatus = dpaStatus; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getConsentCategory() { return consentCategory; }
    public void setConsentCategory(String consentCategory) { this.consentCategory = consentCategory; }
    public LocalDateTime getLastReviewedAt() { return lastReviewedAt; }
    public void setLastReviewedAt(LocalDateTime lastReviewedAt) { this.lastReviewedAt = lastReviewedAt; }
}
