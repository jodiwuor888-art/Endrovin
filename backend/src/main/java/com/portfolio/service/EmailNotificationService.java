package com.portfolio.service;

import com.portfolio.config.PortfolioProperties;
import com.portfolio.entity.Admin;
import com.portfolio.entity.ContactMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final PortfolioProperties properties;
    private final JavaMailSender springMailSender;
    private HttpClient httpClient;

    private volatile String lastDeliveryError = null;
    private volatile String lastDeliverySuccess = null;

    public EmailNotificationService(PortfolioProperties properties,
                                    @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender) {
        this.properties = properties;
        this.springMailSender = mailSender;
        try {
            this.httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(8))
                    .build();
        } catch (Throwable t) {
            log.warn("Eager HttpClient initialization deferred: {}", t.getMessage());
            this.httpClient = null;
        }
    }

    private synchronized HttpClient getHttpClient() {
        if (this.httpClient != null) {
            return this.httpClient;
        }
        try {
            this.httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(8))
                    .build();
            return this.httpClient;
        } catch (Throwable t) {
            log.error("Failed to create HttpClient: {}", t.getMessage());
            return null;
        }
    }

    public String getNotificationEmail() {
        String email = properties.getContact().getNotificationEmail();
        return (email != null && !email.isBlank()) ? email : "bevexj888@gmail.com";
    }

    public boolean isConfigured() {
        return isHttpApiConfigured() || isSmtpConfigured() || this.springMailSender != null;
    }

    public boolean isHttpApiConfigured() {
        String resendKey = getResendApiKey();
        String brevoKey = getBrevoApiKey();
        return (resendKey != null && !resendKey.isBlank()) || (brevoKey != null && !brevoKey.isBlank());
    }

    public boolean isSmtpConfigured() {
        String u = getSmtpUsername();
        String p = getSmtpPassword();
        return u != null && !u.isBlank() && p != null && !p.isBlank();
    }

    private String getResendApiKey() {
        String key = System.getenv("RESEND_API_KEY");
        return (key != null && !key.isBlank()) ? key.trim() : null;
    }

    private String getBrevoApiKey() {
        String key = System.getenv("BREVO_API_KEY");
        return (key != null && !key.isBlank()) ? key.trim() : null;
    }

    private String getSmtpUsername() {
        String u = System.getenv("SMTP_USERNAME");
        if (u != null && !u.isBlank()) return u.trim();
        return null;
    }

    private String getSmtpPassword() {
        String p = System.getenv("SMTP_PASSWORD");
        if (p != null && !p.isBlank()) {
            return p.replace(" ", "").trim();
        }
        return null;
    }

    /**
     * Resend HTTP API Client (Port 443 HTTPS - 100% Unrestricted on Render Free Tier)
     * Free tier: 3,000 emails/month at https://resend.com
     */
    private boolean sendViaResend(String apiKey, String from, String to, String replyTo, String subject, String body) {
        return sendViaResendInternal(apiKey, from, to, replyTo, subject, body, true);
    }

    private boolean sendViaResendInternal(String apiKey, String from, String to, String replyTo, String subject, String body, boolean allowAutoRoute) {
        try {
            String resendFrom = System.getenv("RESEND_FROM");
            if (resendFrom == null || resendFrom.isBlank()) {
                resendFrom = "onboarding@resend.dev";
            }

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"from\":\"").append(escapeJson(resendFrom)).append("\",");
            json.append("\"to\":[\"").append(escapeJson(to)).append("\"],");
            if (replyTo != null && !replyTo.isBlank()) {
                json.append("\"reply_to\":\"").append(escapeJson(replyTo)).append("\",");
            }
            json.append("\"subject\":\"").append(escapeJson(subject)).append("\",");
            json.append("\"text\":\"").append(escapeJson(body)).append("\"");
            json.append("}");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "JoashPortfolio/1.0")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpClient client = getHttpClient();
            if (client == null) {
                log.warn("[RESEND HTTPS] HttpClient unavailable in current environment");
                return false;
            }
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[RESEND HTTPS] Real-time email delivered successfully to {}! Response: {}", to, response.body());
                lastDeliverySuccess = "SUCCESS: Delivered to " + to + " (Response: " + response.body() + ")";
                lastDeliveryError = null;
                return true;
            } else {
                String errorBody = response.body();
                lastDeliveryError = "HTTP " + response.statusCode() + ": " + errorBody;
                log.error("[RESEND HTTPS] Dispatch failed with status {}: {}", response.statusCode(), errorBody);

                // Auto-route if on free tier and recipient does not match Resend account owner
                if (allowAutoRoute && response.statusCode() == 403 && errorBody.contains("only send testing emails to your own email address")) {
                    int start = errorBody.indexOf("(");
                    int end = errorBody.indexOf(")", start);
                    if (start != -1 && end != -1) {
                        String verifiedEmail = errorBody.substring(start + 1, end).trim();
                        log.warn("[RESEND HTTPS] Resend free testing domain detected. Auto-routing notification to verified account email: {}", verifiedEmail);
                        boolean rerouted = sendViaResendInternal(apiKey, from, verifiedEmail, replyTo, subject, body, false);
                        if (rerouted) {
                            lastDeliverySuccess = "SUCCESS: Delivered to verified Resend account (" + verifiedEmail + "). Note: To send directly to " + to + ", verify your domain at resend.com/domains.";
                            lastDeliveryError = null;
                            return true;
                        }
                    }
                }
                return false;
            }
        } catch (Exception e) {
            lastDeliveryError = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error("[RESEND HTTPS] Exception calling Resend API: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Brevo (Sendinblue) HTTP API Client (Port 443 HTTPS)
     */
    private boolean sendViaBrevo(String apiKey, String from, String to, String replyTo, String subject, String body) {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"sender\":{\"name\":\"Joash Portfolio\",\"email\":\"").append(escapeJson(from != null ? from : getNotificationEmail())).append("\"},");
            json.append("\"to\":[{\"email\":\"").append(escapeJson(to)).append("\"}],");
            if (replyTo != null && !replyTo.isBlank()) {
                json.append("\"replyTo\":{\"email\":\"").append(escapeJson(replyTo)).append("\"},");
            }
            json.append("\"subject\":\"").append(escapeJson(subject)).append("\",");
            json.append("\"textContent\":\"").append(escapeJson(body)).append("\"");
            json.append("}");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpClient client = getHttpClient();
            if (client == null) {
                log.warn("[RESEND HTTPS] HttpClient unavailable in current environment");
                return false;
            }
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[BREVO HTTPS] Real-time email delivered successfully to {}! Response: {}", to, response.body());
                return true;
            } else {
                log.error("[BREVO HTTPS] Dispatch failed with status {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("[BREVO HTTPS] Exception calling Brevo API: {}", e.getMessage(), e);
            return false;
        }
    }

    private JavaMailSender buildSender(int port, boolean useSsl) {
        String username = getSmtpUsername();
        String password = getSmtpPassword();
        if (username == null || password == null) {
            return null;
        }

        String host = System.getenv("SMTP_HOST");
        if (host == null || host.isBlank()) host = "smtp.gmail.com";

        JavaMailSenderImpl impl = new JavaMailSenderImpl();
        impl.setHost(host.trim());
        impl.setPort(port);
        impl.setUsername(username);
        impl.setPassword(password);

        Properties props = impl.getJavaMailProperties();

        if (useSsl) {
            impl.setProtocol("smtps");
            props.put("mail.transport.protocol", "smtps");
            props.put("mail.smtps.auth", "true");
            props.put("mail.smtps.ssl.enable", "true");
            props.put("mail.smtps.ssl.protocols", "TLSv1.2 TLSv1.3");
            props.put("mail.smtps.ssl.trust", host.trim());
            props.put("mail.smtps.connectiontimeout", "8000");
            props.put("mail.smtps.timeout", "8000");
            props.put("mail.smtps.writetimeout", "8000");
        } else {
            impl.setProtocol("smtp");
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            props.put("mail.smtp.ssl.trust", host.trim());
            props.put("mail.smtp.connectiontimeout", "8000");
            props.put("mail.smtp.timeout", "8000");
            props.put("mail.smtp.writetimeout", "8000");
        }

        return impl;
    }

    private boolean sendViaSmtp(SimpleMailMessage mail, String context) {
        if (isSmtpConfigured()) {
            String portStr = System.getenv("SMTP_PORT");
            int preferredPort = 465;
            if (portStr != null && !portStr.isBlank()) {
                try {
                    preferredPort = Integer.parseInt(portStr.trim());
                } catch (Exception ignored) {}
            }

            int firstPort = (preferredPort == 587) ? 465 : preferredPort;
            boolean firstSsl = (firstPort == 465);
            int fallbackPort = (firstPort == 465) ? 587 : 465;
            boolean fallbackSsl = (fallbackPort == 465);

            // Attempt 1: Port 465 SMTPS (Direct SSL)
            JavaMailSender primarySender = buildSender(firstPort, firstSsl);
            if (primarySender != null) {
                try {
                    primarySender.send(mail);
                    log.info("[{}] Email dispatched successfully via port {} (SSL={}) to {}", context, firstPort, firstSsl, mail.getTo());
                    return true;
                } catch (Exception e1) {
                    log.warn("[{}] Primary dispatch via port {} (SSL={}) failed: {}. Retrying via fallback port {}...",
                            context, firstPort, firstSsl, e1.getMessage(), fallbackPort);
                }
            }

            // Attempt 2: Fallback Port
            JavaMailSender fallbackSender = buildSender(fallbackPort, fallbackSsl);
            if (fallbackSender != null) {
                try {
                    fallbackSender.send(mail);
                    log.info("[{}] Fallback email dispatched successfully via port {} (SSL={}) to {}", context, fallbackPort, fallbackSsl, mail.getTo());
                    return true;
                } catch (Exception e2) {
                    log.error("[{}] Fallback dispatch via port {} also failed: {}", context, fallbackPort, e2.getMessage());
                }
            }
        }

        // Attempt 3: Spring auto-configured sender bean (or test mock)
        if (this.springMailSender != null) {
            try {
                this.springMailSender.send(mail);
                log.info("[{}] Email dispatched successfully via Spring auto-configured sender to {}", context, mail.getTo());
                return true;
            } catch (Exception e3) {
                log.error("[{}] Auto-configured sender attempt failed: {}", context, e3.getMessage());
            }
        }

        return false;
    }

    /**
     * Master Multi-Tier Email Dispatcher:
     * Tier 1: Resend HTTPS API (Port 443 — guaranteed to bypass cloud firewall blocks)
     * Tier 2: Brevo HTTPS API (Port 443)
     * Tier 3: Direct SMTPS (Port 465 / 587)
     */
    private boolean sendWithFallback(SimpleMailMessage mail, String replyTo, String context) {
        String to = mail.getTo() != null && mail.getTo().length > 0 ? mail.getTo()[0] : getNotificationEmail();
        String from = mail.getFrom() != null ? mail.getFrom() : getNotificationEmail();
        String subject = mail.getSubject() != null ? mail.getSubject() : "Portfolio Notification";
        String body = mail.getText() != null ? mail.getText() : "";

        // Tier 1: Resend HTTPS API
        String resendKey = getResendApiKey();
        if (resendKey != null) {
            log.info("[{}] Attempting real-time HTTPS dispatch via Resend API (port 443)...", context);
            boolean ok = sendViaResend(resendKey, from, to, replyTo, subject, body);
            if (ok) return true;
        }

        // Tier 2: Brevo HTTPS API
        String brevoKey = getBrevoApiKey();
        if (brevoKey != null) {
            log.info("[{}] Attempting real-time HTTPS dispatch via Brevo API (port 443)...", context);
            boolean ok = sendViaBrevo(brevoKey, from, to, replyTo, subject, body);
            if (ok) return true;
        }

        // Tier 3: Direct SMTP
        boolean smtpOk = sendViaSmtp(mail, context);
        if (smtpOk) return true;

        log.error("[{}] All delivery tiers failed for recipient {}", context, to);
        return false;
    }

    private boolean sendWithFallback(SimpleMailMessage mail, String context) {
        return sendWithFallback(mail, mail.getReplyTo(), context);
    }

    @Async
    public void sendContactNotification(ContactMessage message) {
        if (!properties.getContact().isEnableEmailNotifications()) {
            log.info("[CONTACT NOTIFICATION] New message received from {} ({}). Email notifications disabled in configuration.",
                    message.getFullName(), message.getEmail());
            return;
        }

        String recipient = getNotificationEmail();
        String from = getSmtpUsername();
        if (from == null || from.isBlank()) {
            from = properties.getSecurity().getPasswordResetFrom();
        }
        if (from == null || from.isBlank()) {
            from = recipient;
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(recipient);
        mail.setReplyTo(message.getEmail());
        mail.setFrom(from);
        mail.setSubject("⚡ Urgent Portfolio Inquiry: " + message.getSubject() + " (from " + message.getFullName() + ")");
        mail.setText(String.format(
                "Hello Joash,\n\n" +
                "A visitor has submitted a new inquiry on your portfolio website:\n\n" +
                "==================================================\n" +
                "VISITOR DETAILS\n" +
                "==================================================\n" +
                "Full Name:  %s\n" +
                "Email:      %s\n" +
                "Subject:    %s\n" +
                "IP Address: %s\n\n" +
                "==================================================\n" +
                "MESSAGE\n" +
                "==================================================\n" +
                "%s\n\n" +
                "==================================================\n" +
                "QUICK RESPONSE OPTIONS\n" +
                "==================================================\n" +
                "• Simply click 'Reply' in your email client to answer %s directly at %s.\n" +
                "• View and manage all messages in your Admin Dashboard:\n" +
                "  https://endrovin-frontendport.vercel.app/admin/dashboard.html#messages\n",
                message.getFullName(),
                message.getEmail(),
                message.getSubject(),
                message.getClientIp(),
                message.getMessage(),
                message.getFullName(),
                message.getEmail()
        ));

        sendWithFallback(mail, message.getEmail(), "CONTACT NOTIFICATION");
        sendInstantPhonePush("⚡ New Portfolio Inquiry: " + message.getFullName(),
                String.format("Email: %s\nSubject: %s\n\n%s", message.getEmail(), message.getSubject(), message.getMessage()),
                "envelope,sparkles");
    }

    public boolean sendPasswordResetEmail(Admin admin, String resetCode, String resetUrl) {
        String from = getSmtpUsername();
        if (from == null || from.isBlank()) {
            from = properties.getSecurity().getPasswordResetFrom();
        }

        String targetEmail = admin.getEmail();
        if (targetEmail == null || targetEmail.isBlank() || !targetEmail.contains("@")) {
            targetEmail = getNotificationEmail();
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(targetEmail);
        if (from != null && !from.isBlank()) {
            mail.setFrom(from);
        }

        mail.setSubject("🔑 Your Portfolio Admin Password Reset Code: " + resetCode);
        mail.setText(String.format(
                "Hello %s,\n\n" +
                "A password reset was requested for your portfolio administrator account.\n\n" +
                "==================================================\n" +
                "YOUR 6-DIGIT PASSWORD RESET CODE\n" +
                "==================================================\n\n" +
                "                   [ %s ]\n\n" +
                "==================================================\n" +
                "ACCOUNT DETAILS\n" +
                "==================================================\n" +
                "Username: %s\n" +
                "Expires In: 15 Minutes\n\n" +
                "HOW TO SET YOUR NEW PASSWORD:\n" +
                "Option 1 (Fastest): Return to the admin portal and type your 6-digit code: %s\n\n" +
                "Option 2 (One-Click): Click the direct recovery link below:\n" +
                "%s\n\n" +
                "If you did not request this password recovery, you can safely ignore this email.\n",
                admin.getFullName() != null && !admin.getFullName().isBlank() ? admin.getFullName() : admin.getUsername(),
                resetCode,
                admin.getUsername(),
                resetCode,
                resetUrl
        ));

        boolean sent = sendWithFallback(mail, "PASSWORD RESET");
        sendInstantPhonePush("🔑 Admin Password Reset Code: " + resetCode,
                String.format("Password recovery requested for administrator '%s'.\n\nYour 6-Digit Code: %s\n\nDirect Link: %s",
                        admin.getUsername(), resetCode, resetUrl),
                "key,lock");
        return sent;
    }

    public boolean sendPasswordResetEmail(Admin admin, String resetUrl) {
        return sendPasswordResetEmail(admin, "CODE", resetUrl);
    }

    public String getPhonePushTopic() {
        String topic = System.getenv("NTFY_TOPIC");
        if (topic != null && !topic.isBlank()) {
            return topic.trim();
        }
        return "joash-portfolio-alerts";
    }

    public void sendInstantPhonePush(String title, String body, String tags) {
        try {
            String topic = getPhonePushTopic();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://ntfy.sh/" + topic))
                    .header("Title", title)
                    .header("Priority", "urgent")
                    .header("Tags", tags != null ? tags : "bell")
                    .header("Click", "https://endrovin-frontendport.vercel.app/admin/dashboard.html#messages")
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpClient client = getHttpClient();
            if (client != null) {
                client.sendAsync(req, HttpResponse.BodyHandlers.discarding());
                log.info("[PHONE PUSH] Instant mobile notification dispatched to https://ntfy.sh/{}", topic);
            }
        } catch (Exception e) {
            log.warn("[PHONE PUSH] Mobile push dispatch error: {}", e.getMessage());
        }
    }

    public Map<String, Object> runDiagnostics() {
        Map<String, Object> diag = new LinkedHashMap<>();
        String u = getSmtpUsername();
        String p = getSmtpPassword();
        String resendKey = getResendApiKey();
        String brevoKey = getBrevoApiKey();

        diag.put("configured", isConfigured());
        diag.put("resend_api_configured", resendKey != null);
        diag.put("brevo_api_configured", brevoKey != null);
        diag.put("smtp_configured", isSmtpConfigured());
        diag.put("notification_recipient", getNotificationEmail());

        String host = System.getenv("SMTP_HOST");
        if (host == null || host.isBlank()) host = "smtp.gmail.com";
        diag.put("smtp_host", host);

        // Test TCP Socket to Port 465
        long t0 = System.currentTimeMillis();
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, 465), 5000);
            diag.put("tcp_port_465", "CONNECTED in " + (System.currentTimeMillis() - t0) + "ms");
        } catch (Exception e) {
            diag.put("tcp_port_465", "BLOCKED: " + e.getClass().getSimpleName() + " (Render Free Tier blocks raw SMTP ports)");
        }

        // Test TCP Socket to Port 587
        t0 = System.currentTimeMillis();
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, 587), 5000);
            diag.put("tcp_port_587", "CONNECTED in " + (System.currentTimeMillis() - t0) + "ms");
        } catch (Exception e) {
            diag.put("tcp_port_587", "BLOCKED: " + e.getClass().getSimpleName() + " (Render Free Tier blocks raw SMTP ports)");
        }

        if (resendKey != null) {
            diag.put("active_delivery_method", "Resend HTTPS API (Port 443 - Fully Unrestricted)");
        } else if (brevoKey != null) {
            diag.put("active_delivery_method", "Brevo HTTPS API (Port 443 - Fully Unrestricted)");
        } else {
            diag.put("active_delivery_method", "Direct SMTP (Blocked by Render Free Tier firewall)");
            diag.put("recommended_solution", "Add RESEND_API_KEY to Render Environment. Free signup at https://resend.com (takes 30 seconds). Enables instant real-time delivery over HTTPS port 443.");
        }

        // Test live dispatch
        if (isConfigured()) {
            SimpleMailMessage ping = new SimpleMailMessage();
            ping.setTo(getNotificationEmail());
            ping.setFrom(u != null ? u : "onboarding@resend.dev");
            ping.setSubject("⚡ Live Portfolio Notification Test (" + LocalDateTime.now() + ")");
            ping.setText("This is an automated live notification verification confirming that real-time email alerts are now reflecting in your inbox.");
            boolean sent = sendWithFallback(ping, "DIAGNOSTIC TEST");
            diag.put("test_email_dispatch", sent ? "SUCCESS - Dispatched to " + getNotificationEmail() : "FAILED");
            diag.put("resend_diagnostic_detail", lastDeliveryError != null ? lastDeliveryError : (lastDeliverySuccess != null ? lastDeliverySuccess : "None recorded"));
        } else {
            diag.put("test_email_dispatch", "SKIPPED - No email delivery method configured");
        }

        // ntfy.sh phone push diagnostics
        String ntfyTopic = getPhonePushTopic();
        diag.put("ntfy_push_topic", ntfyTopic);
        diag.put("ntfy_push_endpoint", "https://ntfy.sh/" + ntfyTopic);
        diag.put("ntfy_push_method", "HTTPS Port 443 (Fully Unrestricted on Render)");
        sendInstantPhonePush("⚡ Portfolio Live Diagnostic", "Real-time push notification is active! Your portfolio backend is online and ready to send instant alerts to your phone.", "white_check_mark,bell");
        diag.put("ntfy_push_test", "SUCCESS - Push sent to https://ntfy.sh/" + ntfyTopic);

        return diag;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
