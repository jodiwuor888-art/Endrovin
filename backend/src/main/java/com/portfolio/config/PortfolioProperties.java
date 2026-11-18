package com.portfolio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "portfolio")
public class PortfolioProperties {

    private final Security security = new Security();
    private final Upload upload = new Upload();
    private final Contact contact = new Contact();

    public Security getSecurity() {
        return security;
    }

    public Upload getUpload() {
        return upload;
    }

    public Contact getContact() {
        return contact;
    }

    public static class Security {
        private final Jwt jwt = new Jwt();
        private final RateLimit rateLimit = new RateLimit();
        private String passwordResetUrl = "http://localhost:8080/admin/login.html";
        private String passwordResetFrom = "";

        public Jwt getJwt() {
            return jwt;
        }

        public RateLimit getRateLimit() {
            return rateLimit;
        }

        public String getPasswordResetUrl() {
            return passwordResetUrl;
        }

        public void setPasswordResetUrl(String passwordResetUrl) {
            this.passwordResetUrl = passwordResetUrl;
        }

        public String getPasswordResetFrom() {
            return passwordResetFrom;
        }

        public void setPasswordResetFrom(String passwordResetFrom) {
            this.passwordResetFrom = passwordResetFrom;
        }

        public static class Jwt {
            private String secret = "4c9b1397b98d4924a9d7249876fecba27931f82e5b7c8466e34458b688d0bcae";
            private long expirationMs = 86400000;

            public String getSecret() {
                return secret;
            }

            public void setSecret(String secret) {
                this.secret = secret;
            }

            public long getExpirationMs() {
                return expirationMs;
            }

            public void setExpirationMs(long expirationMs) {
                this.expirationMs = expirationMs;
            }
        }

        public static class RateLimit {
            private int maxAttempts = 5;
            private int lockoutDurationMinutes = 15;

            public int getMaxAttempts() {
                return maxAttempts;
            }

            public void setMaxAttempts(int maxAttempts) {
                this.maxAttempts = maxAttempts;
            }

            public int getLockoutDurationMinutes() {
                return lockoutDurationMinutes;
            }

            public void setLockoutDurationMinutes(int lockoutDurationMinutes) {
                this.lockoutDurationMinutes = lockoutDurationMinutes;
            }
        }
    }

    public static class Upload {
        private String dir = "uploads";
        private long maxImageBytes = 5242880;
        private long maxDocumentBytes = 10485760;
        private List<String> allowedImageTypes = new ArrayList<>(List.of("image/jpeg", "image/png", "image/webp", "image/gif"));
        private List<String> allowedDocumentTypes = new ArrayList<>(List.of("application/pdf"));

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }

        public long getMaxImageBytes() {
            return maxImageBytes;
        }

        public void setMaxImageBytes(long maxImageBytes) {
            this.maxImageBytes = maxImageBytes;
        }

        public long getMaxDocumentBytes() {
            return maxDocumentBytes;
        }

        public void setMaxDocumentBytes(long maxDocumentBytes) {
            this.maxDocumentBytes = maxDocumentBytes;
        }

        public List<String> getAllowedImageTypes() {
            return allowedImageTypes;
        }

        public void setAllowedImageTypes(List<String> allowedImageTypes) {
            this.allowedImageTypes = allowedImageTypes;
        }

        public List<String> getAllowedDocumentTypes() {
            return allowedDocumentTypes;
        }

        public void setAllowedDocumentTypes(List<String> allowedDocumentTypes) {
            this.allowedDocumentTypes = allowedDocumentTypes;
        }
    }

    public static class Contact {
        private String notificationEmail = "bevexj888@gmail.com";
        private String enableEmailNotifications = "true";

        public String getNotificationEmail() {
            if (enableEmailNotifications != null && enableEmailNotifications.contains("@")) {
                return enableEmailNotifications.trim();
            }
            return (notificationEmail != null && !notificationEmail.isBlank()) ? notificationEmail : "bevexj888@gmail.com";
        }

        public void setNotificationEmail(String notificationEmail) {
            this.notificationEmail = notificationEmail;
        }

        public boolean isEnableEmailNotifications() {
            if (enableEmailNotifications == null || enableEmailNotifications.isBlank()) {
                return false;
            }
            String val = enableEmailNotifications.trim().toLowerCase();
            return "true".equals(val) || "1".equals(val) || "yes".equals(val) || "on".equals(val) || val.contains("@");
        }

        public void setEnableEmailNotifications(String enableEmailNotifications) {
            this.enableEmailNotifications = enableEmailNotifications;
        }
    }
}
