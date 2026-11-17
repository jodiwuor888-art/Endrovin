package com.portfolio.repository;

import com.portfolio.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
    Optional<UploadedFile> findByStoredFileName(String storedFileName);
    List<UploadedFile> findByFileCategoryOrderByUploadedAtDesc(String fileCategory);
}
