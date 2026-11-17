package com.portfolio.dto;

import jakarta.validation.constraints.*;

public class PublicTestimonialSubmissionRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Size(max = 150, message = "Organization cannot exceed 150 characters")
    private String organization;

    @Size(max = 100, message = "Position cannot exceed 100 characters")
    private String position;

    @NotBlank(message = "Professional relationship is required")
    @Pattern(
        regexp = "Client|Employer|Colleague|Lecturer|Mentor|Project collaborator|Business partner|Other",
        message = "Relationship must be one of: Client, Employer, Colleague, Lecturer, Mentor, Project collaborator, Business partner, Other"
    )
    private String relationship = "Client";

    @NotBlank(message = "Recommendation message is required")
    @Size(min = 20, max = 2000, message = "Recommendation message must be between 20 and 2000 characters")
    private String message;

    @Size(max = 500, message = "Photo URL cannot exceed 500 characters")
    private String photoUrl;

    @Size(max = 255, message = "Website cannot exceed 255 characters")
    private String organizationWebsite;

    @AssertTrue(message = "Consent to publish recommendation is required")
    private boolean consentToPublish;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private int rating = 5;

    // Anti-spam math challenge
    private String captchaToken;
    private String captchaAnswer;

    // Honeypot field (must be left empty by legitimate users)
    private String website;

    public PublicTestimonialSubmissionRequest() {
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
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

    public String getCaptchaToken() {
        return captchaToken;
    }

    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }

    public String getCaptchaAnswer() {
        return captchaAnswer;
    }

    public void setCaptchaAnswer(String captchaAnswer) {
        this.captchaAnswer = captchaAnswer;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
