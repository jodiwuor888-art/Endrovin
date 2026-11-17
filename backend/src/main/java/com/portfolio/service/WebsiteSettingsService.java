package com.portfolio.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.dto.SettingsAuditTrailDto;
import com.portfolio.entity.AuditLog;
import com.portfolio.entity.WebsiteSetting;
import com.portfolio.exception.BadRequestException;
import com.portfolio.repository.AuditLogRepository;
import com.portfolio.repository.WebsiteSettingRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@SuppressWarnings("null")
public class WebsiteSettingsService {

    private static final Logger log = LoggerFactory.getLogger(WebsiteSettingsService.class);

    private final WebsiteSettingRepository settingRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Default System Settings Master Registry
    private static final Map<String, SettingDefinition> DEFAULT_REGISTRY = new LinkedHashMap<>();

    private record SettingDefinition(String key, String defaultValue, String group, String description, boolean isPublic) {}

    static {
        // 1. GENERAL
        def("general.websiteName", "Portfolio Management System", "GENERAL", "Website brand and header name", true);
        def("general.brandName", "Joash Odiwuor", "GENERAL", "Portfolio personal brand name", true);
        def("general.tagline", "Diploma in Computer Science & Aspiring Software Developer", "GENERAL", "Header tagline or sub-headline", true);
        def("general.description", "Professional portfolio and software engineering showcase", "GENERAL", "General website meta description", true);
        def("general.ownerName", "Joash Odiwuor", "GENERAL", "Full legal name of the website owner", true);
        def("general.professionalTitle", "Aspiring Software Developer", "GENERAL", "Professional job title", true);
        def("general.contactEmail", "jodiwuor888@gmail.com", "GENERAL", "Primary administrative contact email", true);
        def("general.contactPhone", "+254 742963410", "GENERAL", "Primary contact telephone / WhatsApp", true);
        def("general.physicalLocation", "Kenya", "GENERAL", "City, Region, or Country", true);
        def("general.defaultLanguage", "en", "GENERAL", "Default website language code", true);
        def("general.timezone", "Africa/Nairobi", "GENERAL", "Default system timezone", true);
        def("general.maintenanceMode", "false", "GENERAL", "Whether the public website is in maintenance mode", true);
        def("general.logoUrl", "", "GENERAL", "URL to brand logo image", true);
        def("general.faviconUrl", "", "GENERAL", "URL to custom browser favicon", true);

        // 2. APPEARANCE
        def("appearance.themeMode", "dark", "APPEARANCE", "Theme color mode: dark, light, or auto", true);
        def("appearance.primaryColor", "#070e17", "APPEARANCE", "Primary background dark obsidian color", true);
        def("appearance.secondaryColor", "#0c1a27", "APPEARANCE", "Deep slate secondary section background", true);
        def("appearance.accentColor", "#10b981", "APPEARANCE", "Radiant emerald green accent color", true);
        def("appearance.backgroundColor", "#050c12", "APPEARANCE", "Deepest background base color", true);
        def("appearance.textColor", "#f8fafc", "APPEARANCE", "Primary light typography color", true);
        def("appearance.fontFamily", "Inter, system-ui, sans-serif", "APPEARANCE", "Modern professional font family", true);
        def("appearance.baseFontSize", "16px", "APPEARANCE", "Base typography scale size", true);
        def("appearance.buttonRadius", "8px", "APPEARANCE", "Button and card border-radius", true);
        def("appearance.headerLayout", "sticky", "APPEARANCE", "Navigation bar layout: sticky or static", true);
        def("appearance.footerLayout", "standard", "APPEARANCE", "Footer layout configuration", true);
        def("appearance.enableSmoothScroll", "true", "APPEARANCE", "Smooth scrolling for in-page anchors", true);

        // 3. HOMEPAGE
        def("homepage.showAbout", "true", "HOMEPAGE", "Show About Me section on public homepage", true);
        def("homepage.showSkills", "true", "HOMEPAGE", "Show Technical Skills section", true);
        def("homepage.showServices", "true", "HOMEPAGE", "Show Professional Services section", true);
        def("homepage.showProjects", "true", "HOMEPAGE", "Show Projects portfolio section", true);
        def("homepage.showEducation", "true", "HOMEPAGE", "Show Education qualifications section", true);
        def("homepage.showExperience", "true", "HOMEPAGE", "Show Professional Experience section", true);
        def("homepage.showCertificates", "true", "HOMEPAGE", "Show Verified Certificates section", true);
        def("homepage.showAchievements", "true", "HOMEPAGE", "Show Honors & Achievements section", true);
        def("homepage.showTestimonials", "true", "HOMEPAGE", "Show Recommendations & Testimonials section", true);
        def("homepage.showContact", "true", "HOMEPAGE", "Show Contact enquiry form section", true);
        def("homepage.showSocialIcons", "true", "HOMEPAGE", "Show social media icons in hero & footer", true);
        def("homepage.maxProjectsDisplayed", "6", "HOMEPAGE", "Maximum projects displayed on homepage", true);
        def("homepage.maxTestimonialsDisplayed", "6", "HOMEPAGE", "Maximum testimonials displayed on homepage", true);
        def("homepage.sectionOrder", "about,skills,services,projects,education,experience,certificates,testimonials,contact", "HOMEPAGE", "Ordered list of sections", true);

        // 4. SEO
        def("seo.metaTitle", "Joash Odiwuor | Software Developer Portfolio", "SEO", "Default HTML document title", true);
        def("seo.metaDescription", "Professional portfolio of Joash Odiwuor - Diploma in Computer Science student and software developer in Kenya.", "SEO", "Search engine meta description", true);
        def("seo.keywords", "software developer, Java, Spring Boot, full-stack, Kenya, web developer", "SEO", "Search engine comma-separated keywords", true);
        def("seo.ogImageUrl", "", "SEO", "Open Graph social media preview card image URL", true);
        def("seo.allowIndexing", "true", "SEO", "Whether search engines should index the website (robots)", true);
        def("seo.canonicalUrl", "https://portfolio.local", "SEO", "Canonical URL for SEO optimization", true);
        def("seo.googleSiteVerification", "", "SEO", "Google Search Console verification meta token", true);
        def("seo.robotsTxt", "User-agent: *\nAllow: /\nDisallow: /admin/", "SEO", "Custom robots.txt directive", true);

        // 5. CONTACT_SOCIAL (Only populated on public portfolio when admin explicitly configures them)
        def("contact.publicEmail", "", "CONTACT_SOCIAL", "Email displayed publicly on contact section", true);
        def("contact.phone", "", "CONTACT_SOCIAL", "Phone number displayed publicly", true);
        def("contact.whatsapp", "", "CONTACT_SOCIAL", "WhatsApp direct message number", true);
        def("contact.githubUrl", "", "CONTACT_SOCIAL", "GitHub developer profile URL", true);
        def("contact.linkedinUrl", "", "CONTACT_SOCIAL", "LinkedIn professional profile URL", true);
        def("contact.twitterUrl", "", "CONTACT_SOCIAL", "X / Twitter profile URL", true);
        def("contact.youtubeUrl", "", "CONTACT_SOCIAL", "YouTube channel URL", true);
        def("contact.enableContactForm", "true", "CONTACT_SOCIAL", "Whether visitor inquiry form is active", true);
        def("contact.contactFormRecipient", "jodiwuor888@gmail.com", "CONTACT_SOCIAL", "Email address receiving visitor inquiries", false);

        // 6. EMAIL_NOTIFICATIONS
        def("email.smtpHost", "smtp.gmail.com", "EMAIL_NOTIFICATIONS", "Outbound SMTP server hostname", false);
        def("email.smtpPort", "587", "EMAIL_NOTIFICATIONS", "Outbound SMTP server port", false);
        def("email.smtpUsername", "", "EMAIL_NOTIFICATIONS", "SMTP authentication username", false);
        def("email.smtpPassword", "", "EMAIL_NOTIFICATIONS", "SMTP authentication password", false);
        def("email.smtpAuth", "true", "EMAIL_NOTIFICATIONS", "Require SMTP authentication", false);
        def("email.smtpStartTls", "true", "EMAIL_NOTIFICATIONS", "Enable STARTTLS encryption", false);
        def("email.senderName", "Joash Portfolio System", "EMAIL_NOTIFICATIONS", "Name on automated outgoing emails", false);
        def("email.senderEmail", "no-reply@portfolio.local", "EMAIL_NOTIFICATIONS", "From email address on outbound emails", false);
        def("email.notifyOnContactInquiry", "true", "EMAIL_NOTIFICATIONS", "Send instant email on new contact inquiry", false);
        def("email.notifyOnTestimonialSubmission", "true", "EMAIL_NOTIFICATIONS", "Send email on new visitor testimonial", false);
        def("email.notifyOnAdminLogin", "true", "EMAIL_NOTIFICATIONS", "Send security alert email on admin sign-in", false);

        // 7. MEDIA_UPLOADS
        def("media.maxImageSizeMb", "5", "MEDIA_UPLOADS", "Max uploaded image size in megabytes", false);
        def("media.maxDocumentSizeMb", "15", "MEDIA_UPLOADS", "Max uploaded CV / document size in megabytes", false);
        def("media.allowedImageFormats", "jpg,jpeg,png,webp,svg", "MEDIA_UPLOADS", "Permitted image extensions", false);
        def("media.allowedDocumentFormats", "pdf,doc,docx", "MEDIA_UPLOADS", "Permitted document extensions", false);
        def("media.enableImageCompression", "true", "MEDIA_UPLOADS", "Optimize and compress uploaded images", false);
        def("media.storageLocation", "local_disk", "MEDIA_UPLOADS", "File storage engine: local_disk or cloud", false);

        // 8. PRIVACY_LEGAL
        def("privacy.enableCookieBanner", "true", "PRIVACY_LEGAL", "Display cookie consent banner to visitors", true);
        def("privacy.cookieBannerText", "This website uses essential cookies for secure browsing and system integrity.", "PRIVACY_LEGAL", "Notice displayed on cookie banner", true);
        def("privacy.policyText", "We value your privacy. Information submitted through contact forms or recommendation submissions is never shared with third parties.", "PRIVACY_LEGAL", "Privacy policy disclosure text", true);
        def("privacy.termsText", "All content, designs, and source code demonstrations are protected.", "PRIVACY_LEGAL", "Terms of service disclosure text", true);
        def("privacy.dataRetentionDays", "365", "PRIVACY_LEGAL", "Days to retain contact submissions before archiving", false);
        def("privacy.requireExplicitConsent", "true", "PRIVACY_LEGAL", "Enforce consent checkbox before testimonial submission", true);

        // 9. BACKUP_MAINTENANCE
        def("maintenance.autoBackupEnabled", "false", "BACKUP_MAINTENANCE", "Enable scheduled database backups", false);
        def("maintenance.backupFrequency", "weekly", "BACKUP_MAINTENANCE", "Backup schedule: daily, weekly, monthly", false);
        def("maintenance.maintenanceNotice", "The portfolio is briefly undergoing scheduled updates. Please check back shortly.", "BACKUP_MAINTENANCE", "Message shown during maintenance mode", true);
        def("maintenance.systemVersion", "1.0.0 (Production)", "BACKUP_MAINTENANCE", "Current portfolio system release version", true);

        // 10. SECURITY_ACCESS
        def("security.requireMfaPasskey", "true", "SECURITY_ACCESS", "Enforce biometric / FIDO2 passkey MFA for administrators", false);
        def("security.sessionTimeoutMinutes", "60", "SECURITY_ACCESS", "Administrator JWT session inactivity timeout", false);
        def("security.maxFailedLoginAttempts", "5", "SECURITY_ACCESS", "Failed login attempts before account lockout", false);
        def("security.lockoutDurationMinutes", "15", "SECURITY_ACCESS", "Temporary lockout window duration in minutes", false);
        def("security.passwordMinLength", "8", "SECURITY_ACCESS", "Minimum allowed password length", false);
        def("security.enableLoginAlerts", "true", "SECURITY_ACCESS", "Record and alert on unusual authentication attempts", false);

        // 11. ANALYTICS_TRACKING
        def("analytics.enableVisitorTracking", "true", "ANALYTICS_TRACKING", "Track page views and device analytics", true);
        def("analytics.googleAnalyticsId", "", "ANALYTICS_TRACKING", "Google Analytics 4 Measurement ID (G-XXXXXXXX)", true);
        def("analytics.anonymizeIp", "true", "ANALYTICS_TRACKING", "Anonymize visitor IP addresses for GDPR compliance", true);
        def("analytics.trackContactConversions", "true", "ANALYTICS_TRACKING", "Log contact form submissions as conversion events", true);
        def("analytics.trackCvDownloads", "true", "ANALYTICS_TRACKING", "Log resume / CV file downloads as events", true);

        // 12. INTEGRATIONS
        def("integrations.googleMapsApiKey", "", "INTEGRATIONS", "Google Maps Embed JavaScript API key", false);
        def("integrations.recaptchaSiteKey", "", "INTEGRATIONS", "Google reCAPTCHA v3 / Turnstile public site key", true);
        def("integrations.recaptchaSecretKey", "", "INTEGRATIONS", "reCAPTCHA server-side secret key", false);
        def("integrations.whatsappDirectChat", "true", "INTEGRATIONS", "Show one-click WhatsApp floating chat trigger", true);
        def("integrations.customDomain", "", "INTEGRATIONS", "Configured production custom domain name", true);

        // 13. ADVANCED_CONFIG
        def("advanced.customCss", "", "ADVANCED_CONFIG", "Custom scoped CSS styling rules injected in head", true);
        def("advanced.customHeaderScripts", "", "ADVANCED_CONFIG", "Header scripts (e.g. meta tags, analytics snippets)", false);
        def("advanced.customFooterScripts", "", "ADVANCED_CONFIG", "Footer scripts (e.g. chat widgets)", false);
        def("advanced.enableApiRateLimiting", "true", "ADVANCED_CONFIG", "Rate-limit public REST API calls", false);
        def("advanced.cacheControlMaxAge", "3600", "ADVANCED_CONFIG", "HTTP Cache-Control header max-age in seconds", false);
    }

    private static void def(String key, String defVal, String group, String desc, boolean isPublic) {
        DEFAULT_REGISTRY.put(key, new SettingDefinition(key, defVal, group, desc, isPublic));
    }

    public WebsiteSettingsService(WebsiteSettingRepository settingRepository,
                                  AuditLogRepository auditLogRepository,
                                  AuditLogService auditLogService) {
        this.settingRepository = settingRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditLogService = auditLogService;
    }

    @PostConstruct
    @Transactional
    public void initDefaults() {
        try {
            // Seed defaults for any missing keys
            for (SettingDefinition def : DEFAULT_REGISTRY.values()) {
                if (!settingRepository.existsById(def.key())) {
                    settingRepository.save(new WebsiteSetting(def.key(), def.defaultValue(), def.group(), def.description()));
                }
            }
            // Ensure initial maintenance mode starts as 'false' until admin explicitly toggles it in Web Settings
            settingRepository.findById("general.maintenanceMode").ifPresent(setting -> {
                if ("true".equalsIgnoreCase(setting.getSettingValue())) {
                    setting.setSettingValue("false");
                    settingRepository.save(setting);
                    log.info("Initialized general.maintenanceMode to 'false' (visitors can view live portfolio normally)");
                }
            });
        } catch (Exception e) {
            log.warn("Could not pre-seed settings table: {}", e.getMessage());
        }
    }

    /**
     * Get all settings merged with system defaults
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAllSettingsCategorized() {
        Map<String, String> dbMap = loadDbSettingsMap();

        Map<String, Map<String, Object>> categories = new LinkedHashMap<>();
        Map<String, String> flatValues = new LinkedHashMap<>();

        DEFAULT_REGISTRY.forEach((key, def) -> {
            String val = dbMap.getOrDefault(key, def.defaultValue());
            flatValues.put(key, val);

            categories.computeIfAbsent(def.group(), g -> new LinkedHashMap<>());
            Map<String, Object> item = new HashMap<>();
            item.put("key", key);
            item.put("value", val);
            item.put("defaultValue", def.defaultValue());
            item.put("group", def.group());
            item.put("description", def.description());
            item.put("isPublic", def.isPublic());
            categories.get(def.group()).put(key, item);
        });

        // Also add any custom keys stored in DB not in default registry
        dbMap.forEach((k, v) -> {
            if (!DEFAULT_REGISTRY.containsKey(k)) {
                flatValues.put(k, v);
            }
        });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("categories", categories);
        result.put("settings", flatValues);
        result.put("totalCount", flatValues.size());
        result.put("lastUpdated", LocalDateTime.now());
        return result;
    }

    /**
     * Batch save settings from admin form
     */
    @Transactional
    public void batchSaveSettings(Map<String, String> newSettings, String username, String ip) {
        if (newSettings == null || newSettings.isEmpty()) {
            return;
        }

        int updatedCount = 0;
        for (Map.Entry<String, String> entry : newSettings.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();

            // Sanitize key
            if (key == null || key.isBlank()) continue;
            key = key.trim();

            SettingDefinition def = DEFAULT_REGISTRY.get(key);
            String group = def != null ? def.group() : "CUSTOM";
            String desc = def != null ? def.description() : "Custom configuration setting";

            WebsiteSetting setting = settingRepository.findById(key)
                    .orElse(new WebsiteSetting(key, val, group, desc));
            setting.setSettingValue(val != null ? val : "");
            setting.setSettingGroup(group);
            setting.setDescription(desc);
            settingRepository.save(setting);
            updatedCount++;
        }

        auditLogService.logAction(username, "BATCH_UPDATE_SETTINGS", "WebsiteSettings", null,
                "Batch updated " + updatedCount + " website setting(s)", ip);
        log.info("Administrator [{}] successfully batch updated {} settings", username, updatedCount);
    }

    /**
     * Reset all settings to system defaults
     */
    @Transactional
    public void resetToDefaults(String username, String ip) {
        settingRepository.deleteAll();

        // Seed with standard defaults
        for (SettingDefinition def : DEFAULT_REGISTRY.values()) {
            settingRepository.save(new WebsiteSetting(def.key(), def.defaultValue(), def.group(), def.description()));
        }

        auditLogService.logAction(username, "RESET_SETTINGS_DEFAULT", "WebsiteSettings", null,
                "Reset all website settings to factory defaults", ip);
        log.warn("Administrator [{}] reset all website settings to system defaults", username);
    }

    /**
     * Export all settings as JSON string (masking private secrets)
     */
    @Transactional(readOnly = true)
    public String exportSettingsJson(boolean includeSecrets) {
        try {
            Map<String, String> exportMap = new LinkedHashMap<>();
            Map<String, String> dbMap = loadDbSettingsMap();

            DEFAULT_REGISTRY.forEach((key, def) -> {
                String val = dbMap.getOrDefault(key, def.defaultValue());
                if (!includeSecrets && isSecretKey(key)) {
                    val = "********";
                }
                exportMap.put(key, val);
            });

            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("system", "Portfolio Management System");
            envelope.put("version", "1.0.0");
            envelope.put("exportedAt", LocalDateTime.now().toString());
            envelope.put("settings", exportMap);

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(envelope);
        } catch (Exception e) {
            log.error("Failed to export settings JSON: {}", e.getMessage(), e);
            throw new BadRequestException("Settings export failed: " + e.getMessage());
        }
    }

    /**
     * Import settings from JSON backup
     */
    @Transactional
    public int importSettingsJson(String json, String username, String ip) {
        try {
            if (json == null || json.isBlank()) {
                throw new BadRequestException("Import JSON cannot be empty");
            }

            Map<String, Object> root = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            @SuppressWarnings("unchecked")
            Map<String, String> settingsMap = (Map<String, String>) root.get("settings");

            if (settingsMap == null || settingsMap.isEmpty()) {
                throw new BadRequestException("Invalid backup JSON format: 'settings' map not found");
            }

            int imported = 0;
            for (Map.Entry<String, String> entry : settingsMap.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue();

                // Skip masked secrets so we don't overwrite valid passwords with '********'
                if ("********".equals(val)) {
                    continue;
                }

                SettingDefinition def = DEFAULT_REGISTRY.get(key);
                String group = def != null ? def.group() : "CUSTOM";
                String desc = def != null ? def.description() : "Imported configuration setting";

                WebsiteSetting s = settingRepository.findById(key)
                        .orElse(new WebsiteSetting(key, val, group, desc));
                s.setSettingValue(val != null ? val : "");
                s.setSettingGroup(group);
                s.setDescription(desc);
                settingRepository.save(s);
                imported++;
            }

            auditLogService.logAction(username, "IMPORT_SETTINGS", "WebsiteSettings", null,
                    "Imported " + imported + " settings from JSON backup", ip);
            log.info("Administrator [{}] imported {} settings from JSON file", username, imported);
            return imported;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to import settings: {}", e.getMessage(), e);
            throw new BadRequestException("Invalid JSON settings format: " + e.getMessage());
        }
    }

    /**
     * Publish settings changes (logs action and verifies state)
     */
    @Transactional
    public void publishSettings(String username, String ip) {
        auditLogService.logAction(username, "PUBLISH_SETTINGS", "WebsiteSettings", null,
                "Published website configuration changes to live website", ip);
        log.info("Administrator [{}] published website settings to live visitors", username);
    }

    /**
     * Publicly accessible settings for frontend theming, SEO, and section display.
     * NEVER returns passwords, secret keys, or private internal configurations.
     */
    @Transactional(readOnly = true)
    public Map<String, String> getPublicSettings() {
        Map<String, String> dbMap = loadDbSettingsMap();

        Map<String, String> publicMap = new LinkedHashMap<>();

        DEFAULT_REGISTRY.forEach((key, def) -> {
            if (def.isPublic() && !isSecretKey(key)) {
                String val = dbMap.getOrDefault(key, def.defaultValue());
                publicMap.put(key, val);
            }
        });

        return publicMap;
    }

    /**
     * Retrieve settings audit history
     */
    @Transactional(readOnly = true)
    public List<SettingsAuditTrailDto> getSettingsAuditTrail() {
        List<AuditLog> logs = auditLogRepository.findTop50ByOrderByTimestampDesc();
        List<SettingsAuditTrailDto> dtos = new ArrayList<>();

        for (AuditLog l : logs) {
            if ("WebsiteSettings".equalsIgnoreCase(l.getEntityName()) ||
                (l.getAction() != null && l.getAction().contains("SETTING"))) {
                dtos.add(new SettingsAuditTrailDto(
                        l.getId(),
                        l.getAction(),
                        l.getEntityId(),
                        l.getDetails(),
                        l.getAdminUsername(),
                        l.getIpAddress(),
                        l.getTimestamp()
                ));
            }
        }
        return dtos;
    }

    private Map<String, String> loadDbSettingsMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (WebsiteSetting setting : settingRepository.findAll()) {
            if (setting != null && setting.getSettingKey() != null) {
                map.put(setting.getSettingKey(), setting.getSettingValue() != null ? setting.getSettingValue() : "");
            }
        }
        return map;
    }

    private boolean isSecretKey(String key) {
        if (key == null) return false;
        String lower = key.toLowerCase(Locale.ROOT);
        return lower.contains("password") || lower.contains("secret") || lower.contains("private") ||
               lower.contains("credential") || lower.contains("token");
    }
}
