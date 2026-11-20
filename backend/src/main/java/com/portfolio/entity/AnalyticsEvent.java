package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_events", indexes = {
    @Index(name = "idx_analytics_event_type", columnList = "event_type"),
    @Index(name = "idx_analytics_timestamp", columnList = "timestamp")
})
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType; // "page_view", "project_view", "service_view", "contact_form_start", "contact_form_submit", "resume_download", "github_click", "whatsapp_click", "recommendation_submit"

    @Column(name = "entity_id", length = 50)
    private String entityId;

    @Column(name = "page_url", length = 500)
    private String pageUrl;

    @Column(name = "referrer", length = 500)
    private String referrer;

    @Column(name = "visitor_id", length = 64)
    private String visitorId;

    @Column(name = "device_type", length = 30)
    private String deviceType; // "DESKTOP", "MOBILE", "TABLET"

    @Column(name = "browser", length = 50)
    private String browser;

    @Column(name = "os", length = 50)
    private String os;

    @Column(name = "country", length = 50)
    private String country = "Unknown";

    @Column(name = "anonymized_ip", length = 50)
    private String anonymizedIp;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public AnalyticsEvent() {
    }

    public AnalyticsEvent(String eventType, String entityId, String pageUrl, String referrer, String visitorId, String deviceType, String browser, String os, String country, String anonymizedIp) {
        this.eventType = eventType;
        this.entityId = entityId;
        this.pageUrl = pageUrl;
        this.referrer = referrer;
        this.visitorId = visitorId;
        this.deviceType = deviceType;
        this.browser = browser;
        this.os = os;
        this.country = country != null ? country : "Unknown";
        this.anonymizedIp = anonymizedIp;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAnonymizedIp() {
        return anonymizedIp;
    }

    public void setAnonymizedIp(String anonymizedIp) {
        this.anonymizedIp = anonymizedIp;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
