package com.portfolio.repository;

import com.portfolio.entity.ThirdPartyProcessor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThirdPartyProcessorRepository extends JpaRepository<ThirdPartyProcessor, Long> {
    List<ThirdPartyProcessor> findAllByOrderByProviderNameAsc();
    List<ThirdPartyProcessor> findByEnabledTrue();
}
