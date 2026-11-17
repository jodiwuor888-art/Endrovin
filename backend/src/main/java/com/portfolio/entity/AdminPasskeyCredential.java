package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_passkey_credentials")
public class AdminPasskeyCredential {

    @Id
    @Column(name = "credential_id", length = 255)
    private String credentialId; // Base64URL-encoded credential ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Lob
    @Column(name = "public_key", nullable = false)
    private String publicKey; // Base64URL-encoded X.509 DER or COSE public key

    @Column(name = "sign_count", nullable = false)
    private long signCount = 0;

    @Column(name = "credential_transports", length = 255)
    private String credentialTransports; // e.g. "internal,hybrid,usb"

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    public AdminPasskeyCredential() {
    }

    public AdminPasskeyCredential(String credentialId, Admin admin, String publicKey, long signCount, String transports) {
        this.credentialId = credentialId;
        this.admin = admin;
        this.publicKey = publicKey;
        this.signCount = signCount;
        this.credentialTransports = transports;
        this.createdAt = LocalDateTime.now();
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public long getSignCount() {
        return signCount;
    }

    public void setSignCount(long signCount) {
        this.signCount = signCount;
    }

    public String getCredentialTransports() {
        return credentialTransports;
    }

    public void setCredentialTransports(String credentialTransports) {
        this.credentialTransports = credentialTransports;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
