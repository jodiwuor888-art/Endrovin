package com.portfolio.dto;

public class DashboardStatsResponse {
    private long totalProjects = 0;
    private long publishedProjects = 0;
    private long totalSkills = 0;
    private long totalServices = 0;
    private long totalEducation = 0;
    private long totalExperience = 0;
    private long totalCertificates = 0;
    private long totalAchievements = 0;
    private long totalTestimonials = 0;
    private long pendingTestimonials = 0;
    private long totalMessages = 0;
    private long unreadMessages = 0;
    private boolean profileConfigured = false;
    private boolean cvUploaded = false;

    public DashboardStatsResponse() {
    }

    public long getTotalProjects() {
        return totalProjects;
    }

    public void setTotalProjects(long totalProjects) {
        this.totalProjects = totalProjects;
    }

    public long getPublishedProjects() {
        return publishedProjects;
    }

    public void setPublishedProjects(long publishedProjects) {
        this.publishedProjects = publishedProjects;
    }

    public long getTotalSkills() {
        return totalSkills;
    }

    public void setTotalSkills(long totalSkills) {
        this.totalSkills = totalSkills;
    }

    public long getTotalServices() {
        return totalServices;
    }

    public void setTotalServices(long totalServices) {
        this.totalServices = totalServices;
    }

    public long getTotalEducation() {
        return totalEducation;
    }

    public void setTotalEducation(long totalEducation) {
        this.totalEducation = totalEducation;
    }

    public long getTotalExperience() {
        return totalExperience;
    }

    public void setTotalExperience(long totalExperience) {
        this.totalExperience = totalExperience;
    }

    public long getTotalCertificates() {
        return totalCertificates;
    }

    public void setTotalCertificates(long totalCertificates) {
        this.totalCertificates = totalCertificates;
    }

    public long getTotalAchievements() {
        return totalAchievements;
    }

    public void setTotalAchievements(long totalAchievements) {
        this.totalAchievements = totalAchievements;
    }

    public long getTotalTestimonials() {
        return totalTestimonials;
    }

    public void setTotalTestimonials(long totalTestimonials) {
        this.totalTestimonials = totalTestimonials;
    }

    public long getPendingTestimonials() {
        return pendingTestimonials;
    }

    public void setPendingTestimonials(long pendingTestimonials) {
        this.pendingTestimonials = pendingTestimonials;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public long getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(long unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public boolean isProfileConfigured() {
        return profileConfigured;
    }

    public void setProfileConfigured(boolean profileConfigured) {
        this.profileConfigured = profileConfigured;
    }

    public boolean isCvUploaded() {
        return cvUploaded;
    }

    public void setCvUploaded(boolean cvUploaded) {
        this.cvUploaded = cvUploaded;
    }
}
