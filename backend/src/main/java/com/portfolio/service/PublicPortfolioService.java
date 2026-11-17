package com.portfolio.service;

import com.portfolio.dto.*;
import com.portfolio.entity.*;
import com.portfolio.exception.BadRequestException;
import com.portfolio.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublicPortfolioService {

    private final PersonalProfileRepository personalProfileRepository;
    private final AboutInformationRepository aboutInformationRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final SkillRepository skillRepository;
    private final ServiceRepository serviceRepository;
    private final ProjectRepository projectRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final CertificateRepository certificateRepository;
    private final AchievementRepository achievementRepository;
    private final TestimonialRepository testimonialRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final SocialLinkRepository socialLinkRepository;
    private final EmailNotificationService emailNotificationService;

    public PublicPortfolioService(PersonalProfileRepository personalProfileRepository,
                                  AboutInformationRepository aboutInformationRepository,
                                  SkillCategoryRepository skillCategoryRepository,
                                  SkillRepository skillRepository,
                                  ServiceRepository serviceRepository,
                                  ProjectRepository projectRepository,
                                  EducationRepository educationRepository,
                                  ExperienceRepository experienceRepository,
                                  CertificateRepository certificateRepository,
                                  AchievementRepository achievementRepository,
                                  TestimonialRepository testimonialRepository,
                                  ContactMessageRepository contactMessageRepository,
                                  SocialLinkRepository socialLinkRepository,
                                  EmailNotificationService emailNotificationService) {
        this.personalProfileRepository = personalProfileRepository;
        this.aboutInformationRepository = aboutInformationRepository;
        this.skillCategoryRepository = skillCategoryRepository;
        this.skillRepository = skillRepository;
        this.serviceRepository = serviceRepository;
        this.projectRepository = projectRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.certificateRepository = certificateRepository;
        this.achievementRepository = achievementRepository;
        this.testimonialRepository = testimonialRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.socialLinkRepository = socialLinkRepository;
        this.emailNotificationService = emailNotificationService;
    }

    @Transactional(readOnly = true)
    public PublicPortfolioResponse getCompletePublicPortfolio() {
        PublicPortfolioResponse response = new PublicPortfolioResponse();

        // 1. Profile
        personalProfileRepository.findFirstByActiveTrueOrderByIdAsc().ifPresent(p -> {
            PersonalProfileDto dto = new PersonalProfileDto();
            dto.setId(p.getId());
            dto.setFullName(p.getFullName());
            dto.setProfessionalHeadline(p.getProfessionalHeadline());
            dto.setProfessionalTitle(p.getProfessionalTitle());
            dto.setBioSummary(p.getBioSummary());
            dto.setLocation(p.getLocation());
            dto.setEmail(p.getEmail());
            dto.setPhone(p.getPhone());
            dto.setGitHubUrl(p.getGitHubUrl());
            dto.setLinkedInUrl(p.getLinkedInUrl());
            dto.setFacebookUrl(p.getFacebookUrl());
            dto.setTwitterUrl(p.getTwitterUrl());
            dto.setProfilePhotoUrl(p.getProfilePhotoUrl());
            dto.setCvFileUrl(p.getCvFileUrl());
            dto.setActive(p.isActive());
            response.setProfile(dto);
        });

        // 2. About
        aboutInformationRepository.findFirstByStatusOrderByIdAsc("PUBLISHED").ifPresent(a -> {
            AboutInformationDto dto = new AboutInformationDto();
            dto.setId(a.getId());
            dto.setSectionTitle(a.getSectionTitle());
            dto.setSubtitle(a.getSubtitle());
            dto.setParagraph1(a.getParagraph1());
            dto.setParagraph2(a.getParagraph2());
            dto.setParagraph3(a.getParagraph3());
            dto.setYearsExperience(a.getYearsExperience());
            dto.setProjectsCompleted(a.getProjectsCompleted());
            dto.setSatisfiedClients(a.getSatisfiedClients());
            dto.setStatus(a.getStatus());
            response.setAbout(dto);
        });

        // 3. Skill Categories & Skills
        List<SkillCategory> categories = skillCategoryRepository.findAllByOrderByDisplayOrderAsc();
        List<SkillCategoryDto> categoryDtos = new ArrayList<>();
        for (SkillCategory cat : categories) {
            SkillCategoryDto catDto = new SkillCategoryDto();
            catDto.setId(cat.getId());
            catDto.setCategoryName(cat.getCategoryName());
            catDto.setIconClass(cat.getIconClass());
            catDto.setDescription(cat.getDescription());
            catDto.setDisplayOrder(cat.getDisplayOrder());

            List<SkillDto> skillDtos = skillRepository.findByCategoryIdAndStatusOrderByDisplayOrderAsc(cat.getId(), "PUBLISHED")
                    .stream().map(s -> {
                        SkillDto sDto = new SkillDto();
                        sDto.setId(s.getId());
                        sDto.setCategoryId(cat.getId());
                        sDto.setCategoryName(cat.getCategoryName());
                        sDto.setSkillName(s.getSkillName());
                        sDto.setProficiencyPercentage(s.getProficiencyPercentage());
                        sDto.setProficiencyLevel(s.getProficiencyLevel());
                        sDto.setIconClass(s.getIconClass());
                        sDto.setDisplayOrder(s.getDisplayOrder());
                        sDto.setStatus(s.getStatus());
                        return sDto;
                    }).collect(Collectors.toList());

            catDto.setSkills(skillDtos);
            categoryDtos.add(catDto);
        }
        response.setSkillCategories(categoryDtos);

        // All published skills flat list
        response.setSkills(skillRepository.findAllByStatusOrderByDisplayOrderAsc("PUBLISHED").stream().map(s -> {
            SkillDto dto = new SkillDto();
            dto.setId(s.getId());
            dto.setCategoryId(s.getCategory().getId());
            dto.setCategoryName(s.getCategory().getCategoryName());
            dto.setSkillName(s.getSkillName());
            dto.setProficiencyPercentage(s.getProficiencyPercentage());
            dto.setProficiencyLevel(s.getProficiencyLevel());
            dto.setIconClass(s.getIconClass());
            dto.setDisplayOrder(s.getDisplayOrder());
            dto.setStatus(s.getStatus());
            return dto;
        }).collect(Collectors.toList()));

        // 4. Services
        response.setServices(serviceRepository.findAllByStatusOrderByDisplayOrderAsc("PUBLISHED").stream().map(s -> {
            ServiceDto dto = new ServiceDto();
            dto.setId(s.getId());
            dto.setTitle(s.getTitle());
            dto.setShortDescription(s.getShortDescription());
            dto.setDetailedDescription(s.getDetailedDescription());
            dto.setIconClass(s.getIconClass());
            dto.setDisplayOrder(s.getDisplayOrder());
            dto.setStatus(s.getStatus());
            return dto;
        }).collect(Collectors.toList()));

        // 5. Projects
        response.setProjects(projectRepository.findAllByStatusOrderByDisplayOrderAsc("PUBLISHED").stream().map(p -> {
            ProjectDto dto = new ProjectDto();
            dto.setId(p.getId());
            dto.setTitle(p.getTitle());
            dto.setSlug(p.getSlug());
            dto.setCategory(p.getCategory());
            dto.setShortDescription(p.getShortDescription());
            dto.setDetailedDescription(p.getDetailedDescription());
            dto.setTechnologies(p.getTechnologies());
            dto.setFeaturedImageUrl(p.getFeaturedImageUrl());
            dto.setProjectLink(p.getProjectLink());
            dto.setGithubUrl(p.getGithubUrl());
            dto.setFeatured(p.isFeatured());
            dto.setDisplayOrder(p.getDisplayOrder());
            dto.setStatus(p.getStatus());
            return dto;
        }).collect(Collectors.toList()));

        // 6. Education
        response.setEducation(educationRepository.findAllByStatusOrderByDisplayOrderAsc("PUBLISHED").stream().map(e -> {
            EducationDto dto = new EducationDto();
            dto.setId(e.getId());
            dto.setDegreeOrDiploma(e.getDegreeOrDiploma());
            dto.setFieldOfStudy(e.getFieldOfStudy());
            dto.setInstitutionName(e.getInstitutionName());
            dto.setLocation(e.getLocation());
            dto.setStartDate(e.getStartDate());
            dto.setEndDate(e.getEndDate());
            dto.setCurrent(e.isCurrent());
            dto.setGradeOrGpa(e.getGradeOrGpa());
            dto.setDescription(e.getDescription());
            dto.setDisplayOrder(e.getDisplayOrder());
            dto.setStatus(e.getStatus());
            return dto;
        }).collect(Collectors.toList()));

        // 7. Experience
        response.setExperience(experienceRepository.findAllByStatusOrderByDisplayOrderAsc("PUBLISHED").stream().map(e -> {
            ExperienceDto dto = new ExperienceDto();
            dto.setId(e.getId());
            dto.setJobTitle(e.getJobTitle());
            dto.setCompanyName(e.getCompanyName());
            dto.setLocation(e.getLocation());
            dto.setEmploymentType(e.getEmploymentType());
            dto.setStartDate(e.getStartDate());
            dto.setEndDate(e.getEndDate());
            dto.setCurrentRole(e.isCurrentRole());
            dto.setResponsibilities(e.getResponsibilities());
            dto.setTechnologiesUsed(e.getTechnologiesUsed());
            dto.setDisplayOrder(e.getDisplayOrder());
            dto.setStatus(e.getStatus());
            return dto;
        }).collect(Collectors.toList()));

        // 8. Certificates
        response.setCertificates(certificateRepository.findAllByStatusOrderByDisplayOrderAsc("PUBLISHED").stream().map(c -> {
            CertificateDto dto = new CertificateDto();
            dto.setId(c.getId());
            dto.setCertificateTitle(c.getCertificateTitle());
            dto.setIssuingOrganization(c.getIssuingOrganization());
            dto.setIssueDate(c.getIssueDate());
            dto.setExpirationDate(c.getExpirationDate());
            dto.setCredentialId(c.getCredentialId());
            dto.setCredentialUrl(c.getCredentialUrl());
            dto.setCertificateFileUrl(c.getCertificateFileUrl());
            dto.setDisplayOrder(c.getDisplayOrder());
            dto.setStatus(c.getStatus());
            return dto;
        }).collect(Collectors.toList()));

        // 9. Achievements
        response.setAchievements(achievementRepository.findAllByStatusOrderByDisplayOrderAsc("PUBLISHED").stream().map(a -> {
            AchievementDto dto = new AchievementDto();
            dto.setId(a.getId());
            dto.setTitle(a.getTitle());
            dto.setOrganization(a.getOrganization());
            dto.setAchievementDate(a.getAchievementDate());
            dto.setDescription(a.getDescription());
            dto.setDisplayOrder(a.getDisplayOrder());
            dto.setStatus(a.getStatus());
            return dto;
        }).collect(Collectors.toList()));

        // 10. Testimonials (Approved and Consented only)
        response.setTestimonials(testimonialRepository.findByStatusAndConsentToPublishTrueOrderByDisplayOrderAscSubmittedAtDesc("APPROVED").stream().map(t -> {
            TestimonialDto dto = new TestimonialDto();
            dto.setId(t.getId());
            dto.setFullName(t.getFullName());
            dto.setClientName(t.getFullName());
            dto.setPosition(t.getPosition());
            dto.setClientTitle(t.getPosition());
            dto.setOrganization(t.getOrganization());
            dto.setClientCompany(t.getOrganization());
            dto.setRelationship(t.getRelationship());
            dto.setPhotoPath(t.getPhotoPath());
            dto.setClientAvatarUrl(t.getPhotoPath());
            dto.setMessage(t.getMessage());
            dto.setTestimonialText(t.getMessage());
            dto.setRating(t.getRating());
            dto.setStatus(t.getStatus());
            dto.setApprovalStatus(t.getStatus());
            dto.setDisplayOrder(t.getDisplayOrder());
            dto.setSubmittedAt(t.getSubmittedAt());
            return dto;
        }).collect(Collectors.toList()));

        // 11. Social Links
        response.setSocialLinks(socialLinkRepository.findByActiveTrueOrderByDisplayOrderAsc().stream().map(s -> {
            SocialLinkDto dto = new SocialLinkDto();
            dto.setId(s.getId());
            dto.setPlatformName(s.getPlatformName());
            dto.setUrl(s.getUrl());
            dto.setIconClass(s.getIconClass());
            dto.setDisplayOrder(s.getDisplayOrder());
            dto.setActive(s.isActive());
            return dto;
        }).collect(Collectors.toList()));

        return response;
    }

    @Transactional
    public ContactMessageResponse submitContactMessage(ContactMessageRequest request, String clientIp) {
        // Honeypot spam protection
        if (request.getWebsite() != null && !request.getWebsite().isBlank()) {
            throw new BadRequestException("Spam detected");
        }

        ContactMessage msg = new ContactMessage(
                request.getFullName().trim(),
                request.getEmail().trim(),
                request.getSubject().trim(),
                request.getMessage().trim(),
                clientIp
        );

        ContactMessage saved = contactMessageRepository.save(msg);

        // Async email notification if enabled
        emailNotificationService.sendContactNotification(saved);

        ContactMessageResponse resp = new ContactMessageResponse();
        resp.setId(saved.getId());
        resp.setFullName(saved.getFullName());
        resp.setEmail(saved.getEmail());
        resp.setSubject(saved.getSubject());
        resp.setMessage(saved.getMessage());
        resp.setRead(saved.isRead());
        resp.setStatus(saved.getStatus());
        resp.setCreatedAt(saved.getCreatedAt());

        return resp;
    }

    /* ==========================================================================
       PUBLIC RECOMMENDATIONS & TESTIMONIALS
       ========================================================================== */

    @Transactional(readOnly = true)
    public List<PublicTestimonialResponse> getApprovedPublicTestimonials() {
        return testimonialRepository.findByStatusAndConsentToPublishTrueOrderByDisplayOrderAscSubmittedAtDesc("APPROVED")
                .stream()
                .map(t -> new PublicTestimonialResponse(
                        t.getId(),
                        t.getFullName(),
                        t.getOrganization(),
                        t.getPosition(),
                        t.getRelationship(),
                        t.getMessage(),
                        t.getPhotoPath(),
                        t.getOrganizationWebsite(),
                        t.getRating(),
                        t.getSubmittedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public String submitPublicRecommendation(PublicTestimonialSubmissionRequest request, String clientIp) {
        // 1. Honeypot check
        if (request.getWebsite() != null && !request.getWebsite().isBlank()) {
            throw new BadRequestException("Spam detected");
        }

        // 2. Consent check
        if (!request.isConsentToPublish()) {
            throw new BadRequestException("Consent to publish is required to submit a recommendation.");
        }

        // 3. CAPTCHA verification
        if (request.getCaptchaToken() == null || request.getCaptchaToken().isBlank()) {
            throw new BadRequestException("Please solve the human security verification challenge.");
        }
        if (request.getCaptchaAnswer() == null || request.getCaptchaAnswer().isBlank()) {
            throw new BadRequestException("Please enter your answer to the security verification challenge.");
        }
        verifyCaptcha(request.getCaptchaToken(), request.getCaptchaAnswer());

        // 4. Create and persist recommendation
        Testimonial testimonial = new Testimonial();
        testimonial.setFullName(sanitize(request.getFullName()));
        testimonial.setEmail(request.getEmail().trim().toLowerCase());
        testimonial.setOrganization(sanitize(request.getOrganization()));
        testimonial.setPosition(sanitize(request.getPosition()));
        testimonial.setRelationship(request.getRelationship() != null ? request.getRelationship() : "Client");
        testimonial.setMessage(sanitize(request.getMessage()));
        testimonial.setPhotoPath(request.getPhotoUrl());
        testimonial.setOrganizationWebsite(request.getOrganizationWebsite());
        testimonial.setConsentToPublish(true);
        testimonial.setStatus("PENDING"); // Mandatory PENDING status
        testimonial.setRating(request.getRating());
        testimonial.setSubmittedAt(java.time.LocalDateTime.now());

        testimonialRepository.save(testimonial);

        return "Thank you for sharing your experience. Your recommendation has been received and will be reviewed before publication.";
    }

    public java.util.Map<String, String> generateCaptcha() {
        java.util.Random rnd = new java.util.Random();
        int a = rnd.nextInt(9) + 1;
        int b = rnd.nextInt(9) + 1;
        int ans = a + b;
        long ts = System.currentTimeMillis();
        String payload = ts + ":" + ans;
        String token = java.util.Base64.getUrlEncoder().encodeToString(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return java.util.Map.of("token", token, "question", "What is " + a + " + " + b + "?");
    }

    private void verifyCaptcha(String token, String answer) {
        try {
            byte[] bytes = java.util.Base64.getUrlDecoder().decode(token);
            String payload = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            String[] parts = payload.split(":");
            long ts = Long.parseLong(parts[0]);
            int expected = Integer.parseInt(parts[1]);

            // Expiry 10 minutes
            if (System.currentTimeMillis() - ts > 600000) {
                throw new BadRequestException("CAPTCHA has expired. Please refresh the verification challenge.");
            }
            if (answer == null || Integer.parseInt(answer.trim()) != expected) {
                throw new BadRequestException("Incorrect CAPTCHA answer. Please verify you are human.");
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Invalid CAPTCHA challenge format");
        }
    }

    private String sanitize(String input) {
        if (input == null) return null;
        return input.trim().replace("<", "&lt;").replace(">", "&gt;");
    }
}
