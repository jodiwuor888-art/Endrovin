-- ============================================================================
-- PORTFOLIO MANAGEMENT SYSTEM - MYSQL / MARIADB SCHEMA (XAMPP COMPATIBLE)
-- Database: PortfolioDB
-- Description: Complete normalized schema for XAMPP (MySQL / MariaDB / phpMyAdmin)
-- Note: Table structures created with ZERO portfolio content records
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `PortfolioDB` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `PortfolioDB`;

-- Disable foreign key checks during schema creation
SET FOREIGN_KEY_CHECKS = 0;

-- 1. ROLES TABLE
CREATE TABLE IF NOT EXISTS `roles` (
    `roleid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_name` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. PERMISSIONS TABLE
CREATE TABLE IF NOT EXISTS `permissions` (
    `permissionid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `permission_name` VARCHAR(100) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. ADMINS TABLE
CREATE TABLE IF NOT EXISTS `admins` (
    `adminid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_locked` TINYINT(1) NOT NULL DEFAULT 0,
    `failed_login_attempts` INT NOT NULL DEFAULT 0,
    `lockout_expiry` DATETIME NULL,
    `last_login` DATETIME NULL,
    `last_login_ip` VARCHAR(50) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_admins_username` (`username`),
    INDEX `idx_admins_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. ADMIN ROLES JUNCTION
CREATE TABLE IF NOT EXISTS `admin_roles` (
    `adminid` BIGINT NOT NULL,
    `roleid` BIGINT NOT NULL,
    PRIMARY KEY (`adminid`, `roleid`),
    CONSTRAINT `fk_admin_roles_admin` FOREIGN KEY (`adminid`) REFERENCES `admins` (`adminid`) ON DELETE CASCADE,
    CONSTRAINT `fk_admin_roles_role` FOREIGN KEY (`roleid`) REFERENCES `roles` (`roleid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. ROLE PERMISSIONS JUNCTION
CREATE TABLE IF NOT EXISTS `role_permissions` (
    `roleid` BIGINT NOT NULL,
    `permissionid` BIGINT NOT NULL,
    PRIMARY KEY (`roleid`, `permissionid`),
    CONSTRAINT `fk_role_perm_role` FOREIGN KEY (`roleid`) REFERENCES `roles` (`roleid`) ON DELETE CASCADE,
    CONSTRAINT `fk_role_perm_perm` FOREIGN KEY (`permissionid`) REFERENCES `permissions` (`permissionid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. PERSONAL PROFILE TABLE
CREATE TABLE IF NOT EXISTS `personal_profile` (
    `profileid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `full_name` VARCHAR(100) NOT NULL,
    `professional_headline` VARCHAR(150) NOT NULL,
    `professional_title` VARCHAR(100) NOT NULL,
    `location` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(30) NULL,
    `git_hub_url` VARCHAR(255) NULL,
    `linked_in_url` VARCHAR(255) NULL,
    `twitter_url` VARCHAR(255) NULL,
    `facebook_url` VARCHAR(255) NULL,
    `profile_photo_url` VARCHAR(500) NULL,
    `cv_file_url` VARCHAR(500) NULL,
    `bio_summary` TEXT NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. ABOUT INFORMATION TABLE
CREATE TABLE IF NOT EXISTS `about_information` (
    `aboutid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `section_title` VARCHAR(100) NOT NULL DEFAULT 'About Me',
    `subtitle` VARCHAR(150) NULL,
    `paragraph1` TEXT NOT NULL,
    `paragraph2` TEXT NULL,
    `paragraph3` TEXT NULL,
    `years_of_experience` INT NOT NULL DEFAULT 0,
    `projects_completed` INT NOT NULL DEFAULT 0,
    `satisfied_clients` INT NOT NULL DEFAULT 0,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. SKILL CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS `skill_categories` (
    `categoryid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `category_name` VARCHAR(100) NOT NULL UNIQUE,
    `icon_class` VARCHAR(50) NULL,
    `description` VARCHAR(255) NULL,
    `display_order` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. SKILLS TABLE
CREATE TABLE IF NOT EXISTS `skills` (
    `skillid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `categoryid` BIGINT NOT NULL,
    `skill_name` VARCHAR(100) NOT NULL,
    `proficiency_percentage` INT NOT NULL CHECK (`proficiency_percentage` BETWEEN 0 AND 100),
    `proficiency_level` VARCHAR(50) NULL,
    `icon_url` VARCHAR(255) NULL,
    `display_order` INT NOT NULL DEFAULT 0,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_skills_category` FOREIGN KEY (`categoryid`) REFERENCES `skill_categories` (`categoryid`) ON DELETE CASCADE,
    INDEX `idx_skills_category` (`categoryid`),
    INDEX `idx_skills_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. SERVICES TABLE
CREATE TABLE IF NOT EXISTS `services` (
    `serviceid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(150) NOT NULL,
    `short_description` VARCHAR(500) NOT NULL,
    `detailed_description` TEXT NULL,
    `icon_class` VARCHAR(50) NULL,
    `display_order` INT NOT NULL DEFAULT 0,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_services_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. PROJECTS TABLE
CREATE TABLE IF NOT EXISTS `projects` (
    `projectid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(150) NOT NULL,
    `slug` VARCHAR(160) NOT NULL UNIQUE,
    `short_description` VARCHAR(500) NOT NULL,
    `detailed_description` TEXT NULL,
    `category` VARCHAR(50) NOT NULL,
    `technologies` VARCHAR(255) NOT NULL,
    `featured_image_url` VARCHAR(500) NULL,
    `project_link` VARCHAR(500) NULL,
    `github_url` VARCHAR(500) NULL,
    `is_featured` TINYINT(1) NOT NULL DEFAULT 0,
    `display_order` INT NOT NULL DEFAULT 0,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_projects_status` (`status`),
    INDEX `idx_projects_category` (`category`),
    INDEX `idx_projects_featured` (`is_featured`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. PROJECT IMAGES TABLE
CREATE TABLE IF NOT EXISTS `project_images` (
    `imageid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `projectid` BIGINT NOT NULL,
    `image_url` VARCHAR(500) NOT NULL,
    `caption` VARCHAR(255) NULL,
    `is_primary` TINYINT(1) NOT NULL DEFAULT 0,
    `display_order` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_project_images_project` FOREIGN KEY (`projectid`) REFERENCES `projects` (`projectid`) ON DELETE CASCADE,
    INDEX `idx_project_images_project` (`projectid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. EDUCATION TABLE
CREATE TABLE IF NOT EXISTS `education` (
    `educationid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `institution_name` VARCHAR(150) NOT NULL,
    `degree_or_diploma` VARCHAR(150) NOT NULL,
    `field_of_study` VARCHAR(100) NOT NULL,
    `location` VARCHAR(100) NOT NULL,
    `start_date` VARCHAR(30) NOT NULL,
    `end_date` VARCHAR(30) NULL,
    `is_current` TINYINT(1) NOT NULL DEFAULT 0,
    `grade_or_gpa` VARCHAR(50) NULL,
    `description` TEXT NULL,
    `display_order` INT NOT NULL DEFAULT 0,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_education_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. EXPERIENCE TABLE
CREATE TABLE IF NOT EXISTS `experience` (
    `experienceid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `job_title` VARCHAR(150) NOT NULL,
    `company_name` VARCHAR(150) NOT NULL,
    `location` VARCHAR(100) NOT NULL,
    `employment_type` VARCHAR(50) NULL,
    `start_date` VARCHAR(30) NOT NULL,
    `end_date` VARCHAR(30) NULL,
    `is_current_role` TINYINT(1) NOT NULL DEFAULT 0,
    `responsibilities` TEXT NOT NULL,
    `technologies_used` VARCHAR(255) NULL,
    `display_order` INT NOT NULL DEFAULT 0,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_experience_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. CERTIFICATES TABLE
CREATE TABLE IF NOT EXISTS `certificates` (
    `certificateid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `certificate_title` VARCHAR(150) NOT NULL,
    `issuing_organization` VARCHAR(150) NOT NULL,
    `issue_date` VARCHAR(30) NOT NULL,
    `expiration_date` VARCHAR(30) NULL,
    `credential_id` VARCHAR(100) NULL,
    `credential_url` VARCHAR(500) NULL,
    `certificate_file_url` VARCHAR(500) NULL,
    `display_order` INT NOT NULL DEFAULT 0,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_certificates_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. ACHIEVEMENTS TABLE
CREATE TABLE IF NOT EXISTS `achievements` (
    `achievementid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(150) NOT NULL,
    `organization` VARCHAR(150) NULL,
    `achievement_date` VARCHAR(30) NULL,
    `description` TEXT NOT NULL,
    `display_order` INT NOT NULL DEFAULT 0,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_achievements_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. TESTIMONIALS TABLE
CREATE TABLE IF NOT EXISTS `testimonials` (
    `testimonialid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `client_name` VARCHAR(100) NULL DEFAULT '',
    `client_title` VARCHAR(100) NULL,
    `client_company` VARCHAR(100) NULL,
    `client_avatar_url` VARCHAR(500) NULL,
    `testimonial_text` TEXT NULL,
    `rating` INT NOT NULL DEFAULT 5,
    `approval_status` VARCHAR(20) NULL DEFAULT 'PENDING',
    `display_order` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `full_name` VARCHAR(100) NOT NULL DEFAULT '',
    `email` VARCHAR(150) NOT NULL DEFAULT '',
    `organization` VARCHAR(150) NULL,
    `position` VARCHAR(100) NULL,
    `relationship` VARCHAR(50) NOT NULL DEFAULT 'Client',
    `message` TEXT NOT NULL,
    `photo_path` VARCHAR(500) NULL,
    `organization_website` VARCHAR(255) NULL,
    `consent_to_publish` TINYINT(1) NOT NULL DEFAULT 1,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    `submitted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `reviewed_at` DATETIME NULL,
    `reviewed_by_admin_id` BIGINT NULL,
    `rejection_reason` VARCHAR(255) NULL,
    INDEX `idx_testimonials_approval` (`approval_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. CONTACT MESSAGES TABLE
CREATE TABLE IF NOT EXISTS `contact_messages` (
    `messageid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `subject` VARCHAR(150) NOT NULL,
    `message` TEXT NOT NULL,
    `client_ip` VARCHAR(50) NULL,
    `is_read` TINYINT(1) NOT NULL DEFAULT 0,
    `read_at` DATETIME NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_contact_read` (`is_read`),
    INDEX `idx_contact_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. SOCIAL LINKS TABLE
CREATE TABLE IF NOT EXISTS `social_links` (
    `link_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `platform_name` VARCHAR(50) NOT NULL,
    `url` VARCHAR(255) NOT NULL,
    `icon_class` VARCHAR(50) NOT NULL,
    `display_order` INT NOT NULL DEFAULT 0,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_social_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. WEBSITE SETTINGS TABLE
CREATE TABLE IF NOT EXISTS `website_settings` (
    `setting_key` VARCHAR(50) PRIMARY KEY,
    `setting_value` TEXT NOT NULL,
    `setting_group` VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    `description` VARCHAR(255) NULL,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 21. AUDIT LOGS TABLE
CREATE TABLE IF NOT EXISTS `audit_logs` (
    `logid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `action` VARCHAR(50) NOT NULL,
    `entity_name` VARCHAR(50) NOT NULL,
    `entity_id` VARCHAR(50) NULL,
    `admin_username` VARCHAR(50) NOT NULL,
    `ip_address` VARCHAR(50) NOT NULL,
    `details` TEXT NULL,
    `timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_audit_action` (`action`),
    INDEX `idx_audit_admin` (`admin_username`),
    INDEX `idx_audit_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 22. UPLOADED FILES METADATA TABLE
CREATE TABLE IF NOT EXISTS `uploaded_files` (
    `fileid` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `original_filename` VARCHAR(255) NOT NULL,
    `stored_filename` VARCHAR(255) NOT NULL UNIQUE,
    `file_path` VARCHAR(500) NOT NULL,
    `file_url` VARCHAR(500) NOT NULL,
    `content_type` VARCHAR(100) NOT NULL,
    `file_size_bytes` BIGINT NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `uploaded_by` VARCHAR(50) NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_uploaded_files_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 23. PASSKEY CREDENTIALS TABLE (WEBAUTHN / FIDO2)
CREATE TABLE IF NOT EXISTS `admin_passkey_credentials` (
    `credential_id` VARCHAR(255) PRIMARY KEY,
    `admin_id` BIGINT NOT NULL,
    `public_key` TEXT NOT NULL,
    `sign_count` BIGINT NOT NULL DEFAULT 0,
    `credential_transports` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_used_at` DATETIME NULL,
    `revoked_at` DATETIME NULL,
    CONSTRAINT `fk_passkey_cred_admin` FOREIGN KEY (`admin_id`) REFERENCES `admins` (`adminid`) ON DELETE CASCADE,
    INDEX `idx_passkey_admin` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 24. ADMIN DEVICES TABLE
CREATE TABLE IF NOT EXISTS `admin_devices` (
    `device_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `admin_id` BIGINT NOT NULL,
    `credential_id` VARCHAR(255) NOT NULL,
    `device_name` VARCHAR(100) NOT NULL,
    `device_type` VARCHAR(50) NOT NULL,
    `registered_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_used_at` DATETIME NULL,
    `revoked_at` DATETIME NULL,
    CONSTRAINT `fk_admin_devices_admin` FOREIGN KEY (`admin_id`) REFERENCES `admins` (`adminid`) ON DELETE CASCADE,
    CONSTRAINT `fk_admin_devices_cred` FOREIGN KEY (`credential_id`) REFERENCES `admin_passkey_credentials` (`credential_id`) ON DELETE CASCADE,
    INDEX `idx_admin_devices_admin` (`admin_id`),
    INDEX `idx_admin_devices_cred` (`credential_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 25. WEBAUTHN CHALLENGES TABLE
CREATE TABLE IF NOT EXISTS `webauthn_challenges` (
    `challenge_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `admin_id` BIGINT NULL,
    `challenge_value` VARCHAR(255) NOT NULL UNIQUE,
    `challenge_type` VARCHAR(50) NOT NULL,
    `expires_at` DATETIME NOT NULL,
    `used_at` DATETIME NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_challenges_value` (`challenge_value`),
    INDEX `idx_challenges_expiry` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 26. ADMIN RECOVERY CODES TABLE
CREATE TABLE IF NOT EXISTS `admin_recovery_codes` (
    `recovery_code_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `admin_id` BIGINT NOT NULL,
    `hashed_recovery_code` VARCHAR(255) NOT NULL,
    `used_at` DATETIME NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_recovery_codes_admin` FOREIGN KEY (`admin_id`) REFERENCES `admins` (`adminid`) ON DELETE CASCADE,
    INDEX `idx_recovery_codes_admin` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 27. ADMIN AUTHENTICATION LOGS TABLE
CREATE TABLE IF NOT EXISTS `admin_authentication_logs` (
    `log_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `admin_id` BIGINT NULL,
    `authentication_method` VARCHAR(50) NOT NULL,
    `device_information` VARCHAR(255) NULL,
    `ip_address` VARCHAR(50) NULL,
    `authentication_status` VARCHAR(50) NOT NULL,
    `event_timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_auth_logs_admin` (`admin_id`),
    INDEX `idx_auth_logs_time` (`event_timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 28. WEBSITE CONFIGURATION SETTINGS TABLE
CREATE TABLE IF NOT EXISTS `website_settings` (
    `setting_key` VARCHAR(100) PRIMARY KEY,
    `setting_value` LONGTEXT NULL,
    `setting_group` VARCHAR(50) NOT NULL,
    `description` VARCHAR(255) NULL,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_website_settings_group` (`setting_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default configurations
INSERT IGNORE INTO `website_settings` (`setting_key`, `setting_value`, `setting_group`, `description`) VALUES
('general.websiteName', 'Portfolio Management System', 'GENERAL', 'Website brand and header name'),
('general.brandName', 'Joash Odiwuor', 'GENERAL', 'Portfolio personal brand name'),
('general.tagline', 'Diploma in Computer Science & Aspiring Software Developer', 'GENERAL', 'Header tagline or sub-headline'),
('general.description', 'Professional portfolio and software engineering showcase', 'GENERAL', 'General website meta description'),
('general.ownerName', 'Joash Odiwuor', 'GENERAL', 'Full legal name of the website owner'),
('general.professionalTitle', 'Aspiring Software Developer', 'GENERAL', 'Professional job title'),
('general.contactEmail', 'jodiwuor888@gmail.com', 'GENERAL', 'Primary administrative contact email'),
('general.contactPhone', '+254 742963410', 'GENERAL', 'Primary contact telephone / WhatsApp'),
('general.physicalLocation', 'Kenya', 'GENERAL', 'City, Region, or Country'),
('general.defaultLanguage', 'en', 'GENERAL', 'Default website language code'),
('general.timezone', 'Africa/Nairobi', 'GENERAL', 'Default system timezone'),
('general.maintenanceMode', 'false', 'GENERAL', 'Whether the public website is in maintenance mode'),
('maintenance.maintenanceNotice', 'The portfolio is briefly undergoing scheduled updates. Please check back shortly.', 'MAINTENANCE', 'Custom message displayed during maintenance mode'),
('appearance.themeMode', 'dark', 'APPEARANCE', 'Theme color mode: dark, light, or auto'),
('appearance.primaryColor', '#0f172a', 'APPEARANCE', 'Primary background dark navy color'),
('appearance.secondaryColor', '#1e293b', 'APPEARANCE', 'Deep blue secondary section background'),
('appearance.accentColor', '#00d2ff', 'APPEARANCE', 'Electric blue primary accent color'),
('appearance.backgroundColor', '#0a0f1d', 'APPEARANCE', 'Deepest background base color'),
('appearance.textColor', '#f8fafc', 'APPEARANCE', 'Primary light typography color'),
('appearance.fontFamily', 'Inter, system-ui, sans-serif', 'APPEARANCE', 'Modern professional font family'),
('homepage.showAbout', 'true', 'HOMEPAGE', 'Show About Me section on public homepage'),
('homepage.showSkills', 'true', 'HOMEPAGE', 'Show Technical Skills section'),
('homepage.showServices', 'true', 'HOMEPAGE', 'Show Professional Services section'),
('homepage.showProjects', 'true', 'HOMEPAGE', 'Show Projects portfolio section'),
('homepage.showEducation', 'true', 'HOMEPAGE', 'Show Education qualifications section'),
('homepage.showExperience', 'true', 'HOMEPAGE', 'Show Professional Experience section'),
('homepage.showCertificates', 'true', 'HOMEPAGE', 'Show Verified Certificates section'),
('homepage.showAchievements', 'true', 'HOMEPAGE', 'Show Honors & Achievements section'),
('homepage.showTestimonials', 'true', 'HOMEPAGE', 'Show Recommendations & Testimonials section'),
('homepage.showContact', 'true', 'HOMEPAGE', 'Show Contact enquiry form section'),
('homepage.showSocialIcons', 'true', 'HOMEPAGE', 'Show social media icons in hero & footer'),
('homepage.maxProjectsDisplayed', '6', 'HOMEPAGE', 'Maximum projects displayed on homepage'),
('homepage.maxTestimonialsDisplayed', '6', 'HOMEPAGE', 'Maximum testimonials displayed on homepage'),
('seo.metaTitle', 'Joash Odiwuor | Software Developer Portfolio', 'SEO', 'Default HTML document title'),
('seo.metaDescription', 'Professional portfolio of Joash Odiwuor - Diploma in Computer Science student and software developer in Kenya.', 'SEO', 'Search engine meta description'),
('seo.keywords', 'software developer, Java, Spring Boot, full-stack, Kenya, web developer', 'SEO', 'Search engine comma-separated keywords'),
('seo.allowIndexing', 'true', 'SEO', 'Whether search engines should index the website (robots)'),
('contact.publicEmail', 'jodiwuor888@gmail.com', 'CONTACT_SOCIAL', 'Email displayed publicly on contact section'),
('contact.phone', '+254 742963410', 'CONTACT_SOCIAL', 'Phone number displayed publicly'),
('contact.whatsapp', '+254 742963410', 'CONTACT_SOCIAL', 'WhatsApp direct message number'),
('contact.enableContactForm', 'true', 'CONTACT_SOCIAL', 'Whether visitor inquiry form is active'),
('security.requireMfaPasskey', 'true', 'SECURITY_ACCESS', 'Enforce biometric / FIDO2 passkey MFA for administrators'),
('security.sessionTimeoutMinutes', '60', 'SECURITY_ACCESS', 'Administrator JWT session inactivity timeout'),
('privacy.enableCookieBanner', 'true', 'PRIVACY_LEGAL', 'Display cookie consent banner to visitors');

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
