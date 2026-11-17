package com.portfolio.dto;

import jakarta.validation.constraints.NotBlank;

public class AboutInformationDto {
    private Long id;

    @NotBlank(message = "Section title is required")
    private String sectionTitle;

    private String subtitle;

    @NotBlank(message = "Paragraph 1 is required")
    private String paragraph1;

    private String paragraph2;
    private String paragraph3;
    private int yearsExperience = 0;
    private int projectsCompleted = 0;
    private int satisfiedClients = 0;
    private String status = "PUBLISHED";

    public AboutInformationDto() {
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
}
