package com.portfolio.service;

import com.portfolio.config.PortfolioProperties;
import com.portfolio.entity.Admin;
import com.portfolio.entity.ContactMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
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
    private final JavaMailSender mailSender;

    public EmailNotificationService(PortfolioProperties properties,
                                    @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender) {
        this.properties = properties;
        this.mailSender = mailSender;
    }

    public String getNotificationEmail() {
        String email = properties.getContact().getNotificationEmail();
        return (email != null && !email.isBlank()) ? email : "bevexj888@gmail.com";
    }

    public boolean isConfigured() {
        return resolveMailSender() != null;
    }

    private JavaMailSender resolveMailSender() {
        if (this.mailSender != null) {
            return this.mailSender;
        }

        // Dynamic fallback: build JavaMailSenderImpl from environment variables if auto-config didn't bind
        String username = System.getenv("SMTP_USERNAME");
        String password = System.getenv("SMTP_PASSWORD");
        if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
            String host = System.getenv("SMTP_HOST");
            if (host == null || host.isBlank()) host = "smtp.gmail.com";

            int port = 587;
            String portStr = System.getenv("SMTP_PORT");
            if (portStr != null && !portStr.isBlank()) {
                try {
                    port = Integer.parseInt(portStr.trim());
                } catch (Exception ignored) {}
            }

            JavaMailSenderImpl impl = new JavaMailSenderImpl();
            impl.setHost(host);
            impl.setPort(port);
            impl.setUsername(username.trim());
            impl.setPassword(password.trim());

            Properties props = impl.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "false");
            props.put("mail.smtp.connectiontimeout", "8000");
            props.put("mail.smtp.timeout", "8000");
            props.put("mail.smtp.writetimeout", "8000");

            log.info("[EMAIL SERVICE] Initialized dynamic JavaMailSender for {} via {}:{}", username, host, port);
            return impl;
        }
        return null;
    }

    @Async
    public void sendContactNotification(ContactMessage message) {
        if (!properties.getContact().isEnableEmailNotifications()) {
            log.info("[CONTACT NOTIFICATION] New message received from {} ({}). Email notifications disabled in configuration.",
                    message.getFullName(), message.getEmail());
            return;
        }

        JavaMailSender activeSender = resolveMailSender();
        if (activeSender == null) {
            log.warn("[CONTACT NOTIFICATION] SMTP credentials not set (SMTP_USERNAME / SMTP_PASSWORD). Message saved in database. Skipping email alert.");
            return;
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            String recipient = getNotificationEmail();
            mail.setTo(recipient);
            mail.setReplyTo(message.getEmail());

            String from = properties.getSecurity().getPasswordResetFrom();
            if (from == null || from.isBlank()) {
                from = System.getenv("SMTP_USERNAME");
            }
            if (from == null || from.isBlank()) {
                from = recipient;
            }
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

            activeSender.send(mail);
            log.info("[CONTACT NOTIFICATION] Alert email dispatched successfully to {}", recipient);
        } catch (Exception e) {
            log.error("[CONTACT NOTIFICATION] Failed to send email alert: {}", e.getMessage(), e);
        }
    }

    public boolean sendPasswordResetEmail(Admin admin, String resetUrl) {
        JavaMailSender activeSender = resolveMailSender();
        if (activeSender == null) {
            log.error("[PASSWORD RESET] MailSender is unavailable (SMTP_USERNAME / SMTP_PASSWORD missing); reset email was not sent.");
            return false;
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(admin.getEmail());

            String from = properties.getSecurity().getPasswordResetFrom();
            if (from == null || from.isBlank()) {
                from = System.getenv("SMTP_USERNAME");
            }
            if (from != null && !from.isBlank()) {
                mail.setFrom(from);
            }

            mail.setSubject("Administrator account recovery");
            mail.setText(String.format(
                    "A password reset was requested for your portfolio administrator account.\n\n" +
                            "Username: %s\n\n" +
                            "Use this one-time link within 15 minutes to set a new password:\n%s\n\n" +
                            "If you did not request this, you can ignore this email.",
                    admin.getUsername(), resetUrl
            ));

            activeSender.send(mail);
            return true;
        } catch (MailException ex) {
            log.error("[PASSWORD RESET] Failed to send recovery email to administrator ID {}.",
                    admin.getId(), ex);
            return false;
        }
    }
}
