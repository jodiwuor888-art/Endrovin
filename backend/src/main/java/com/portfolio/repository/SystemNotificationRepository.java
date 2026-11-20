package com.portfolio.repository;

import com.portfolio.entity.SystemNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {
    List<SystemNotification> findTop20ByOrderByCreatedAtDesc();
    long countByReadFalse();
}
