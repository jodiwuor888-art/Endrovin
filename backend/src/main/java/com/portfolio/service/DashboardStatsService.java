package com.portfolio.service;

import com.portfolio.dto.DashboardStatsResponse;
import com.portfolio.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardStatsService {

    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final ServiceRepository serviceRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final CertificateRepository certificateRepository;
    private final AchievementRepository achievementRepository;
    private final TestimonialRepository testimonialRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final PersonalProfileRepository personalProfileRepository;

    public DashboardStatsService(ProjectRepository projectRepository,
                                 SkillRepository skillRepository,
                                 ServiceRepository serviceRepository,
                                 EducationRepository educationRepository,
                                 ExperienceRepository experienceRepository,
                                 CertificateRepository certificateRepository,
                                 AchievementRepository achievementRepository,
                                 TestimonialRepository testimonialRepository,
                                 ContactMessageRepository contactMessageRepository,
                                 PersonalProfileRepository personalProfileRepository) {
        this.projectRepository = projectRepository;
        this.skillRepository = skillRepository;
        this.serviceRepository = serviceRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.certificateRepository = certificateRepository;
        this.achievementRepository = achievementRepository;
        this.testimonialRepository = testimonialRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.personalProfileRepository = personalProfileRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStatistics() {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        stats.setTotalProjects(projectRepository.count());
        stats.setPublishedProjects(projectRepository.countByStatus("PUBLISHED"));
        stats.setTotalSkills(skillRepository.count());
        stats.setTotalServices(serviceRepository.count());
        stats.setTotalEducation(educationRepository.count());
        stats.setTotalExperience(experienceRepository.count());
        stats.setTotalCertificates(certificateRepository.count());
        stats.setTotalAchievements(achievementRepository.count());
        stats.setTotalTestimonials(testimonialRepository.count());
        stats.setPendingTestimonials(testimonialRepository.countByApprovalStatus("PENDING"));
        stats.setTotalMessages(contactMessageRepository.count());
        stats.setUnreadMessages(contactMessageRepository.countByReadFalse());

        personalProfileRepository.findFirstByActiveTrueOrderByIdAsc().ifPresent(profile -> {
            stats.setProfileConfigured(true);
            stats.setCvUploaded(profile.getCvFileUrl() != null && !profile.getCvFileUrl().isBlank());
        });

        return stats;
    }
}
