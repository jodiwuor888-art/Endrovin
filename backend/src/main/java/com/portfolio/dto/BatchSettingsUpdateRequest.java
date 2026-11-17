package com.portfolio.dto;

import java.util.Map;

public class BatchSettingsUpdateRequest {

    private Map<String, String> settings;

    public BatchSettingsUpdateRequest() {
    }

    public BatchSettingsUpdateRequest(Map<String, String> settings) {
        this.settings = settings;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }
}
