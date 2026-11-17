package com.portfolio.dto;

import jakarta.validation.constraints.NotBlank;

public class SocialLinkDto {
    private Long id;

    @NotBlank(message = "Platform name is required")
    private String platformName;

    @NotBlank(message = "URL is required")
    private String url;

    @NotBlank(message = "Icon class is required")
    private String iconClass;

    private int displayOrder = 0;
    private boolean active = true;

    public SocialLinkDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
