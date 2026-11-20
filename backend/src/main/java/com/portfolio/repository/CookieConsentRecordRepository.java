package com.portfolio.repository;

import com.portfolio.entity.CookieConsentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CookieConsentRecordRepository extends JpaRepository<CookieConsentRecord, Long> {
    Optional<CookieConsentRecord> findByConsentUuid(String consentUuid);
    long countByDecision(String decision);
    long countByCreatedAtAfter(LocalDateTime since);
}
