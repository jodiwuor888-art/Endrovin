package com.portfolio.repository;

import com.portfolio.entity.Admin;
import com.portfolio.entity.AdminDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminDeviceRepository extends JpaRepository<AdminDevice, Long> {
    List<AdminDevice> findByAdminAndRevokedAtIsNullOrderByRegisteredAtDesc(Admin admin);
    List<AdminDevice> findByAdminIdAndRevokedAtIsNullOrderByRegisteredAtDesc(Long adminId);
    Optional<AdminDevice> findByCredential_CredentialId(String credentialId);
    Optional<AdminDevice> findByIdAndAdmin(Long id, Admin admin);
    long countByAdminAndRevokedAtIsNull(Admin admin);
}
