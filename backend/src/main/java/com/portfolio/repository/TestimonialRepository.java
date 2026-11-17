package com.portfolio.repository;

import com.portfolio.entity.Testimonial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {

    // Public website query: only APPROVED recommendations with consentToPublish = true
    List<Testimonial> findByStatusAndConsentToPublishTrueOrderByDisplayOrderAscSubmittedAtDesc(String status);

    // Filter by status
    List<Testimonial> findByStatusOrderBySubmittedAtDesc(String status);

    Page<Testimonial> findByStatusOrderBySubmittedAtDesc(String status, Pageable pageable);

    // Search by sender name or organization
    @Query("SELECT t FROM Testimonial t WHERE LOWER(t.fullName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(t.organization) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY t.submittedAt DESC")
    List<Testimonial> searchTestimonials(@Param("q") String query);

    long countByStatus(String status);

    // Backward-compatible methods
    default List<Testimonial> findByApprovalStatusOrderByDisplayOrderAsc(String approvalStatus) {
        return findByStatusAndConsentToPublishTrueOrderByDisplayOrderAscSubmittedAtDesc(approvalStatus);
    }

    default List<Testimonial> findAllByOrderByCreatedAtDesc() {
        return findByStatusOrderBySubmittedAtDesc("ALL");
    }

    default long countByApprovalStatus(String approvalStatus) {
        return countByStatus(approvalStatus);
    }
}
