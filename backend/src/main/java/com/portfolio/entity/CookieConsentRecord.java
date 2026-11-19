package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cookie_consent_records")
public class CookieConsentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consent_id")
    private Long id;

    @Column(name = "consent_uuid", nullable = false, unique = true, length = 64)
    private String consentUuid;

    @Column(name = "ip_hash", length = 64)
    private String ipHash;

    @Column(name = "necessary", nullable = false)
    private boolean necessary = true;

    @Column(name = "functional", nullable = false)
    private boolean functional = false;

    @Column(name = "analytics", nullable = false)
    private boolean analytics = false;

    @Column(name = "marketing", nullable = false)
    private boolean marketing = false;

    @Column(name = "decision", nullable = false, length = 30)
    private String decision; // "ACCEPT_ALL", "REJECT_OPTIONAL", "CUSTOM"

    @Column(name = "policy_version", length = 20)
    private String policyVersion = "1.0";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    public CookieConsentRecord() {
    }

    public CookieConsentRecord(String consentUuid, String ipHash, boolean functional, boolean analytics, boolean marketing, String decision, String policyVersion) {
        this.consentUuid = consentUuid;
        this.ipHash = ipHash;
        this.necessary = true;
        this.functional = functional;
        this.analytics = analytics;
        this.marketing = marketing;
        this.decision = decision;
        this.policyVersion = policyVersion != null ? policyVersion : "1.0";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConsentUuid() {
        return consentUuid;
    }

    public void setConsentUuid(String consentUuid) {
        this.consentUuid = consentUuid;
    }

    public String getIpHash() {
        return ipHash;
    }

    public void setIpHash(String ipHash) {
        this.ipHash = ipHash;
    }

    public boolean isNecessary() {
        return necessary;
    }

    public void setNecessary(boolean necessary) {
        this.necessary = necessary;
    }

    public boolean isFunctional() {
        return functional;
    }

    public void setFunctional(boolean functional) {
        this.functional = functional;
    }

    public boolean isAnalytics() {
        return analytics;
    }

    public void setAnalytics(boolean analytics) {
        this.analytics = analytics;
    }

    public boolean isMarketing() {
        return marketing;
    }

    public void setMarketing(boolean marketing) {
        this.marketing = marketing;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(String policyVersion) {
        this.policyVersion = policyVersion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getWithdrawnAt() {
        return withdrawnAt;
    }

    public void setWithdrawnAt(LocalDateTime withdrawnAt) {
        this.withdrawnAt = withdrawnAt;
    }
}
