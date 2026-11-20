package com.portfolio.repository;

import com.portfolio.entity.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    long countByTimestampAfter(LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT a.visitorId) FROM AnalyticsEvent a WHERE a.timestamp >= :since")
    long countUniqueVisitorsSince(@Param("since") LocalDateTime since);

    long countByEventTypeAndTimestampAfter(String eventType, LocalDateTime since);

    @Query("SELECT a.pageUrl, COUNT(a) as cnt FROM AnalyticsEvent a WHERE a.eventType = 'page_view' AND a.timestamp >= :since GROUP BY a.pageUrl ORDER BY cnt DESC")
    List<Object[]> findTopPagesSince(@Param("since") LocalDateTime since);

    @Query("SELECT a.referrer, COUNT(a) as cnt FROM AnalyticsEvent a WHERE a.referrer IS NOT NULL AND a.referrer != '' AND a.timestamp >= :since GROUP BY a.referrer ORDER BY cnt DESC")
    List<Object[]> findTopReferrersSince(@Param("since") LocalDateTime since);

    @Query("SELECT a.deviceType, COUNT(a) as cnt FROM AnalyticsEvent a WHERE a.timestamp >= :since GROUP BY a.deviceType ORDER BY cnt DESC")
    List<Object[]> findDeviceDistributionSince(@Param("since") LocalDateTime since);

    List<AnalyticsEvent> findTop50ByOrderByTimestampDesc();
}
