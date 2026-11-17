package com.portfolio.service;

import com.portfolio.config.PortfolioProperties;
import com.portfolio.entity.Admin;
import com.portfolio.entity.ContactMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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

    @Async
    public void sendContactNotification(ContactMessage message) {
        if (!properties.getContact().isEnableEmailNotifications()) {
            log.info("[CONTACT NOTIFICATION] New message received from {} ({}). SMTP notification disabled in configuration.",
                    message.getFullName(), message.getEmail());
            return;
        }

        if (mailSender == null) {
            log.warn("[CONTACT NOTIFICATION] MailSender bean not available. Skipping email dispatch.");
            return;
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(properties.getContact().getNotificationEmail());
            mail.setSubject("New Portfolio Contact: " + message.getSubject());
            mail.setText(String.format(
                    "You received a new inquiry on your Portfolio website:\n\n" +
                            "From: %s\n" +
                            "Email: %s\n" +
                            "IP Address: %s\n\n" +
                            "Message:\n%s\n\n" +
                            "---\n" +
                            "Respond from your Portfolio Admin Dashboard.",
                    message.getFullName(), message.getEmail(), message.getClientIp(), message.getMessage()
            ));

            mailSender.send(mail);
            log.info("[CONTACT NOTIFICATION] Email sent successfully to {}", properties.getContact().getNotificationEmail());
        } catch (Exception e) {
            log.error("[CONTACT NOTIFICATION] Failed to send email alert: {}", e.getMessage());
        }
    }

    public boolean sendPasswordResetEmail(Admin admin, String resetUrl) {
        if (mailSender == null) {
            log.error("[PASSWORD RESET] MailSender bean is unavailable; reset email was not sent.");
            return false;
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(admin.getEmail());
            String from = properties.getSecurity().getPasswordResetFrom();
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

            mailSender.send(mail);
            return true;
        } catch (MailException ex) {
            log.error("[PASSWORD RESET] Failed to send recovery email to administrator ID {}.",
                    admin.getId(), ex);
            return false;
        }
    }
}
