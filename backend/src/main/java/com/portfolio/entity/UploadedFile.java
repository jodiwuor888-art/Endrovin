package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "UploadedFiles")
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FileID")
    private Long id;

    @Column(name = "OriginalFileName", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "StoredFileName", nullable = false, unique = true, length = 255)
    private String storedFileName;

    @Column(name = "FileType", nullable = false, length = 100)
    private String fileType;

    @Column(name = "FileCategory", nullable = false, length = 50)
    private String fileCategory; // PROFILE_PHOTO, CV, PROJECT_IMAGE, CERTIFICATE

    @Column(name = "FileSize", nullable = false)
    private Long fileSize;

    @Column(name = "FileExtension", nullable = false, length = 20)
    private String fileExtension;

    @Column(name = "StoragePath", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "UploadedBy", nullable = false, length = 50)
    private String uploadedBy;

    @Column(name = "UploadedAt", nullable = false, updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    public UploadedFile() {
    }

    public UploadedFile(String originalFileName, String storedFileName, String fileType, String fileCategory, Long fileSize, String fileExtension, String storagePath, String uploadedBy) {
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.fileType = fileType;
        this.fileCategory = fileCategory;
        this.fileSize = fileSize;
        this.fileExtension = fileExtension;
        this.storagePath = storagePath;
        this.uploadedBy = uploadedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileCategory() {
        return fileCategory;
    }

    public void setFileCategory(String fileCategory) {
        this.fileCategory = fileCategory;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
