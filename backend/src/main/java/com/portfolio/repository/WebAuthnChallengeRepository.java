package com.portfolio.repository;

import com.portfolio.entity.WebAuthnChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface WebAuthnChallengeRepository extends JpaRepository<WebAuthnChallenge, Long> {
    Optional<WebAuthnChallenge> findByChallengeValue(String challengeValue);
    Optional<WebAuthnChallenge> findByChallengeValueAndUsedAtIsNullAndExpiresAtAfter(String challengeValue, LocalDateTime now);
    void deleteByExpiresAtBefore(LocalDateTime now);
}
