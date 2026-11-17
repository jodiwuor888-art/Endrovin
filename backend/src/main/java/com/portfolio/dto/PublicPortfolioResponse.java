package com.portfolio.dto;

import java.util.ArrayList;
import java.util.List;

public class PublicPortfolioResponse {
    private PersonalProfileDto profile;
    private AboutInformationDto about;
    private List<SkillCategoryDto> skillCategories = new ArrayList<>();
    private List<SkillDto> skills = new ArrayList<>();
    private List<ServiceDto> services = new ArrayList<>();
    private List<ProjectDto> projects = new ArrayList<>();
    private List<EducationDto> education = new ArrayList<>();
    private List<ExperienceDto> experience = new ArrayList<>();
    private List<CertificateDto> certificates = new ArrayList<>();
    private List<AchievementDto> achievements = new ArrayList<>();
    private List<TestimonialDto> testimonials = new ArrayList<>();
    private List<SocialLinkDto> socialLinks = new ArrayList<>();

    public PublicPortfolioResponse() {
    }

    public PersonalProfileDto getProfile() {
        return profile;
    }

    public void setProfile(PersonalProfileDto profile) {
        this.profile = profile;
    }

    public AboutInformationDto getAbout() {
        return about;
    }

    public void setAbout(AboutInformationDto about) {
        this.about = about;
    }

    public List<SkillCategoryDto> getSkillCategories() {
        return skillCategories;
    }

    public void setSkillCategories(List<SkillCategoryDto> skillCategories) {
        this.skillCategories = skillCategories;
    }

    public List<SkillDto> getSkills() {
        return skills;
    }

    public void setSkills(List<SkillDto> skills) {
        this.skills = skills;
    }

    public List<ServiceDto> getServices() {
        return services;
    }

    public void setServices(List<ServiceDto> services) {
        this.services = services;
    }

    public List<ProjectDto> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectDto> projects) {
        this.projects = projects;
    }

    public List<EducationDto> getEducation() {
        return education;
    }

    public void setEducation(List<EducationDto> education) {
        this.education = education;
    }

    public List<ExperienceDto> getExperience() {
        return experience;
    }

    public void setExperience(List<ExperienceDto> experience) {
        this.experience = experience;
    }

    public List<CertificateDto> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<CertificateDto> certificates) {
        this.certificates = certificates;
    }

    public List<AchievementDto> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<AchievementDto> achievements) {
        this.achievements = achievements;
    }

    public List<TestimonialDto> getTestimonials() {
        return testimonials;
    }

    public void setTestimonials(List<TestimonialDto> testimonials) {
        this.testimonials = testimonials;
    }

    public List<SocialLinkDto> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(List<SocialLinkDto> socialLinks) {
        this.socialLinks = socialLinks;
    }
}
