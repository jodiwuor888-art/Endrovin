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
            mail.setReplyTo(message.getEmail());

            String from = properties.getSecurity().getPasswordResetFrom();
            if (from != null && !from.isBlank()) {
                mail.setFrom(from);
            }

            mail.setSubject("⚡ Urgent Portfolio Inquiry: " + message.getSubject() + " (from " + message.getFullName() + ")");
            mail.setText(String.format(
                    "Hello Joash,\n\n" +
                    "A new visitor just submitted a contact message on your portfolio:\n\n" +
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

            mailSender.send(mail);
            log.info("[CONTACT NOTIFICATION] Alert email sent successfully to {}", properties.getContact().getNotificationEmail());
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
