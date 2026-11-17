package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AboutInformation")
public class AboutInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AboutID")
    private Long id;

    @Column(name = "SectionTitle", nullable = false, length = 150)
    private String sectionTitle;

    @Column(name = "Subtitle", length = 200)
    private String subtitle;

    @Lob
    @Column(name = "Paragraph1", nullable = false)
    private String paragraph1;

    @Lob
    @Column(name = "Paragraph2")
    private String paragraph2;

    @Lob
    @Column(name = "Paragraph3")
    private String paragraph3;

    @Column(name = "YearsExperience", nullable = false)
    private int yearsExperience = 0;

    @Column(name = "ProjectsCompleted", nullable = false)
    private int projectsCompleted = 0;

    @Column(name = "SatisfiedClients", nullable = false)
    private int satisfiedClients = 0;

    @Column(name = "Status", nullable = false, length = 20)
    private String status = "PUBLISHED";

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public AboutInformation() {
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

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getParagraph1() {
        return paragraph1;
    }

    public void setParagraph1(String paragraph1) {
        this.paragraph1 = paragraph1;
    }

    public String getParagraph2() {
        return paragraph2;
    }

    public void setParagraph2(String paragraph2) {
        this.paragraph2 = paragraph2;
    }

    public String getParagraph3() {
        return paragraph3;
    }

    public void setParagraph3(String paragraph3) {
        this.paragraph3 = paragraph3;
    }

    public int getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(int yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public int getProjectsCompleted() {
        return projectsCompleted;
    }

    public void setProjectsCompleted(int projectsCompleted) {
        this.projectsCompleted = projectsCompleted;
    }

    public int getSatisfiedClients() {
        return satisfiedClients;
    }

    public void setSatisfiedClients(int satisfiedClients) {
        this.satisfiedClients = satisfiedClients;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
