package com.portfolio.dto;

import java.time.LocalDateTime;

public class PublicTestimonialResponse {
    private Long id;
    private String fullName;
    private String organization;
    private String position;
    private String relationship;
    private String message;
    private String photoUrl;
    private String organizationWebsite;
    private int rating;
    private LocalDateTime submittedAt;

    public PublicTestimonialResponse() {
    }

    public PublicTestimonialResponse(Long id, String fullName, String organization, String position,
                                     String relationship, String message, String photoUrl,
                                     String organizationWebsite, int rating, LocalDateTime submittedAt) {
        this.id = id;
        this.fullName = fullName;
        this.organization = organization;
        this.position = position;
        this.relationship = relationship;
        this.message = message;
        this.photoUrl = photoUrl;
        this.organizationWebsite = organizationWebsite;
        this.rating = rating;
        this.submittedAt = submittedAt;
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    // Compatibility getters
    public String getClientName() {
        return fullName;
    }

    public String getClientTitle() {
        return position;
    }

    public String getClientCompany() {
        return organization;
    }

    public String getClientAvatarUrl() {
        return photoUrl;
    }

    public String getTestimonialText() {
        return message;
    }
}
