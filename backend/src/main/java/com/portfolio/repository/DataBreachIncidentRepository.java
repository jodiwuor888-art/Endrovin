package com.portfolio.repository;

import com.portfolio.entity.DataBreachIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataBreachIncidentRepository extends JpaRepository<DataBreachIncident, Long> {
    List<DataBreachIncident> findAllByOrderByDiscoveryDateDesc();
    long countByStatus(String status);
}
