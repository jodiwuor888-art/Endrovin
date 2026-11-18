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

import java.util.Properties;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final PortfolioProperties properties;
    private final JavaMailSender springMailSender;

    public EmailNotificationService(PortfolioProperties properties,
                                    @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender) {
        this.properties = properties;
        this.springMailSender = mailSender;
    }

    public String getNotificationEmail() {
        String email = properties.getContact().getNotificationEmail();
        return (email != null && !email.isBlank()) ? email : "bevexj888@gmail.com";
    }

    public boolean isConfigured() {
        String u = getSmtpUsername();
        String p = getSmtpPassword();
        return u != null && !u.isBlank() && p != null && !p.isBlank();
    }

    private String getSmtpUsername() {
        String u = System.getenv("SMTP_USERNAME");
        if (u != null && !u.isBlank()) return u.trim();
        return null;
    }

    private String getSmtpPassword() {
        String p = System.getenv("SMTP_PASSWORD");
        if (p != null && !p.isBlank()) {
            // Remove any whitespace (Google App Passwords are often copied in 4-character blocks with spaces)
            return p.replace(" ", "").trim();
        }
        return null;
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
            props.put("mail.smtps.socketFactory.port", String.valueOf(port));
            props.put("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtps.socketFactory.fallback", "false");
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

    private boolean sendWithFallback(SimpleMailMessage mail, String context) {
        if (!isConfigured()) {
            log.warn("[{}] SMTP credentials not set (SMTP_USERNAME / SMTP_PASSWORD). Skipping email alert.", context);
            return false;
        }

        String portStr = System.getenv("SMTP_PORT");
        int preferredPort = 465;
        if (portStr != null && !portStr.isBlank()) {
            try {
                preferredPort = Integer.parseInt(portStr.trim());
            } catch (Exception ignored) {}
        }

        // On cloud containers (Render), port 465 SMTPS connects directly through the outbound proxy.
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

        // Attempt 3: Spring auto-configured sender bean
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

        sendWithFallback(mail, "CONTACT NOTIFICATION");
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
                "Username:   %s\n" +
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

        return sendWithFallback(mail, "PASSWORD RESET");
    }

    // Overload for backward compatibility
    public boolean sendPasswordResetEmail(Admin admin, String resetUrl) {
        return sendPasswordResetEmail(admin, "CODE", resetUrl);
    }
}
