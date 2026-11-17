package com.portfolio.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SkillDto {
    private Long id;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String categoryName;

    @NotBlank(message = "Skill name is required")
    private String skillName;

    @Min(value = 0, message = "Proficiency cannot be less than 0")
    @Max(value = 100, message = "Proficiency cannot exceed 100")
    private int proficiencyPercentage = 0;

    private String proficiencyLevel;
    private String iconClass;
    private int displayOrder = 0;
    private String status = "PUBLISHED";

    public SkillDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public int getProficiencyPercentage() {
        return proficiencyPercentage;
    }

    public void setProficiencyPercentage(int proficiencyPercentage) {
        this.proficiencyPercentage = proficiencyPercentage;
    }

    public String getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(String proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public String getIconClass() {
        return iconClass;
    }

    public void setIconClass(String iconClass) {
        this.iconClass = iconClass;
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
