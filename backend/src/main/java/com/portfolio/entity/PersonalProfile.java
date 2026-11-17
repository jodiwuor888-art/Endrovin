package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PersonalProfile")
public class PersonalProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProfileID")
    private Long id;

    @Column(name = "FullName", nullable = false, length = 100)
    private String fullName;

    @Column(name = "ProfessionalHeadline", nullable = false, length = 200)
    private String professionalHeadline;

    @Column(name = "ProfessionalTitle", nullable = false, length = 150)
    private String professionalTitle;

    @Lob
    @Column(name = "BioSummary")
    private String bioSummary;

    @Column(name = "Location", nullable = false, length = 100)
    private String location;

    @Column(name = "Email", nullable = false, length = 120)
    private String email;

    @Column(name = "Phone", length = 50)
    private String phone;

    @Column(name = "GitHubUrl", length = 255)
    private String gitHubUrl;

    @Column(name = "LinkedInUrl", length = 255)
    private String linkedInUrl;

    @Column(name = "FacebookUrl", length = 255)
    private String facebookUrl;

    @Column(name = "TwitterUrl", length = 255)
    private String twitterUrl;

    @Column(name = "ProfilePhotoUrl", length = 500)
    private String profilePhotoUrl;

    @Column(name = "CvFileUrl", length = 500)
    private String cvFileUrl;

    @Column(name = "IsActive", nullable = false)
    private boolean active = true;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PersonalProfile() {
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public String getProfessionalHeadline() {
        return professionalHeadline;
    }

    public void setProfessionalHeadline(String professionalHeadline) {
        this.professionalHeadline = professionalHeadline;
    }

    public String getProfessionalTitle() {
        return professionalTitle;
    }

    public void setProfessionalTitle(String professionalTitle) {
        this.professionalTitle = professionalTitle;
    }

    public String getBioSummary() {
        return bioSummary;
    }

    public void setBioSummary(String bioSummary) {
        this.bioSummary = bioSummary;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGitHubUrl() {
        return gitHubUrl;
    }

    public void setGitHubUrl(String gitHubUrl) {
        this.gitHubUrl = gitHubUrl;
    }

    public String getLinkedInUrl() {
        return linkedInUrl;
    }

    public void setLinkedInUrl(String linkedInUrl) {
        this.linkedInUrl = linkedInUrl;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getTwitterUrl() {
        return twitterUrl;
    }

    public void setTwitterUrl(String twitterUrl) {
        this.twitterUrl = twitterUrl;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getCvFileUrl() {
        return cvFileUrl;
    }

    public void setCvFileUrl(String cvFileUrl) {
        this.cvFileUrl = cvFileUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
}
