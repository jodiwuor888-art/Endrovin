package com.portfolio.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Centralized, secure IP address extraction.
 *
 * X-Forwarded-For is ONLY trusted when the application is explicitly
 * configured to trust it (TRUST_PROXY_HEADERS=true). When running
 * directly (no proxy), the raw remoteAddr is always used to prevent
 * IP spoofing attacks that bypass rate-limiting and audit logs.
 */
@Component
public class IpAddressExtractor {

    @Value("${portfolio.security.trust-proxy-headers:false}")
    private boolean trustProxyHeaders;

    /**
     * Extract the real client IP address safely.
     * Never trust X-Forwarded-For unless explicitly configured.
     */
    public String extract(HttpServletRequest request) {
        if (trustProxyHeaders) {
            // Only use the LAST (rightmost) IP added by trusted infrastructure
            // The first entry is attacker-controlled; the last is set by our reverse proxy
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                String[] parts = xForwardedFor.split(",");
                // Use the last segment (set by our trusted Render/Vercel proxy)
                String ip = parts[parts.length - 1].trim();
                if (!ip.isBlank() && isValidIp(ip)) {
                    return ip;
                }
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isBlank() && isValidIp(xRealIp.trim())) {
                return xRealIp.trim();
            }
        }

        return request.getRemoteAddr();
    }

    private boolean isValidIp(String ip) {
        // Basic sanity check to avoid injecting malformed values into logs/DB
        if (ip == null || ip.length() > 45) return false;
        // Reject obvious injection attempts (must only contain IP chars)
        return ip.matches("[0-9a-fA-F:.]+");
    }
}
