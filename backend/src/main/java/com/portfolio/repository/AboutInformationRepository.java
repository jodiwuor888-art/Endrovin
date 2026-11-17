package com.portfolio.repository;

import com.portfolio.entity.AboutInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AboutInformationRepository extends JpaRepository<AboutInformation, Long> {
    Optional<AboutInformation> findFirstByStatusOrderByIdAsc(String status);
}
