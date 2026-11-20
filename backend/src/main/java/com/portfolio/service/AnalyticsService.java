package com.portfolio.service;

import com.portfolio.dto.AnalyticsEventRequest;
import com.portfolio.dto.AnalyticsSummaryResponse;
import com.portfolio.entity.AnalyticsEvent;
import com.portfolio.repository.AnalyticsEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AnalyticsService {

    // Strict allowlist of permitted event types — prevents arbitrary data injection
    private static final Set<String> ALLOWED_EVENT_TYPES = Set.of(
            "page_view", "project_view", "service_view",
            "contact_form_start", "contact_form_submit",
            "resume_download", "github_click", "linkedin_click",
            "whatsapp_click", "email_click",
            "recommendation_submit", "quotation_request"
    );

    private final AnalyticsEventRepository analyticsEventRepository;

    public AnalyticsService(AnalyticsEventRepository analyticsEventRepository) {
        this.analyticsEventRepository = analyticsEventRepository;
    }

    @Transactional
    public void recordEvent(AnalyticsEventRequest req, String rawIp) {
        if (req == null || req.getEventType() == null) return;

        // Security: Reject unknown event types — prevents arbitrary data injection
        String eventType = req.getEventType().toLowerCase().trim();
        if (!ALLOWED_EVENT_TYPES.contains(eventType)) {
            // Silently discard; do not reveal the allowlist in error messages
            return;
        }

        // Sanitize and truncate all string inputs to prevent DB column overflow
        String pageUrl   = truncate(req.getPageUrl(), 500);
        String referrer  = truncate(req.getReferrer(), 500);
        String visitorId = truncate(req.getVisitorId(), 100);
        String deviceType = truncate(req.getDeviceType() != null ? req.getDeviceType() : "DESKTOP", 50);
        String browser   = truncate(req.getBrowser() != null ? req.getBrowser() : "Browser", 100);
        String os        = truncate(req.getOs() != null ? req.getOs() : "OS", 100);

        String anonymizedIp = hashIp(rawIp);
        AnalyticsEvent event = new AnalyticsEvent(
                eventType,
                truncate(req.getEntityId(), 100),
                pageUrl,
                referrer,
                visitorId,
                deviceType,
                browser,
                os,
                "Kenya",
                anonymizedIp
        );
        analyticsEventRepository.save(event);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary(String period) {
        LocalDateTime since = calculateSinceDate(period);

        AnalyticsSummaryResponse res = new AnalyticsSummaryResponse();
        res.setPeriod(period);
        res.setTotalPageViews(analyticsEventRepository.countByEventTypeAndTimestampAfter("page_view", since));
        res.setUniqueVisitors(analyticsEventRepository.countUniqueVisitorsSince(since));
        res.setProjectViews(analyticsEventRepository.countByEventTypeAndTimestampAfter("project_view", since));
        res.setServiceViews(analyticsEventRepository.countByEventTypeAndTimestampAfter("service_view", since));
        res.setContactFormSubmissions(analyticsEventRepository.countByEventTypeAndTimestampAfter("contact_form_submit", since));
        res.setCvDownloads(analyticsEventRepository.countByEventTypeAndTimestampAfter("resume_download", since));

        // Top pages
        List<Map<String, Object>> topPages = new ArrayList<>();
        for (Object[] row : analyticsEventRepository.findTopPagesSince(since)) {
            if (row != null && row.length >= 2) {
                Map<String, Object> map = new HashMap<>();
                map.put("url", String.valueOf(row[0]));
                map.put("views", row[1]);
                topPages.add(map);
            }
        }
        res.setTopPages(topPages);

        // Top referrers
        List<Map<String, Object>> topReferrers = new ArrayList<>();
        for (Object[] row : analyticsEventRepository.findTopReferrersSince(since)) {
            if (row != null && row.length >= 2) {
                Map<String, Object> map = new HashMap<>();
                map.put("referrer", String.valueOf(row[0]));
                map.put("count", row[1]);
                topReferrers.add(map);
            }
        }
        res.setTopReferrers(topReferrers);

        // Device distribution
        List<Map<String, Object>> devices = new ArrayList<>();
        for (Object[] row : analyticsEventRepository.findDeviceDistributionSince(since)) {
            if (row != null && row.length >= 2) {
                Map<String, Object> map = new HashMap<>();
                map.put("device", String.valueOf(row[0]));
                map.put("count", row[1]);
                devices.add(map);
            }
        }
        res.setDeviceDistribution(devices);

        return res;
    }

    private LocalDateTime calculateSinceDate(String period) {
        if ("today".equalsIgnoreCase(period)) {
            return LocalDateTime.now().toLocalDate().atStartOfDay();
        } else if ("yesterday".equalsIgnoreCase(period)) {
            return LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay();
        } else if ("7d".equalsIgnoreCase(period)) {
            return LocalDateTime.now().minusDays(7);
        } else if ("90d".equalsIgnoreCase(period)) {
            return LocalDateTime.now().minusDays(90);
        } else {
            // Default: 30 days
            return LocalDateTime.now().minusDays(30);
        }
    }

    private String hashIp(String ip) {
        if (ip == null || ip.isBlank()) return "anonymous";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(ip.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (Exception e) {
            return "hash-err";
        }
    }
}
