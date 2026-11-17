package com.portfolio.repository;

import com.portfolio.entity.Admin;
import com.portfolio.entity.AdminRecoveryCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminRecoveryCodeRepository extends JpaRepository<AdminRecoveryCode, Long> {
    List<AdminRecoveryCode> findByAdminAndUsedAtIsNull(Admin admin);
    List<AdminRecoveryCode> findByAdminIdAndUsedAtIsNull(Long adminId);
    void deleteByAdmin(Admin admin);
    long countByAdminAndUsedAtIsNull(Admin admin);
}
