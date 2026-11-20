package com.portfolio.repository;

import com.portfolio.entity.LegalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LegalDocumentRepository extends JpaRepository<LegalDocument, Long> {
    List<LegalDocument> findAllByOrderByUpdatedAtDesc();
    List<LegalDocument> findByDocumentTypeOrderByVersionDesc(String documentType);
    Optional<LegalDocument> findFirstByDocumentTypeAndStatusOrderByEffectiveDateDesc(String documentType, String status);
}
