package com.portfolio.dto;

public class CookieConsentDto {
    private String consentUuid;
    private boolean necessary = true;
    private boolean functional;
    private boolean analytics;
    private boolean marketing;
    private String decision; // ACCEPT_ALL, REJECT_OPTIONAL, CUSTOM
    private String policyVersion;

    public CookieConsentDto() {}

    public String getConsentUuid() { return consentUuid; }
    public void setConsentUuid(String consentUuid) { this.consentUuid = consentUuid; }
    public boolean isNecessary() { return necessary; }
    public void setNecessary(boolean necessary) { this.necessary = necessary; }
    public boolean isFunctional() { return functional; }
    public void setFunctional(boolean functional) { this.functional = functional; }
    public boolean isAnalytics() { return analytics; }
    public void setAnalytics(boolean analytics) { this.analytics = analytics; }
    public boolean isMarketing() { return marketing; }
    public void setMarketing(boolean marketing) { this.marketing = marketing; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(String policyVersion) { this.policyVersion = policyVersion; }
}
