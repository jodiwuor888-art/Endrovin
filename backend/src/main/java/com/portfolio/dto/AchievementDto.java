package com.portfolio.dto;

import jakarta.validation.constraints.NotBlank;

public class AchievementDto {
    private Long id;

    @NotBlank(message = "Achievement title is required")
    private String title;

    private String organization;
    private String achievementDate;

    @NotBlank(message = "Description is required")
    private String description;

    private int displayOrder = 0;
    private String status = "PUBLISHED";

    public AchievementDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getAchievementDate() {
        return achievementDate;
    }

    public void setAchievementDate(String achievementDate) {
        this.achievementDate = achievementDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
