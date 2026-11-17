package com.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Education")
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EducationID")
    private Long id;

    @Column(name = "DegreeOrDiploma", nullable = false, length = 150)
    private String degreeOrDiploma;

    @Column(name = "FieldOfStudy", nullable = false, length = 150)
    private String fieldOfStudy;

    @Column(name = "InstitutionName", nullable = false, length = 200)
    private String institutionName;

    @Column(name = "Location", nullable = false, length = 100)
    private String location;

    @Column(name = "StartDate", nullable = false, length = 50)
    private String startDate;

    @Column(name = "EndDate", length = 50)
    private String endDate;

    @Column(name = "IsCurrent", nullable = false)
    private boolean current = false;

    @Column(name = "GradeOrGpa", length = 50)
    private String gradeOrGpa;

    @Lob
    @Column(name = "Description")
    private String description;

    @Column(name = "DisplayOrder", nullable = false)
    private int displayOrder = 0;

    @Column(name = "Status", nullable = false, length = 20)
    private String status = "PUBLISHED";

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Education() {
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDegreeOrDiploma() {
        return degreeOrDiploma;
    }

    public void setDegreeOrDiploma(String degreeOrDiploma) {
        this.degreeOrDiploma = degreeOrDiploma;
    }

    public String getFieldOfStudy() {
        return fieldOfStudy;
    }

    public void setFieldOfStudy(String fieldOfStudy) {
        this.fieldOfStudy = fieldOfStudy;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public String getGradeOrGpa() {
        return gradeOrGpa;
    }

    public void setGradeOrGpa(String gradeOrGpa) {
        this.gradeOrGpa = gradeOrGpa;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
