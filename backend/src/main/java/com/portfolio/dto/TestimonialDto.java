package com.portfolio.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class TestimonialDto {
    private Long id;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String email;
    private String organization;
    private String position;
    private String relationship = "Client";

    @NotBlank(message = "Recommendation message is required")
    private String message;

    private String photoPath;
    private String organizationWebsite;
    private boolean consentToPublish = true;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private int rating = 5;

    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, ARCHIVED
    private int displayOrder = 0;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedByAdminId;
    private String rejectionReason;
    private LocalDateTime createdAt;

    public TestimonialDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getOrganizationWebsite() {
        return organizationWebsite;
    }

    public void setOrganizationWebsite(String organizationWebsite) {
        this.organizationWebsite = organizationWebsite;
    }

    public boolean isConsentToPublish() {
        return consentToPublish;
    }

    public void setConsentToPublish(boolean consentToPublish) {
        this.consentToPublish = consentToPublish;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public Long getReviewedByAdminId() {
        return reviewedByAdminId;
    }

    public void setReviewedByAdminId(Long reviewedByAdminId) {
        this.reviewedByAdminId = reviewedByAdminId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Backward-compatible accessors
    public String getClientName() {
        return fullName;
    }

    public void setClientName(String clientName) {
        this.fullName = clientName;
    }

    public String getClientTitle() {
        return position;
    }

    public void setClientTitle(String clientTitle) {
        this.position = clientTitle;
    }

    public String getClientCompany() {
        return organization;
    }

    public void setClientCompany(String clientCompany) {
        this.organization = clientCompany;
    }

    public String getClientAvatarUrl() {
        return photoPath;
    }

    public void setClientAvatarUrl(String clientAvatarUrl) {
        this.photoPath = clientAvatarUrl;
    }

    public String getTestimonialText() {
        return message;
    }

    public void setTestimonialText(String testimonialText) {
        this.message = testimonialText;
    }

    public String getApprovalStatus() {
        return status;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.status = approvalStatus;
    }
}
