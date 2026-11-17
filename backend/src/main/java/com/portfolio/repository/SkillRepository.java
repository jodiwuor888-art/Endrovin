package com.portfolio.repository;

import com.portfolio.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findAllByStatusOrderByDisplayOrderAsc(String status);
    List<Skill> findAllByOrderByDisplayOrderAsc();
    List<Skill> findByCategoryIdAndStatusOrderByDisplayOrderAsc(Long categoryId, String status);
    long countByStatus(String status);
}
