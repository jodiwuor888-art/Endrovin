package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "webauthn_challenges")
public class WebAuthnChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Long id;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "challenge_value", nullable = false, unique = true, length = 255)
    private String challengeValue; // Base64URL-encoded challenge

    @Column(name = "challenge_type", nullable = false, length = 50)
    private String challengeType; // "REGISTRATION", "AUTHENTICATION"

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public WebAuthnChallenge() {
    }

    public WebAuthnChallenge(Long adminId, String challengeValue, String challengeType, int expirySeconds) {
        this.adminId = adminId;
        this.challengeValue = challengeValue;
        this.challengeType = challengeType;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusSeconds(expirySeconds);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getChallengeValue() {
        return challengeValue;
    }

    public void setChallengeValue(String challengeValue) {
        this.challengeValue = challengeValue;
    }

    public String getChallengeType() {
        return challengeType;
    }

    public void setChallengeType(String challengeType) {
        this.challengeType = challengeType;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }
}
