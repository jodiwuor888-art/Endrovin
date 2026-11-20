package com.portfolio.repository;

import com.portfolio.entity.DataSubjectRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataSubjectRequestRepository extends JpaRepository<DataSubjectRequest, Long> {
    Optional<DataSubjectRequest> findByTicketNumber(String ticketNumber);
    List<DataSubjectRequest> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
}
