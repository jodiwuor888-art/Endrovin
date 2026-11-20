package com.portfolio.dto;

import java.util.List;
import java.util.Map;

public class AiAssistResponse {
    private String suggestedTitle;
    private String enhancedContent;
    private String suggestedSeoTitle;
    private String suggestedSeoDescription;
    private List<String> suggestedTags;
    private String inquiryClassification;
    private String inquirySentiment;
    private List<String> recommendations;

    public AiAssistResponse() {}

    public String getSuggestedTitle() { return suggestedTitle; }
    public void setSuggestedTitle(String suggestedTitle) { this.suggestedTitle = suggestedTitle; }
    public String getEnhancedContent() { return enhancedContent; }
    public void setEnhancedContent(String enhancedContent) { this.enhancedContent = enhancedContent; }
    public String getSuggestedSeoTitle() { return suggestedSeoTitle; }
    public void setSuggestedSeoTitle(String suggestedSeoTitle) { this.suggestedSeoTitle = suggestedSeoTitle; }
    public String getSuggestedSeoDescription() { return suggestedSeoDescription; }
    public void setSuggestedSeoDescription(String suggestedSeoDescription) { this.suggestedSeoDescription = suggestedSeoDescription; }
    public List<String> getSuggestedTags() { return suggestedTags; }
    public void setSuggestedTags(List<String> suggestedTags) { this.suggestedTags = suggestedTags; }
    public String getInquiryClassification() { return inquiryClassification; }
    public void setInquiryClassification(String inquiryClassification) { this.inquiryClassification = inquiryClassification; }
    public String getInquirySentiment() { return inquirySentiment; }
    public void setInquirySentiment(String inquirySentiment) { this.inquirySentiment = inquirySentiment; }
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
}
