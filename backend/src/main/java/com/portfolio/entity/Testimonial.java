package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Testimonials")
public class Testimonial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TestimonialID")
    private Long id;

    @Column(name = "FullName", nullable = false, length = 100)
    private String fullName;

    @Column(name = "Email", nullable = false, length = 150)
    private String email;

    @Column(name = "Organization", length = 150)
    private String organization;

    @Column(name = "Position", length = 100)
    private String position;

    @Column(name = "Relationship", nullable = false, length = 50)
    private String relationship = "Client"; // Client, Employer, Colleague, Lecturer, Mentor, Project collaborator, Business partner, Other

    @Lob
    @Column(name = "Message", nullable = false)
    private String message;

    @Column(name = "PhotoPath", length = 500)
    private String photoPath;

    @Column(name = "OrganizationWebsite", length = 255)
    private String organizationWebsite;

    @Column(name = "ConsentToPublish", nullable = false)
    private boolean consentToPublish = true;

    @Column(name = "Status", nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, ARCHIVED

    @Column(name = "Rating", nullable = false)
    private int rating = 5;

    @Column(name = "DisplayOrder", nullable = false)
    private int displayOrder = 0;

    @Column(name = "SubmittedAt", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "ReviewedAt")
    private LocalDateTime reviewedAt;

    @Column(name = "ReviewedByAdminID")
    private Long reviewedByAdminId;

    @Column(name = "RejectionReason", length = 255)
    private String rejectionReason;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Testimonial() {
    }

    @PrePersist
    public void prePersist() {
        if (this.submittedAt == null) this.submittedAt = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Standard Getters & Setters
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Backward-Compatibility Aliases
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
