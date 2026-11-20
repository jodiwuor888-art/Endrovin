package com.portfolio.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsSummaryResponse {
    private long totalPageViews;
    private long uniqueVisitors;
    private long projectViews;
    private long serviceViews;
    private long contactFormSubmissions;
    private long cvDownloads;
    private List<Map<String, Object>> topPages;
    private List<Map<String, Object>> topReferrers;
    private List<Map<String, Object>> deviceDistribution;
    private String period; // "today", "yesterday", "7d", "30d", "90d"

    public AnalyticsSummaryResponse() {}

    public long getTotalPageViews() { return totalPageViews; }
    public void setTotalPageViews(long totalPageViews) { this.totalPageViews = totalPageViews; }
    public long getUniqueVisitors() { return uniqueVisitors; }
    public void setUniqueVisitors(long uniqueVisitors) { this.uniqueVisitors = uniqueVisitors; }
    public long getProjectViews() { return projectViews; }
    public void setProjectViews(long projectViews) { this.projectViews = projectViews; }
    public long getServiceViews() { return serviceViews; }
    public void setServiceViews(long serviceViews) { this.serviceViews = serviceViews; }
    public long getContactFormSubmissions() { return contactFormSubmissions; }
    public void setContactFormSubmissions(long contactFormSubmissions) { this.contactFormSubmissions = contactFormSubmissions; }
    public long getCvDownloads() { return cvDownloads; }
    public void setCvDownloads(long cvDownloads) { this.cvDownloads = cvDownloads; }
    public List<Map<String, Object>> getTopPages() { return topPages; }
    public void setTopPages(List<Map<String, Object>> topPages) { this.topPages = topPages; }
    public List<Map<String, Object>> getTopReferrers() { return topReferrers; }
    public void setTopReferrers(List<Map<String, Object>> topReferrers) { this.topReferrers = topReferrers; }
    public List<Map<String, Object>> getDeviceDistribution() { return deviceDistribution; }
    public void setDeviceDistribution(List<Map<String, Object>> deviceDistribution) { this.deviceDistribution = deviceDistribution; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
}
