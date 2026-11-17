-- ============================================================================
-- MIGRATION V2: CLIENT RECOMMENDATIONS & WEBAUTHN PASSKEYS (MYSQL / XAMPP)
-- Database: PortfolioDB
-- ============================================================================

USE `PortfolioDB`;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. UPDATE / ENHANCE TESTIMONIALS TABLE
ALTER TABLE `testimonials`
    MODIFY COLUMN `client_name` VARCHAR(100) NULL DEFAULT '',
    MODIFY COLUMN `testimonial_text` TEXT NULL,
    MODIFY COLUMN `approval_status` VARCHAR(20) NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS `full_name` VARCHAR(100) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS `email` VARCHAR(150) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS `organization` VARCHAR(150) NULL,
    ADD COLUMN IF NOT EXISTS `position` VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS `relationship` VARCHAR(50) NOT NULL DEFAULT 'Client',
    ADD COLUMN IF NOT EXISTS `message` TEXT NULL,
    ADD COLUMN IF NOT EXISTS `photo_path` VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS `organization_website` VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS `consent_to_publish` TINYINT(1) NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS `submitted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS `reviewed_at` DATETIME NULL,
    ADD COLUMN IF NOT EXISTS `reviewed_by_admin_id` BIGINT NULL,
    ADD COLUMN IF NOT EXISTS `rejection_reason` VARCHAR(255) NULL;

-- 2. PASSKEY CREDENTIALS TABLE
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

-- 3. ADMIN DEVICES TABLE
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

-- 4. WEBAUTHN CHALLENGES TABLE
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

-- 5. ADMIN RECOVERY CODES TABLE
CREATE TABLE IF NOT EXISTS `admin_recovery_codes` (
    `recovery_code_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `admin_id` BIGINT NOT NULL,
    `hashed_recovery_code` VARCHAR(255) NOT NULL,
    `used_at` DATETIME NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_recovery_codes_admin` FOREIGN KEY (`admin_id`) REFERENCES `admins` (`adminid`) ON DELETE CASCADE,
    INDEX `idx_recovery_codes_admin` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. ADMIN AUTHENTICATION LOGS TABLE
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

SET FOREIGN_KEY_CHECKS = 1;
