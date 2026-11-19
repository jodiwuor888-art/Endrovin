package com.portfolio.service;

import com.portfolio.dto.*;
import com.portfolio.entity.*;
import com.portfolio.exception.BadRequestException;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("null")
public class AdminManagementService {

    private final PersonalProfileRepository profileRepository;
    private final AboutInformationRepository aboutRepository;
    private final ProjectRepository projectRepository;
    private final SkillCategoryRepository categoryRepository;
    private final SkillRepository skillRepository;
    private final ServiceRepository serviceRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final CertificateRepository certificateRepository;
    private final AchievementRepository achievementRepository;
    private final TestimonialRepository testimonialRepository;
    private final ContactMessageRepository messageRepository;
    private final SocialLinkRepository socialLinkRepository;
    private final WebsiteSettingRepository settingRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;

    public AdminManagementService(PersonalProfileRepository profileRepository,
                                  AboutInformationRepository aboutRepository,
                                  ProjectRepository projectRepository,
                                  SkillCategoryRepository categoryRepository,
                                  SkillRepository skillRepository,
                                  ServiceRepository serviceRepository,
                                  EducationRepository educationRepository,
                                  ExperienceRepository experienceRepository,
                                  CertificateRepository certificateRepository,
                                  AchievementRepository achievementRepository,
                                  TestimonialRepository testimonialRepository,
                                  ContactMessageRepository messageRepository,
                                  SocialLinkRepository socialLinkRepository,
                                  WebsiteSettingRepository settingRepository,
                                  AuditLogRepository auditLogRepository,
                                  AuditLogService auditLogService) {
        this.profileRepository = profileRepository;
        this.aboutRepository = aboutRepository;
        this.projectRepository = projectRepository;
        this.categoryRepository = categoryRepository;
        this.skillRepository = skillRepository;
        this.serviceRepository = serviceRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.certificateRepository = certificateRepository;
        this.achievementRepository = achievementRepository;
        this.testimonialRepository = testimonialRepository;
        this.messageRepository = messageRepository;
        this.socialLinkRepository = socialLinkRepository;
        this.settingRepository = settingRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditLogService = auditLogService;
    }

    // ==========================================
    // 1. PERSONAL PROFILE
    // ==========================================
    @Transactional(readOnly = true)
    public PersonalProfileDto getProfile() {
        return profileRepository.findFirstByActiveTrueOrderByIdAsc()
                .map(this::mapToProfileDto)
                .orElse(null);
    }

    public PersonalProfileDto saveProfile(PersonalProfileDto dto, String username, String ip) {
        PersonalProfile profile = profileRepository.findFirstByActiveTrueOrderByIdAsc()
                .orElse(new PersonalProfile());

        profile.setFullName(dto.getFullName());
        profile.setProfessionalHeadline(dto.getProfessionalHeadline());
        profile.setProfessionalTitle(dto.getProfessionalTitle());
        profile.setBioSummary(dto.getBioSummary());
        profile.setLocation(dto.getLocation());
        profile.setEmail(dto.getEmail());
        profile.setPhone(dto.getPhone());
        profile.setGitHubUrl(dto.getGitHubUrl());
        profile.setLinkedInUrl(dto.getLinkedInUrl());
        profile.setFacebookUrl(dto.getFacebookUrl());
        profile.setTwitterUrl(dto.getTwitterUrl());
        if (dto.getProfilePhotoUrl() != null && !dto.getProfilePhotoUrl().isBlank()) {
            profile.setProfilePhotoUrl(dto.getProfilePhotoUrl());
        }
        if (dto.getCvFileUrl() != null && !dto.getCvFileUrl().isBlank()) {
            profile.setCvFileUrl(dto.getCvFileUrl());
        }
        profile.setActive(dto.isActive());

        PersonalProfile saved = profileRepository.save(profile);
        auditLogService.logAction(username, "SAVE_PROFILE", "PersonalProfile", String.valueOf(saved.getId()), "Updated personal profile", ip);
        return mapToProfileDto(saved);
    }

    private PersonalProfileDto mapToProfileDto(PersonalProfile p) {
        PersonalProfileDto d = new PersonalProfileDto();
        d.setId(p.getId());
        d.setFullName(p.getFullName());
        d.setProfessionalHeadline(p.getProfessionalHeadline());
        d.setProfessionalTitle(p.getProfessionalTitle());
        d.setBioSummary(p.getBioSummary());
        d.setLocation(p.getLocation());
        d.setEmail(p.getEmail());
        d.setPhone(p.getPhone());
        d.setGitHubUrl(p.getGitHubUrl());
        d.setLinkedInUrl(p.getLinkedInUrl());
        d.setFacebookUrl(p.getFacebookUrl());
        d.setTwitterUrl(p.getTwitterUrl());
        d.setProfilePhotoUrl(p.getProfilePhotoUrl());
        d.setCvFileUrl(p.getCvFileUrl());
        d.setActive(p.isActive());
        return d;
    }

    // ==========================================
    // 2. ABOUT INFORMATION
    // ==========================================
    @Transactional(readOnly = true)
    public AboutInformationDto getAbout() {
        return aboutRepository.findFirstByStatusOrderByIdAsc("PUBLISHED")
                .or(() -> aboutRepository.findAll().stream().findFirst())
                .map(this::mapToAboutDto)
                .orElse(null);
    }

    public AboutInformationDto saveAbout(AboutInformationDto dto, String username, String ip) {
        AboutInformation about = aboutRepository.findAll().stream().findFirst()
                .orElse(new AboutInformation());

        about.setSectionTitle(dto.getSectionTitle());
        about.setSubtitle(dto.getSubtitle());
        about.setParagraph1(dto.getParagraph1());
        about.setParagraph2(dto.getParagraph2());
        about.setParagraph3(dto.getParagraph3());
        about.setYearsExperience(dto.getYearsExperience());
        about.setProjectsCompleted(dto.getProjectsCompleted());
        about.setSatisfiedClients(dto.getSatisfiedClients());
        about.setStatus(dto.getStatus() != null ? dto.getStatus() : "PUBLISHED");

        AboutInformation saved = aboutRepository.save(about);
        auditLogService.logAction(username, "SAVE_ABOUT", "AboutInformation", String.valueOf(saved.getId()), "Updated about section", ip);
        return mapToAboutDto(saved);
    }

    private AboutInformationDto mapToAboutDto(AboutInformation a) {
        AboutInformationDto d = new AboutInformationDto();
        d.setId(a.getId());
        d.setSectionTitle(a.getSectionTitle());
        d.setSubtitle(a.getSubtitle());
        d.setParagraph1(a.getParagraph1());
        d.setParagraph2(a.getParagraph2());
        d.setParagraph3(a.getParagraph3());
        d.setYearsExperience(a.getYearsExperience());
        d.setProjectsCompleted(a.getProjectsCompleted());
        d.setSatisfiedClients(a.getSatisfiedClients());
        d.setStatus(a.getStatus());
        return d;
    }

    // ==========================================
    // 3. PROJECTS CRUD
    // ==========================================
    @Transactional(readOnly = true)
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::mapToProjectDto)
                .collect(Collectors.toList());
    }

    public ProjectDto createProject(ProjectDto dto, String username, String ip) {
        Project p = new Project();
        applyProjectDto(p, dto);
        if (p.getSlug() == null || p.getSlug().isBlank()) {
            p.setSlug(generateSlug(p.getTitle()));
        }
        Project saved = projectRepository.save(java.util.Objects.requireNonNull(p, "project must not be null"));
        auditLogService.logAction(username, "CREATE_PROJECT", "Projects", String.valueOf(saved.getId()), "Created project: " + saved.getTitle(), ip);
        return mapToProjectDto(saved);
    }

    public ProjectDto updateProject(@org.springframework.lang.NonNull Long id, ProjectDto dto, String username, String ip) {
         Project p = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        applyProjectDto(p, dto);
        Project saved = projectRepository.save(java.util.Objects.requireNonNull(p, "project must not be null"));
        auditLogService.logAction(username, "UPDATE_PROJECT", "Projects", String.valueOf(saved.getId()), "Updated project: " + saved.getTitle(), ip);
        return mapToProjectDto(saved);
    }

    public void deleteProject(@org.springframework.lang.NonNull Long id, String username, String ip) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectRepository.deleteById(id);
        auditLogService.logAction(username, "DELETE_PROJECT", "Projects", String.valueOf(id), "Deleted project: " + p.getTitle(), ip);
    }

    private void applyProjectDto(Project p, ProjectDto d) {
        p.setTitle(d.getTitle());
        if (d.getSlug() != null && !d.getSlug().isBlank()) {
            p.setSlug(d.getSlug());
        }
        p.setCategory(d.getCategory());
        p.setShortDescription(d.getShortDescription());
        p.setDetailedDescription(d.getDetailedDescription());
        p.setTechnologies(d.getTechnologies());
        if (d.getFeaturedImageUrl() != null) p.setFeaturedImageUrl(d.getFeaturedImageUrl());
        p.setProjectLink(d.getProjectLink());
        p.setGithubUrl(d.getGithubUrl());
        p.setFeatured(d.isFeatured());
        p.setDisplayOrder(d.getDisplayOrder());
        p.setStatus(d.getStatus() != null ? d.getStatus() : "PUBLISHED");
        if (d.getSeoTitle() != null) p.setSeoTitle(d.getSeoTitle());
        if (d.getSeoDescription() != null) p.setSeoDescription(d.getSeoDescription());
        if (d.getViewCount() > 0) p.setViewCount(d.getViewCount());
    }

    private ProjectDto mapToProjectDto(Project p) {
        ProjectDto d = new ProjectDto();
        d.setId(p.getId());
        d.setTitle(p.getTitle());
        d.setSlug(p.getSlug());
        d.setCategory(p.getCategory());
        d.setShortDescription(p.getShortDescription());
        d.setDetailedDescription(p.getDetailedDescription());
        d.setTechnologies(p.getTechnologies());
        d.setFeaturedImageUrl(p.getFeaturedImageUrl());
        d.setProjectLink(p.getProjectLink());
        d.setGithubUrl(p.getGithubUrl());
        d.setFeatured(p.isFeatured());
        d.setDisplayOrder(p.getDisplayOrder());
        d.setStatus(p.getStatus());
        d.setSeoTitle(p.getSeoTitle());
        d.setSeoDescription(p.getSeoDescription());
        d.setViewCount(p.getViewCount());
        return d;
    }

    private String generateSlug(String title) {
        String base = title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        return base + "-" + System.currentTimeMillis() % 10000;
    }

    // ==========================================
    // 4. SKILL CATEGORIES & SKILLS CRUD
    // ==========================================
    @Transactional(readOnly = true)
    public List<SkillCategoryDto> getAllCategoriesWithSkills() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc().stream().map(cat -> {
            SkillCategoryDto d = new SkillCategoryDto();
            d.setId(cat.getId());
            d.setCategoryName(cat.getCategoryName());
            d.setIconClass(cat.getIconClass());
            d.setDescription(cat.getDescription());
            d.setDisplayOrder(cat.getDisplayOrder());

            List<SkillDto> skills = skillRepository.findByCategoryIdAndStatusOrderByDisplayOrderAsc(cat.getId(), "PUBLISHED").stream().map(s -> {
                SkillDto sd = new SkillDto();
                sd.setId(s.getId());
                sd.setCategoryId(cat.getId());
                sd.setCategoryName(cat.getCategoryName());
                sd.setSkillName(s.getSkillName());
                sd.setProficiencyPercentage(s.getProficiencyPercentage());
                sd.setProficiencyLevel(s.getProficiencyLevel());
                sd.setIconClass(s.getIconClass());
                sd.setDisplayOrder(s.getDisplayOrder());
                sd.setStatus(s.getStatus());
                return sd;
            }).collect(Collectors.toList());

            d.setSkills(skills);
            return d;
        }).collect(Collectors.toList());
    }

    public SkillCategoryDto createCategory(SkillCategoryDto dto, String username, String ip) {
        SkillCategory cat = new SkillCategory(dto.getCategoryName(), dto.getIconClass(), dto.getDescription(), dto.getDisplayOrder());
        SkillCategory saved = categoryRepository.save(cat);
        auditLogService.logAction(username, "CREATE_CATEGORY", "SkillCategories", String.valueOf(saved.getId()), "Created skill category: " + saved.getCategoryName(), ip);
        dto.setId(saved.getId());
        return dto;
    }

    public void deleteCategory(Long id, String username, String ip) {
        if (id == null) {
            throw new ResourceNotFoundException("Skill category not found: null");
        }
        SkillCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill category not found: " + id));
        categoryRepository.deleteById(id);
        auditLogService.logAction(username, "DELETE_CATEGORY", "SkillCategories", String.valueOf(id), "Deleted skill category: " + cat.getCategoryName(), ip);
    }

    @Transactional(readOnly = true)
    public List<SkillDto> getAllSkills() {
        return skillRepository.findAllByOrderByDisplayOrderAsc().stream().map(s -> {
            SkillDto d = new SkillDto();
            d.setId(s.getId());
            d.setCategoryId(s.getCategory().getId());
            d.setCategoryName(s.getCategory().getCategoryName());
            d.setSkillName(s.getSkillName());
            d.setProficiencyPercentage(s.getProficiencyPercentage());
            d.setProficiencyLevel(s.getProficiencyLevel());
            d.setIconClass(s.getIconClass());
            d.setDisplayOrder(s.getDisplayOrder());
            d.setStatus(s.getStatus());
            return d;
        }).collect(Collectors.toList());
    }

    public SkillDto createSkill(SkillDto dto, String username, String ip) {
        Long categoryId = dto.getCategoryId();
        if (categoryId == null) {
            throw new ResourceNotFoundException("Category not found: null");
        }
        SkillCategory cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));

        Skill s = new Skill();
        s.setCategory(cat);
        s.setSkillName(dto.getSkillName());
        s.setProficiencyPercentage(dto.getProficiencyPercentage());
        s.setProficiencyLevel(dto.getProficiencyLevel());
        s.setIconClass(dto.getIconClass());
        s.setDisplayOrder(dto.getDisplayOrder());
        s.setStatus(dto.getStatus() != null ? dto.getStatus() : "PUBLISHED");

        Skill saved = skillRepository.save(s);
        auditLogService.logAction(username, "CREATE_SKILL", "Skills", String.valueOf(saved.getId()), "Created skill: " + saved.getSkillName(), ip);
        dto.setId(saved.getId());
        dto.setCategoryName(cat.getCategoryName());
        return dto;
    }

    public SkillDto updateSkill(@NonNull Long id, SkillDto dto, String username, String ip) {
        Skill s = skillRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + id));

        Long categoryId = dto.getCategoryId();
        if (categoryId != null && !categoryId.equals(s.getCategory().getId())) {
            SkillCategory cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
            s.setCategory(cat);
        }

        s.setSkillName(dto.getSkillName());
        s.setProficiencyPercentage(dto.getProficiencyPercentage());
        s.setProficiencyLevel(dto.getProficiencyLevel());
        s.setIconClass(dto.getIconClass());
        s.setDisplayOrder(dto.getDisplayOrder());
        s.setStatus(dto.getStatus() != null ? dto.getStatus() : "PUBLISHED");

        Skill saved = skillRepository.save(s);
        auditLogService.logAction(username, "UPDATE_SKILL", "Skills", String.valueOf(saved.getId()), "Updated skill: " + saved.getSkillName(), ip);
        dto.setId(saved.getId());
        dto.setCategoryName(s.getCategory().getCategoryName());
        return dto;
    }

    public void deleteSkill(@NonNull Long id, String username, String ip) {
        Skill s = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + id));
        skillRepository.deleteById(id);
        auditLogService.logAction(username, "DELETE_SKILL", "Skills", String.valueOf(id), "Deleted skill: " + s.getSkillName(), ip);
    }

    // ==========================================
    // 5. SERVICES CRUD
    // ==========================================
    @Transactional(readOnly = true)
    public List<ServiceDto> getAllServices() {
        return serviceRepository.findAllByOrderByDisplayOrderAsc().stream().map(s -> {
            ServiceDto d = new ServiceDto();
            d.setId(s.getId());
            d.setTitle(s.getTitle());
            d.setShortDescription(s.getShortDescription());
            d.setDetailedDescription(s.getDetailedDescription());
            d.setIconClass(s.getIconClass());
            d.setDisplayOrder(s.getDisplayOrder());
            d.setStatus(s.getStatus());
            return d;
        }).collect(Collectors.toList());
    }

    public ServiceDto createService(ServiceDto dto, String username, String ip) {
        ServiceEntity s = new ServiceEntity();
        s.setTitle(dto.getTitle());
        s.setShortDescription(dto.getShortDescription());
        s.setDetailedDescription(dto.getDetailedDescription());
        s.setIconClass(dto.getIconClass());
        s.setDisplayOrder(dto.getDisplayOrder());
        s.setStatus(dto.getStatus() != null ? dto.getStatus() : "PUBLISHED");

        ServiceEntity saved = serviceRepository.save(s);
        auditLogService.logAction(username, "CREATE_SERVICE", "Services", String.valueOf(saved.getId()), "Created service: " + saved.getTitle(), ip);
        dto.setId(saved.getId());
        return dto;
    }

    public ServiceDto updateService(@NonNull Long id, ServiceDto dto, String username, String ip) {
        ServiceEntity s = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));
        s.setTitle(dto.getTitle());
        s.setShortDescription(dto.getShortDescription());
        s.setDetailedDescription(dto.getDetailedDescription());
        s.setIconClass(dto.getIconClass());
        s.setDisplayOrder(dto.getDisplayOrder());
        s.setStatus(dto.getStatus() != null ? dto.getStatus() : "PUBLISHED");

        ServiceEntity saved = serviceRepository.save(s);
        auditLogService.logAction(username, "UPDATE_SERVICE", "Services", String.valueOf(saved.getId()), "Updated service: " + saved.getTitle(), ip);
        dto.setId(saved.getId());
        return dto;
    }

    public void deleteService(@NonNull Long id, String username, String ip) {
        ServiceEntity s = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));
        serviceRepository.deleteById(id);
        auditLogService.logAction(username, "DELETE_SERVICE", "Services", String.valueOf(id), "Deleted service: " + s.getTitle(), ip);
    }

    // ==========================================
    // 6. EDUCATION CRUD
    // ==========================================
    @Transactional(readOnly = true)
    public List<EducationDto> getAllEducation() {
        return educationRepository.findAllByOrderByDisplayOrderAsc().stream().map(e -> {
            EducationDto d = new EducationDto();
            d.setId(e.getId());
            d.setDegreeOrDiploma(e.getDegreeOrDiploma());
            d.setFieldOfStudy(e.getFieldOfStudy());
            d.setInstitutionName(e.getInstitutionName());
            d.setLocation(e.getLocation());
            d.setStartDate(e.getStartDate());
            d.setEndDate(e.getEndDate());
            d.setCurrent(e.isCurrent());
            d.setGradeOrGpa(e.getGradeOrGpa());
            d.setDescription(e.getDescription());
            d.setDisplayOrder(e.getDisplayOrder());
            d.setStatus(e.getStatus());
            return d;
        }).collect(Collectors.toList());
    }

    public EducationDto createEducation(EducationDto dto, String username, String ip) {
        Education e = new Education();
        applyEducationDto(e, dto);
        Education saved = educationRepository.save(java.util.Objects.requireNonNull(e, "education must not be null"));
        auditLogService.logAction(username, "CREATE_EDUCATION", "Education", String.valueOf(saved.getId()), "Added education: " + saved.getDegreeOrDiploma(), ip);
        dto.setId(saved.getId());
        return dto;
    }

    public EducationDto updateEducation(@NonNull Long id, EducationDto dto, String username, String ip) {
        Education e = educationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Education not found: " + id));
        applyEducationDto(e, dto);
        Education saved = educationRepository.save(java.util.Objects.requireNonNull(e, "education must not be null"));
        auditLogService.logAction(username, "UPDATE_EDUCATION", "Education", String.valueOf(saved.getId()), "Updated education: " + saved.getDegreeOrDiploma(), ip);
        dto.setId(saved.getId());
        return dto;
    }

    public void deleteEducation(@NonNull Long id, String username, String ip) {
        Education e = educationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Education not found: " + id));
        educationRepository.deleteById(id);
        auditLogService.logAction(username, "DELETE_EDUCATION", "Education", String.valueOf(id), "Deleted education: " + e.getDegreeOrDiploma(), ip);
    }

    private void applyEducationDto(Education e, EducationDto d) {
        e.setDegreeOrDiploma(d.getDegreeOrDiploma());
        e.setFieldOfStudy(d.getFieldOfStudy());
        e.setInstitutionName(d.getInstitutionName());
        e.setLocation(d.getLocation());
        e.setStartDate(d.getStartDate());
        e.setEndDate(d.getEndDate());
        e.setCurrent(d.isCurrent());
        e.setGradeOrGpa(d.getGradeOrGpa());
        e.setDescription(d.getDescription());
        e.setDisplayOrder(d.getDisplayOrder());
        e.setStatus(d.getStatus() != null ? d.getStatus() : "PUBLISHED");
    }

    // ==========================================
    // 7. EXPERIENCE CRUD
    // ==========================================
    @Transactional(readOnly = true)
    public List<ExperienceDto> getAllExperience() {
        return experienceRepository.findAllByOrderByDisplayOrderAsc().stream().map(e -> {
            ExperienceDto d = new ExperienceDto();
            d.setId(e.getId());
            d.setJobTitle(e.getJobTitle());
            d.setCompanyName(e.getCompanyName());
            d.setLocation(e.getLocation());
            d.setEmploymentType(e.getEmploymentType());
            d.setStartDate(e.getStartDate());
            d.setEndDate(e.getEndDate());
            d.setCurrentRole(e.isCurrentRole());
            d.setResponsibilities(e.getResponsibilities());
            d.setTechnologiesUsed(e.getTechnologiesUsed());
            d.setDisplayOrder(e.getDisplayOrder());
            d.setStatus(e.getStatus());
            return d;
        }).collect(Collectors.toList());
    }

    public ExperienceDto createExperience(ExperienceDto dto, String username, String ip) {
        Experience e = new Experience();
        applyExperienceDto(e, dto);
        Experience saved = experienceRepository.save(e);
        auditLogService.logAction(username, "CREATE_EXPERIENCE", "Experience", String.valueOf(saved.getId()), "Added experience: " + saved.getJobTitle(), ip);
        dto.setId(saved.getId());
        return dto;
    }

    public ExperienceDto updateExperience(Long id, ExperienceDto dto, String username, String ip) {
        Long experienceId = java.util.Objects.requireNonNull(id, "Experience id must not be null");
        Experience e = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found: " + experienceId));
        applyExperienceDto(e, dto);
        Experience saved = experienceRepository.save(java.util.Objects.requireNonNull(e, "Experience must not be null"));
        auditLogService.logAction(username, "UPDATE_EXPERIENCE", "Experience", String.valueOf(saved.getId()), "Updated experience: " + saved.getJobTitle(), ip);
        dto.setId(saved.getId());
        return dto;
    }

    public void deleteExperience(Long id, String username, String ip) {
        Long experienceId = java.util.Objects.requireNonNull(id, "Experience id must not be null");
        Experience e = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found: " + experienceId));
        experienceRepository.delete(java.util.Objects.requireNonNull(e, "Experience must not be null"));
        auditLogService.logAction(username, "DELETE_EXPERIENCE", "Experience", String.valueOf(experienceId), "Deleted experience: " + e.getJobTitle(), ip);
    }

    private void applyExperienceDto(Experience e, ExperienceDto d) {
        e.setJobTitle(d.getJobTitle());
        e.setCompanyName(d.getCompanyName());
        e.setLocation(d.getLocation());
        e.setEmploymentType(d.getEmploymentType());
        e.setStartDate(d.getStartDate());
        e.setEndDate(d.getEndDate());
        e.setCurrentRole(d.isCurrentRole());
        e.setResponsibilities(d.getResponsibilities());
        e.setTechnologiesUsed(d.getTechnologiesUsed());
        e.setDisplayOrder(d.getDisplayOrder());
        e.setStatus(d.getStatus() != null ? d.getStatus() : "PUBLISHED");
    }

    // ==========================================
    // 8. CERTIFICATES CRUD
    // ==========================================
    @Transactional(readOnly = true)
    public List<CertificateDto> getAllCertificates() {
        return certificateRepository.findAllByOrderByDisplayOrderAsc().stream().map(c -> {
            CertificateDto d = new CertificateDto();
            d.setId(c.getId());
            d.setCertificateTitle(c.getCertificateTitle());
            d.setIssuingOrganization(c.getIssuingOrganization());
            d.setIssueDate(c.getIssueDate());
            d.setExpirationDate(c.getExpirationDate());
            d.setCredentialId(c.getCredentialId());
            d.setCredentialUrl(c.getCredentialUrl());
            d.setCertificateFileUrl(c.getCertificateFileUrl());
            d.setDisplayOrder(c.getDisplayOrder());
            d.setStatus(c.getStatus());
            return d;
        }).collect(Collectors.toList());
    }

    public CertificateDto createCertificate(CertificateDto dto, String username, String ip) {
        Certificate c = new Certificate();
        applyCertificateDto(c, dto);
        Certificate saved = certificateRepository.save(java.util.Objects.requireNonNull(c, "certificate must not be null"));
        auditLogService.logAction(username, "CREATE_CERTIFICATE", "Certificates", String.valueOf(saved.getId()), "Added certificate: " + saved.getCertificateTitle(), ip);
        dto.setId(saved.getId());
        return dto;
    }

    public CertificateDto updateCertificate(@NonNull Long id, CertificateDto dto, String username, String ip) {
        Certificate c = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + id));
        applyCertificateDto(c, dto);
        Certificate saved = certificateRepository.save(java.util.Objects.requireNonNull(c, "certificate must not be null"));
        auditLogService.logAction(username, "UPDATE_CERTIFICATE", "Certificates", String.valueOf(saved.getId()), "Updated certificate: " + saved.getCertificateTitle(), ip);
        dto.setId(saved.getId());
        return dto;
    }

    public void deleteCertificate(@NonNull Long id, String username, String ip) {
        Certificate c = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + id));
        certificateRepository.deleteById(id);
        auditLogService.logAction(username, "DELETE_CERTIFICATE", "Certificates", String.valueOf(id), "Deleted certificate: " + c.getCertificateTitle(), ip);
    }

    private void applyCertificateDto(Certificate c, CertificateDto d) {
        c.setCertificateTitle(d.getCertificateTitle());
        c.setIssuingOrganization(d.getIssuingOrganization());
        c.setIssueDate(d.getIssueDate());
        c.setExpirationDate(d.getExpirationDate());
        c.setCredentialId(d.getCredentialId());
        c.setCredentialUrl(d.getCredentialUrl());
        if (d.getCertificateFileUrl() != null) c.setCertificateFileUrl(d.getCertificateFileUrl());
        c.setDisplayOrder(d.getDisplayOrder());
        c.setStatus(d.getStatus() != null ? d.getStatus() : "PUBLISHED");
    }

    // ==========================================
    // 9. ACHIEVEMENTS CRUD
    // ==========================================
    @Transactional(readOnly = true)
    public List<AchievementDto> getAllAchievements() {
        return achievementRepository.findAllByOrderByDisplayOrderAsc().stream().map(a -> {
            AchievementDto d = new AchievementDto();
            d.setId(a.getId());
            d.setTitle(a.getTitle());
            d.setOrganization(a.getOrganization());
            d.setAchievementDate(a.getAchievementDate());
            d.setDescription(a.getDescription());
            d.setDisplayOrder(a.getDisplayOrder());
            d.setStatus(a.getStatus());
            return d;
        }).collect(Collectors.toList());
    }

    public AchievementDto createAchievement(AchievementDto dto, String username, String ip) {
        Achievement a = new Achievement();
        a.setTitle(dto.getTitle());
        a.setOrganization(dto.getOrganization());
        a.setAchievementDate(dto.getAchievementDate());
        a.setDescription(dto.getDescription());
        a.setDisplayOrder(dto.getDisplayOrder());
        a.setStatus(dto.getStatus() != null ? dto.getStatus() : "PUBLISHED");

        Achievement saved = achievementRepository.save(a);
        auditLogService.logAction(username, "CREATE_ACHIEVEMENT", "Achievements", String.valueOf(saved.getId()), "Added achievement: " + saved.getTitle(), ip);
        dto.setId(saved.getId());
        return dto;
    }

    public AchievementDto updateAchievement(Long id, AchievementDto dto, String username, String ip) {
        Long achievementId = java.util.Objects.requireNonNull(id, "Achievement id must not be null");
        Achievement a = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found: " + achievementId));
        a.setTitle(dto.getTitle());
        a.setOrganization(dto.getOrganization());
        a.setAchievementDate(dto.getAchievementDate());
        a.setDescription(dto.getDescription());
        a.setDisplayOrder(dto.getDisplayOrder());
        a.setStatus(dto.getStatus() != null ? dto.getStatus() : "PUBLISHED");

        Achievement saved = achievementRepository.save(a);
        auditLogService.logAction(username, "UPDATE_ACHIEVEMENT", "Achievements", String.valueOf(saved.getId()), "Updated achievement: " + saved.getTitle(), ip);
        dto.setId(saved.getId());
        return dto;
    }

    public void deleteAchievement(Long id, String username, String ip) {
        Long achievementId = java.util.Objects.requireNonNull(id, "Achievement id must not be null");
        Achievement a = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found: " + achievementId));
        achievementRepository.deleteById(achievementId);
        auditLogService.logAction(username, "DELETE_ACHIEVEMENT", "Achievements", String.valueOf(achievementId), "Deleted achievement: " + a.getTitle(), ip);
    }

    // ==========================================
    // 10. TESTIMONIALS MANAGEMENT & APPROVAL
    // ==========================================
    @Transactional(readOnly = true)
    public List<TestimonialDto> getAllTestimonials() {
        return getTestimonialsFiltered("ALL", null);
    }

    @Transactional(readOnly = true)
    public List<TestimonialDto> getTestimonialsFiltered(String status, String query) {
        List<Testimonial> list;
        if (query != null && !query.isBlank()) {
            list = testimonialRepository.searchTestimonials(query.trim());
            if (status != null && !"ALL".equalsIgnoreCase(status)) {
                list = list.stream().filter(t -> t.getStatus().equalsIgnoreCase(status)).collect(Collectors.toList());
            }
        } else if (status != null && !"ALL".equalsIgnoreCase(status)) {
            list = testimonialRepository.findByStatusOrderBySubmittedAtDesc(status.toUpperCase());
        } else {
            list = testimonialRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "submittedAt"));
        }

        return list.stream().map(this::mapToTestimonialDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TestimonialDto getTestimonialById(Long id) {
        Testimonial t = testimonialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial not found: " + id));
        return mapToTestimonialDto(t);
    }

    public TestimonialDto createTestimonial(TestimonialDto dto, String username, String ip) {
        Testimonial t = new Testimonial();
        t.setFullName(dto.getFullName() != null ? dto.getFullName() : dto.getClientName());
        t.setEmail(dto.getEmail() != null ? dto.getEmail() : "admin@portfolio.local");
        t.setOrganization(dto.getOrganization() != null ? dto.getOrganization() : dto.getClientCompany());
        t.setPosition(dto.getPosition() != null ? dto.getPosition() : dto.getClientTitle());
        t.setRelationship(dto.getRelationship() != null ? dto.getRelationship() : "Client");
        t.setMessage(dto.getMessage() != null ? dto.getMessage() : dto.getTestimonialText());
        t.setPhotoPath(dto.getPhotoPath() != null ? dto.getPhotoPath() : dto.getClientAvatarUrl());
        t.setOrganizationWebsite(dto.getOrganizationWebsite());
        t.setConsentToPublish(dto.isConsentToPublish());
        t.setRating(dto.getRating());
        t.setStatus(dto.getStatus() != null ? dto.getStatus() : "APPROVED");
        t.setDisplayOrder(dto.getDisplayOrder());
        t.setSubmittedAt(dto.getSubmittedAt() != null ? dto.getSubmittedAt() : LocalDateTime.now());

        Testimonial saved = testimonialRepository.save(t);
        auditLogService.logAction(username, "CREATE_TESTIMONIAL", "Testimonials", String.valueOf(saved.getId()), "Created testimonial for " + saved.getFullName(), ip);
        return mapToTestimonialDto(saved);
    }

    @CacheEvict(value = {"publicPortfolio", "publicTestimonials"}, allEntries = true)
    public TestimonialDto updateApprovalStatus(Long id, String status, String username, String ip) {
        Long testimonialId = java.util.Objects.requireNonNull(id, "Testimonial id must not be null");
        Testimonial t = testimonialRepository.findById(testimonialId)
            .orElseThrow(() -> new ResourceNotFoundException("Testimonial not found: " + testimonialId));

        String upperStatus = status.toUpperCase();
        if ("APPROVED".equals(upperStatus) && !t.isConsentToPublish()) {
            throw new BadRequestException("Cannot approve testimonial without visitor's publication consent.");
        }

        t.setStatus(upperStatus);
        t.setReviewedAt(LocalDateTime.now());
        Testimonial saved = testimonialRepository.save(t);
        auditLogService.logAction(username, "MODERATE_TESTIMONIAL", "Testimonials", String.valueOf(saved.getId()), "Moderated testimonial status to " + upperStatus, ip);

        return mapToTestimonialDto(saved);
    }

    @SuppressWarnings("null")
    @CacheEvict(value = {"publicPortfolio", "publicTestimonials"}, allEntries = true)
    public TestimonialDto rejectTestimonial(Long id, String reason, String username, String ip) {
        Long testimonialId = java.util.Objects.requireNonNull(id, "Testimonial id must not be null");
        Testimonial t = testimonialRepository.findById(testimonialId)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial not found: " + testimonialId));
        t.setStatus("REJECTED");
        t.setReviewedAt(LocalDateTime.now());
        t.setRejectionReason(reason);
        Testimonial saved = testimonialRepository.save(java.util.Objects.requireNonNull(t));
        auditLogService.logAction(username, "REJECT_TESTIMONIAL", "Testimonials", String.valueOf(saved.getId()), "Rejected testimonial. Reason: " + reason, ip);
        return mapToTestimonialDto(saved);
    }

    @SuppressWarnings("null")
    @CacheEvict(value = {"publicPortfolio", "publicTestimonials"}, allEntries = true)
    public void deleteTestimonial(Long id, String username, String ip) {
        Long testimonialId = java.util.Objects.requireNonNull(id, "Testimonial id must not be null");
        Testimonial t = testimonialRepository.findById(testimonialId)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial not found: " + testimonialId));
        testimonialRepository.delete(java.util.Objects.requireNonNull(t));
        auditLogService.logAction(username, "DELETE_TESTIMONIAL", "Testimonials", String.valueOf(testimonialId), "Deleted testimonial from " + t.getFullName(), ip);
    }

    private TestimonialDto mapToTestimonialDto(Testimonial t) {
        TestimonialDto d = new TestimonialDto();
        d.setId(t.getId());
        d.setFullName(t.getFullName());
        d.setClientName(t.getFullName());
        d.setEmail(t.getEmail());
        d.setPosition(t.getPosition());
        d.setClientTitle(t.getPosition());
        d.setOrganization(t.getOrganization());
        d.setClientCompany(t.getOrganization());
        d.setRelationship(t.getRelationship());
        d.setPhotoPath(t.getPhotoPath());
        d.setClientAvatarUrl(t.getPhotoPath());
        d.setMessage(t.getMessage());
        d.setTestimonialText(t.getMessage());
        d.setOrganizationWebsite(t.getOrganizationWebsite());
        d.setConsentToPublish(t.isConsentToPublish());
        d.setRating(t.getRating());
        d.setStatus(t.getStatus());
        d.setApprovalStatus(t.getStatus());
        d.setDisplayOrder(t.getDisplayOrder());
        d.setSubmittedAt(t.getSubmittedAt());
        d.setReviewedAt(t.getReviewedAt());
        d.setReviewedByAdminId(t.getReviewedByAdminId());
        d.setRejectionReason(t.getRejectionReason());
        d.setCreatedAt(t.getCreatedAt());
        return d;
    }

    // ==========================================
    // 11. CONTACT MESSAGES MANAGEMENT
    // ==========================================
    @Transactional(readOnly = true)
    public List<ContactMessageResponse> getAllMessages() {
        return messageRepository.findAllByOrderByCreatedAtDesc().stream().map(m -> {
            ContactMessageResponse r = new ContactMessageResponse();
            r.setId(m.getId());
            r.setFullName(m.getFullName());
            r.setEmail(m.getEmail());
            r.setSubject(m.getSubject());
            r.setMessage(m.getMessage());
            r.setRead(m.isRead());
            r.setStatus(m.getStatus());
            r.setClientIp(m.getClientIp());
            r.setPhone(m.getPhone());
            r.setCompany(m.getCompany());
            r.setServiceInterest(m.getServiceInterest());
            r.setEstimatedBudget(m.getEstimatedBudget());
            r.setLeadStatus(m.getLeadStatus());
            r.setFollowUpDate(m.getFollowUpDate());
            r.setInternalNotes(m.getInternalNotes());
            r.setQuotationDetails(m.getQuotationDetails());
            r.setCreatedAt(m.getCreatedAt());
            r.setReadAt(m.getReadAt());
            return r;
        }).collect(Collectors.toList());
    }

    public void markMessageRead(Long id, boolean read, String username, String ip) {
        ContactMessage m = messageRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + id));
        m.setRead(read);
        m.setStatus(read ? "READ" : "NEW");
        m.setReadAt(read ? LocalDateTime.now() : null);
        messageRepository.save(m);
        auditLogService.logAction(username, "UPDATE_MESSAGE_STATUS", "ContactMessages", String.valueOf(id), "Marked message as " + (read ? "read" : "unread"), ip);
    }

    public void archiveMessage(Long id, String username, String ip) {
        ContactMessage m = messageRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + id));
        m.setStatus("ARCHIVED");
        messageRepository.save(m);
        auditLogService.logAction(username, "ARCHIVE_MESSAGE", "ContactMessages", String.valueOf(id), "Archived contact message", ip);
    }

    public void deleteMessage(Long id, String username, String ip) {
        Long messageId = java.util.Objects.requireNonNull(id, "id must not be null");
        messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + id));
        messageRepository.deleteById(messageId);
        auditLogService.logAction(username, "DELETE_MESSAGE", "ContactMessages", String.valueOf(id), "Deleted contact message", ip);
    }

    // ==========================================
    // 12. SOCIAL LINKS CRUD
    // ==========================================
    @Transactional(readOnly = true)
    public List<SocialLinkDto> getAllSocialLinks() {
        return socialLinkRepository.findAllByOrderByDisplayOrderAsc().stream().map(s -> {
            SocialLinkDto d = new SocialLinkDto();
            d.setId(s.getId());
            d.setPlatformName(s.getPlatformName());
            d.setUrl(s.getUrl());
            d.setIconClass(s.getIconClass());
            d.setDisplayOrder(s.getDisplayOrder());
            d.setActive(s.isActive());
            return d;
        }).collect(Collectors.toList());
    }

    public SocialLinkDto createSocialLink(SocialLinkDto dto, String username, String ip) {
        SocialLink s = new SocialLink(dto.getPlatformName(), dto.getUrl(), dto.getIconClass(), dto.getDisplayOrder());
        s.setActive(dto.isActive());
        SocialLink saved = socialLinkRepository.save(s);
        auditLogService.logAction(username, "CREATE_SOCIAL_LINK", "SocialLinks", String.valueOf(saved.getId()), "Added social link: " + saved.getPlatformName(), ip);
        dto.setId(saved.getId());
        return dto;
    }

    public SocialLinkDto updateSocialLink(Long id, SocialLinkDto dto, String username, String ip) {
        SocialLink s = socialLinkRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Social link not found: " + id));
        s.setPlatformName(dto.getPlatformName());
        s.setUrl(dto.getUrl());
        s.setIconClass(dto.getIconClass());
        s.setDisplayOrder(dto.getDisplayOrder());
        s.setActive(dto.isActive());
        SocialLink saved = socialLinkRepository.save(s);
        auditLogService.logAction(username, "UPDATE_SOCIAL_LINK", "SocialLinks", String.valueOf(saved.getId()), "Updated social link: " + saved.getPlatformName(), ip);
        dto.setId(saved.getId());
        return dto;
    }

    public void deleteSocialLink(Long id, String username, String ip) {
        SocialLink s = socialLinkRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Social link not found: " + id));
        socialLinkRepository.delete(java.util.Objects.requireNonNull(s, "social link must not be null"));
        auditLogService.logAction(username, "DELETE_SOCIAL_LINK", "SocialLinks", String.valueOf(id), "Deleted social link: " + s.getPlatformName(), ip);
    }

    // ==========================================
    // 13. WEBSITE SETTINGS
    // ==========================================
    @Transactional(readOnly = true)
    public List<WebsiteSettingDto> getAllSettings() {
        return settingRepository.findAll().stream()
                .map(s -> new WebsiteSettingDto(s.getSettingKey(), s.getSettingValue(), s.getSettingGroup(), s.getDescription()))
                .collect(Collectors.toList());
    }

    public WebsiteSettingDto saveSetting(WebsiteSettingDto dto, String username, String ip) {
        String settingKey = java.util.Objects.requireNonNull(dto.getSettingKey(), "settingKey must not be null");
        WebsiteSetting s = settingRepository.findById(settingKey)
            .orElse(new WebsiteSetting(settingKey, dto.getSettingValue(), dto.getSettingGroup(), dto.getDescription()));
        s.setSettingValue(dto.getSettingValue());
        s.setSettingGroup(dto.getSettingGroup());
        s.setDescription(dto.getDescription());
        WebsiteSetting saved = settingRepository.save(s);
        auditLogService.logAction(username, "SAVE_SETTING", "WebsiteSettings", saved.getSettingKey(), "Updated setting: " + saved.getSettingKey(), ip);
        return dto;
    }

    // ==========================================
    // 14. AUDIT LOGS
    // ==========================================
    @Transactional(readOnly = true)
    public List<AuditLogDto> getRecentAuditLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc().stream().map(l -> {
            AuditLogDto d = new AuditLogDto();
            d.setId(l.getId());
            d.setAdminUsername(l.getAdminUsername());
            d.setAction(l.getAction());
            d.setEntityName(l.getEntityName());
            d.setEntityId(l.getEntityId());
            d.setDetails(l.getDetails());
            d.setIpAddress(l.getIpAddress());
            d.setTimestamp(l.getTimestamp());
            return d;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deleteAuditLog(Long id, String username, String ip) {
        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log entry not found with ID: " + id));
        auditLogRepository.delete(log);
        auditLogService.logAction(username, "DELETE_AUDIT_LOG", "AuditLogs", String.valueOf(id),
                "Deleted audit log record #" + id + " (" + log.getAction() + ")", ip);
    }

    @Transactional
    public void clearAllAuditLogs(String username, String ip) {
        long count = auditLogRepository.count();
        auditLogRepository.deleteAll();
        auditLogService.logAction(username, "CLEAR_ALL_AUDIT_LOGS", "AuditLogs", "ALL",
                "Cleared all " + count + " audit log entries", ip);
    }
}
