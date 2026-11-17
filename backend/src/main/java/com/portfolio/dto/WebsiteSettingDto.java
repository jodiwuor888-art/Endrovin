package com.portfolio.dto;

import jakarta.validation.constraints.NotBlank;

public class WebsiteSettingDto {

    @NotBlank(message = "Setting key is required")
    private String settingKey;

    private String settingValue;

    @NotBlank(message = "Setting group is required")
    private String settingGroup;

    private String description;

    public WebsiteSettingDto() {
    }

    public WebsiteSettingDto(String settingKey, String settingValue, String settingGroup, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.settingGroup = settingGroup;
        this.description = description;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getSettingGroup() {
        return settingGroup;
    }

    public void setSettingGroup(String settingGroup) {
        this.settingGroup = settingGroup;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
