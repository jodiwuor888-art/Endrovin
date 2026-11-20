package com.portfolio.dto;

import java.util.List;
import java.util.Map;

public class AiAssistRequest {
    private String promptType; // "ENHANCE_PROJECT", "GENERATE_SEO", "CLASSIFY_INQUIRY", "PORTFOLIO_AUDIT"
    private String title;
    private String currentText;
    private String category;
    private String technologies;
    private Map<String, Object> metadata;

    public AiAssistRequest() {}

    public String getPromptType() { return promptType; }
    public void setPromptType(String promptType) { this.promptType = promptType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCurrentText() { return currentText; }
    public void setCurrentText(String currentText) { this.currentText = currentText; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTechnologies() { return technologies; }
    public void setTechnologies(String technologies) { this.technologies = technologies; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
