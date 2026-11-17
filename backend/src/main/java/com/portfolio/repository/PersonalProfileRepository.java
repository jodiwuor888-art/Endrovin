package com.portfolio.repository;

import com.portfolio.entity.PersonalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonalProfileRepository extends JpaRepository<PersonalProfile, Long> {
    Optional<PersonalProfile> findFirstByActiveTrueOrderByIdAsc();
}
