package com.portfolio.service;

import com.portfolio.dto.AiAssistRequest;
import com.portfolio.dto.AiAssistResponse;
import com.portfolio.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AiAssistantService {

    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final ServiceRepository serviceRepository;
    private final TestimonialRepository testimonialRepository;
    private final PersonalProfileRepository profileRepository;

    public AiAssistantService(ProjectRepository projectRepository,
                             SkillRepository skillRepository,
                             ServiceRepository serviceRepository,
                             TestimonialRepository testimonialRepository,
                             PersonalProfileRepository profileRepository) {
        this.projectRepository = projectRepository;
        this.skillRepository = skillRepository;
        this.serviceRepository = serviceRepository;
        this.testimonialRepository = testimonialRepository;
        this.profileRepository = profileRepository;
    }

    public AiAssistResponse processAiRequest(AiAssistRequest req) {
        AiAssistResponse res = new AiAssistResponse();
        if (req == null || req.getPromptType() == null) return res;

        String type = req.getPromptType().toUpperCase();
        switch (type) {
            case "ENHANCE_PROJECT" -> enhanceProject(req, res);
            case "GENERATE_SEO" -> generateSeo(req, res);
            case "CLASSIFY_INQUIRY" -> classifyInquiry(req, res);
            case "PORTFOLIO_AUDIT" -> auditPortfolio(res);
            default -> res.setEnhancedContent("Unsupported assistant request type.");
        }
        return res;
    }

    private void enhanceProject(AiAssistRequest req, AiAssistResponse res) {
        String title = req.getTitle() != null ? req.getTitle() : "Software Project";
        String tech = req.getTechnologies() != null ? req.getTechnologies() : "Java, Spring Boot, MySQL";
        String current = req.getCurrentText() != null ? req.getCurrentText() : "";

        res.setSuggestedTitle(title);
        res.setEnhancedContent(
                "An enterprise-ready, high-performance " + title + " engineered with " + tech +
                ". Features secure modular architecture, optimized database queries, robust REST API contracts, and an intuitive responsive user interface. " +
                (current.isBlank() ? "" : " " + current.trim())
        );
        res.setSuggestedTags(Arrays.asList("FullStack", "Java", "SpringBoot", "APIs", "DatabaseDesign", "SecureCode"));
    }

    private void generateSeo(AiAssistRequest req, AiAssistResponse res) {
        String title = req.getTitle() != null ? req.getTitle() : "Joash Odiwuor Portfolio";
        String cat = req.getCategory() != null ? req.getCategory() : "Software Development";
        String tech = req.getTechnologies() != null ? req.getTechnologies() : "Java & Modern Web";

        res.setSuggestedSeoTitle(title + " | " + cat + " by Joash Odiwuor");
        res.setSuggestedSeoDescription(
                "Explore " + title + ", a professional " + cat + " engineered using " + tech +
                ". Built with clean architecture, enterprise security, and resilient performance."
        );
    }

    private void classifyInquiry(AiAssistRequest req, AiAssistResponse res) {
        String text = (req.getCurrentText() != null ? req.getCurrentText() : "").toLowerCase();
        String subject = (req.getTitle() != null ? req.getTitle() : "").toLowerCase();
        String combined = subject + " " + text;

        if (combined.contains("hire") || combined.contains("job") || combined.contains("interview") || combined.contains("full-time") || combined.contains("position") || combined.contains("role")) {
            res.setInquiryClassification("JOB_OPPORTUNITY");
            res.setInquirySentiment("HIGH_PRIORITY");
        } else if (combined.contains("project") || combined.contains("quote") || combined.contains("build") || combined.contains("contract") || combined.contains("freelance") || combined.contains("website")) {
            res.setInquiryClassification("PROJECT_CONSULTATION");
            res.setInquirySentiment("HIGH_PRIORITY");
        } else if (combined.contains("price") || combined.contains("cost") || combined.contains("rate") || combined.contains("budget")) {
            res.setInquiryClassification("QUOTATION_REQUEST");
            res.setInquirySentiment("MEDIUM_PRIORITY");
        } else if (combined.contains("seo") || combined.contains("casino") || combined.contains("crypto") || combined.contains("loan") || combined.contains("viagra")) {
            res.setInquiryClassification("SPAM");
            res.setInquirySentiment("LOW_PRIORITY");
        } else {
            res.setInquiryClassification("GENERAL_INQUIRY");
            res.setInquirySentiment("STANDARD");
        }
    }

    @Transactional(readOnly = true)
    public void auditPortfolio(AiAssistResponse res) {
        List<String> auditList = new ArrayList<>();

        long projects = projectRepository.count();
        long skills = skillRepository.count();
        long services = serviceRepository.count();
        long testimonials = testimonialRepository.count();
        boolean hasProfile = profileRepository.findFirstByActiveTrueOrderByIdAsc().isPresent();

        if (!hasProfile) auditList.add("Configure your primary Personal Profile with title, bio, and contact info.");
        if (projects < 3) auditList.add("Add at least 3 featured projects showcasing your Java Spring Boot and Web skills.");
        if (skills < 5) auditList.add("Add comprehensive technical skills across Backend, Frontend, and Database categories.");
        if (services < 2) auditList.add("Define professional service offerings (e.g. Web Development, API Architecture).");
        if (testimonials == 0) auditList.add("Collect client or collaborator recommendations to bolster social proof.");

        if (auditList.isEmpty()) {
            auditList.add("Your portfolio is well-configured with complete core content and professional showcases!");
        }

        res.setRecommendations(auditList);
    }
}
