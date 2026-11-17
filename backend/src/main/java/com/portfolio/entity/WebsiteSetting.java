package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "WebsiteSettings")
public class WebsiteSetting {

    @Id
    @Column(name = "SettingKey", length = 100)
    private String settingKey;

    @Lob
    @Column(name = "SettingValue")
    private String settingValue;

    @Column(name = "SettingGroup", nullable = false, length = 50)
    private String settingGroup;

    @Column(name = "Description", length = 255)
    private String description;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public WebsiteSetting() {
    }

    public WebsiteSetting(String settingKey, String settingValue, String settingGroup, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.settingGroup = settingGroup;
        this.description = description;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
