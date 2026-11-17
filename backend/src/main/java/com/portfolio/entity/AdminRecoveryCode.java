package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_recovery_codes")
public class AdminRecoveryCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recovery_code_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "hashed_recovery_code", nullable = false, length = 255)
    private String hashedRecoveryCode;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AdminRecoveryCode() {
    }

    public AdminRecoveryCode(Admin admin, String hashedRecoveryCode) {
        this.admin = admin;
        this.hashedRecoveryCode = hashedRecoveryCode;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getRecoveryCodeId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getHashedRecoveryCode() {
        return hashedRecoveryCode;
    }

    public void setHashedRecoveryCode(String hashedRecoveryCode) {
        this.hashedRecoveryCode = hashedRecoveryCode;
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

    public boolean isUsed() {
        return usedAt != null;
    }
}
