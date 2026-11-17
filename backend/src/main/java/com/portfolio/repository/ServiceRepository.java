package com.portfolio.repository;

import com.portfolio.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findAllByStatusOrderByDisplayOrderAsc(String status);
    List<ServiceEntity> findAllByOrderByDisplayOrderAsc();
    long countByStatus(String status);
}
