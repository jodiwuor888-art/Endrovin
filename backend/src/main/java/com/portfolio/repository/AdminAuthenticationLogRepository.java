package com.portfolio.repository;

import com.portfolio.entity.AdminAuthenticationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminAuthenticationLogRepository extends JpaRepository<AdminAuthenticationLog, Long> {
    List<AdminAuthenticationLog> findByAdminIdOrderByEventTimestampDesc(Long adminId);
    Page<AdminAuthenticationLog> findAllByOrderByEventTimestampDesc(Pageable pageable);
    List<AdminAuthenticationLog> findTop20ByOrderByEventTimestampDesc();
}
