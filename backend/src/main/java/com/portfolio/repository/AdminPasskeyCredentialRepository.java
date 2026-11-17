package com.portfolio.repository;

import com.portfolio.entity.Admin;
import com.portfolio.entity.AdminPasskeyCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminPasskeyCredentialRepository extends JpaRepository<AdminPasskeyCredential, String> {
    List<AdminPasskeyCredential> findByAdminAndRevokedAtIsNull(Admin admin);
    List<AdminPasskeyCredential> findByAdminIdAndRevokedAtIsNull(Long adminId);
    Optional<AdminPasskeyCredential> findByCredentialIdAndRevokedAtIsNull(String credentialId);
    long countByAdminAndRevokedAtIsNull(Admin admin);
    long countByAdminIdAndRevokedAtIsNull(Long adminId);
}
