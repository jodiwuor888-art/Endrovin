package com.portfolio.dto;

import jakarta.validation.constraints.NotBlank;

public class EducationDto {
    private Long id;

    @NotBlank(message = "Degree / Diploma is required")
    private String degreeOrDiploma;

    @NotBlank(message = "Field of Study is required")
    private String fieldOfStudy;

    @NotBlank(message = "Institution Name is required")
    private String institutionName;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Start Date is required")
    private String startDate;

    private String endDate;
    private boolean current = false;
    private String gradeOrGpa;
    private String description;
    private int displayOrder = 0;
    private String status = "PUBLISHED";

    public EducationDto() {
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
}
