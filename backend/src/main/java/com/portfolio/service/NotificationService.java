package com.portfolio.service;

import com.portfolio.entity.SystemNotification;
import com.portfolio.repository.SystemNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final SystemNotificationRepository notificationRepository;

    public NotificationService(SystemNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public SystemNotification createNotification(String title, String message, String type, String linkUrl) {
        SystemNotification notification = new SystemNotification(title, message, type, linkUrl);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<SystemNotification> getRecentNotifications() {
        return notificationRepository.findTop20ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByReadFalse();
    }

    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead() {
        List<SystemNotification> unread = notificationRepository.findAll();
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
