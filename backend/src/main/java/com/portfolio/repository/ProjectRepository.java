package com.portfolio.repository;

import com.portfolio.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByStatusOrderByDisplayOrderAsc(String status);
    List<Project> findByFeaturedTrueAndStatusOrderByDisplayOrderAsc(String status);
    List<Project> findByCategoryAndStatusOrderByDisplayOrderAsc(String category, String status);
    Optional<Project> findBySlugAndStatus(String slug, String status);
    Optional<Project> findBySlug(String slug);
    List<Project> findAllByOrderByDisplayOrderAsc();
    long countByStatus(String status);
}
