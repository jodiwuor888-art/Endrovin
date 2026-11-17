-- ============================================================================
-- PORTFOLIO MANAGEMENT SYSTEM - DATABASE SCHEMA (Microsoft SQL Server)
-- Database Name: PortfolioDB
-- Production Ready Schema: Normalized, Indexed, Secure, Empty Content Tables
-- ============================================================================

-- Create Database if not exists
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'PortfolioDB')
BEGIN
    CREATE DATABASE [PortfolioDB];
END
GO

USE [PortfolioDB];
GO

-- 1. ROLES TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Roles')
BEGIN
    CREATE TABLE Roles (
        RoleID INT IDENTITY(1,1) PRIMARY KEY,
        RoleName NVARCHAR(50) NOT NULL UNIQUE,
        Description NVARCHAR(255) NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

-- 2. PERMISSIONS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Permissions')
BEGIN
    CREATE TABLE Permissions (
        PermissionID INT IDENTITY(1,1) PRIMARY KEY,
        PermissionName NVARCHAR(100) NOT NULL UNIQUE,
        Description NVARCHAR(255) NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

-- 3. ADMINS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Admins')
BEGIN
    CREATE TABLE Admins (
        AdminID BIGINT IDENTITY(1,1) PRIMARY KEY,
        Username NVARCHAR(50) NOT NULL UNIQUE,
        Email NVARCHAR(120) NOT NULL UNIQUE,
        PasswordHash NVARCHAR(255) NOT NULL,
        FullName NVARCHAR(100) NOT NULL,
        IsActive BIT NOT NULL DEFAULT 1,
        FailedLoginAttempts INT NOT NULL DEFAULT 0,
        LockoutUntil DATETIME2 NULL,
        LastLoginAt DATETIME2 NULL,
        PasswordResetToken NVARCHAR(100) NULL,
        PasswordResetExpiry DATETIME2 NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_Admins_Username ON Admins(Username);
    CREATE NONCLUSTERED INDEX IX_Admins_Email ON Admins(Email);
END
GO

-- 4. ADMIN_ROLES MAPPING TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'AdminRoles')
BEGIN
    CREATE TABLE AdminRoles (
        AdminID BIGINT NOT NULL,
        RoleID INT NOT NULL,
        AssignedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        PRIMARY KEY (AdminID, RoleID),
        CONSTRAINT FK_AdminRoles_Admin FOREIGN KEY (AdminID) REFERENCES Admins(AdminID) ON DELETE CASCADE,
        CONSTRAINT FK_AdminRoles_Role FOREIGN KEY (RoleID) REFERENCES Roles(RoleID) ON DELETE CASCADE
    );
END
GO

-- 5. ROLE_PERMISSIONS MAPPING TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'RolePermissions')
BEGIN
    CREATE TABLE RolePermissions (
        RoleID INT NOT NULL,
        PermissionID INT NOT NULL,
        PRIMARY KEY (RoleID, PermissionID),
        CONSTRAINT FK_RolePermissions_Role FOREIGN KEY (RoleID) REFERENCES Roles(RoleID) ON DELETE CASCADE,
        CONSTRAINT FK_RolePermissions_Permission FOREIGN KEY (PermissionID) REFERENCES Permissions(PermissionID) ON DELETE CASCADE
    );
END
GO

-- 6. PERSONAL PROFILE TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'PersonalProfile')
BEGIN
    CREATE TABLE PersonalProfile (
        ProfileID BIGINT IDENTITY(1,1) PRIMARY KEY,
        FullName NVARCHAR(100) NOT NULL,
        ProfessionalHeadline NVARCHAR(200) NOT NULL,
        ProfessionalTitle NVARCHAR(150) NOT NULL,
        BioSummary NVARCHAR(MAX) NULL,
        Location NVARCHAR(100) NOT NULL,
        Email NVARCHAR(120) NOT NULL,
        Phone NVARCHAR(50) NULL,
        GitHubUrl NVARCHAR(255) NULL,
        LinkedInUrl NVARCHAR(255) NULL,
        FacebookUrl NVARCHAR(255) NULL,
        TwitterUrl NVARCHAR(255) NULL,
        ProfilePhotoUrl NVARCHAR(500) NULL,
        CvFileUrl NVARCHAR(500) NULL,
        IsActive BIT NOT NULL DEFAULT 1,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

-- 7. ABOUT INFORMATION TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'AboutInformation')
BEGIN
    CREATE TABLE AboutInformation (
        AboutID BIGINT IDENTITY(1,1) PRIMARY KEY,
        SectionTitle NVARCHAR(150) NOT NULL,
        Subtitle NVARCHAR(200) NULL,
        Paragraph1 NVARCHAR(MAX) NOT NULL,
        Paragraph2 NVARCHAR(MAX) NULL,
        Paragraph3 NVARCHAR(MAX) NULL,
        YearsExperience INT NOT NULL DEFAULT 0,
        ProjectsCompleted INT NOT NULL DEFAULT 0,
        SatisfiedClients INT NOT NULL DEFAULT 0,
        Status NVARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

-- 8. SKILL CATEGORIES TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'SkillCategories')
BEGIN
    CREATE TABLE SkillCategories (
        CategoryID BIGINT IDENTITY(1,1) PRIMARY KEY,
        CategoryName NVARCHAR(100) NOT NULL UNIQUE,
        IconClass NVARCHAR(50) NULL,
        Description NVARCHAR(300) NULL,
        DisplayOrder INT NOT NULL DEFAULT 0,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_SkillCategories_Order ON SkillCategories(DisplayOrder);
END
GO

-- 9. SKILLS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Skills')
BEGIN
    CREATE TABLE Skills (
        SkillID BIGINT IDENTITY(1,1) PRIMARY KEY,
        CategoryID BIGINT NOT NULL,
        SkillName NVARCHAR(100) NOT NULL,
        ProficiencyPercentage INT NOT NULL DEFAULT 0,
        ProficiencyLevel NVARCHAR(50) NULL,
        IconClass NVARCHAR(50) NULL,
        DisplayOrder INT NOT NULL DEFAULT 0,
        Status NVARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_Skills_Category FOREIGN KEY (CategoryID) REFERENCES SkillCategories(CategoryID) ON DELETE CASCADE
    );
    CREATE NONCLUSTERED INDEX IX_Skills_Category ON Skills(CategoryID);
    CREATE NONCLUSTERED INDEX IX_Skills_Status ON Skills(Status);
END
GO

-- 10. SERVICES TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Services')
BEGIN
    CREATE TABLE Services (
        ServiceID BIGINT IDENTITY(1,1) PRIMARY KEY,
        Title NVARCHAR(150) NOT NULL,
        ShortDescription NVARCHAR(500) NOT NULL,
        DetailedDescription NVARCHAR(MAX) NULL,
        IconClass NVARCHAR(50) NULL,
        DisplayOrder INT NOT NULL DEFAULT 0,
        Status NVARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_Services_Status ON Services(Status);
END
GO

-- 11. PROJECTS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Projects')
BEGIN
    CREATE TABLE Projects (
        ProjectID BIGINT IDENTITY(1,1) PRIMARY KEY,
        Title NVARCHAR(200) NOT NULL,
        Slug NVARCHAR(220) NOT NULL UNIQUE,
        Category NVARCHAR(100) NOT NULL,
        ShortDescription NVARCHAR(500) NOT NULL,
        DetailedDescription NVARCHAR(MAX) NULL,
        Technologies NVARCHAR(300) NOT NULL,
        FeaturedImageUrl NVARCHAR(500) NULL,
        ProjectLink NVARCHAR(500) NULL,
        GithubUrl NVARCHAR(500) NULL,
        IsFeatured BIT NOT NULL DEFAULT 0,
        DisplayOrder INT NOT NULL DEFAULT 0,
        Status NVARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_Projects_Slug ON Projects(Slug);
    CREATE NONCLUSTERED INDEX IX_Projects_Status ON Projects(Status);
    CREATE NONCLUSTERED INDEX IX_Projects_Category ON Projects(Category);
END
GO

-- 12. PROJECT IMAGES TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ProjectImages')
BEGIN
    CREATE TABLE ProjectImages (
        ImageID BIGINT IDENTITY(1,1) PRIMARY KEY,
        ProjectID BIGINT NOT NULL,
        ImageUrl NVARCHAR(500) NOT NULL,
        Caption NVARCHAR(255) NULL,
        DisplayOrder INT NOT NULL DEFAULT 0,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_ProjectImages_Project FOREIGN KEY (ProjectID) REFERENCES Projects(ProjectID) ON DELETE CASCADE
    );
    CREATE NONCLUSTERED INDEX IX_ProjectImages_Project ON ProjectImages(ProjectID);
END
GO

-- 13. EDUCATION TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Education')
BEGIN
    CREATE TABLE Education (
        EducationID BIGINT IDENTITY(1,1) PRIMARY KEY,
        DegreeOrDiploma NVARCHAR(150) NOT NULL,
        FieldOfStudy NVARCHAR(150) NOT NULL,
        InstitutionName NVARCHAR(200) NOT NULL,
        Location NVARCHAR(100) NOT NULL,
        StartDate NVARCHAR(50) NOT NULL,
        EndDate NVARCHAR(50) NULL,
        IsCurrent BIT NOT NULL DEFAULT 0,
        GradeOrGpa NVARCHAR(50) NULL,
        Description NVARCHAR(MAX) NULL,
        DisplayOrder INT NOT NULL DEFAULT 0,
        Status NVARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_Education_Status ON Education(Status);
END
GO

-- 14. CERTIFICATES TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Certificates')
BEGIN
    CREATE TABLE Certificates (
        CertificateID BIGINT IDENTITY(1,1) PRIMARY KEY,
        CertificateTitle NVARCHAR(200) NOT NULL,
        IssuingOrganization NVARCHAR(200) NOT NULL,
        IssueDate NVARCHAR(50) NOT NULL,
        ExpirationDate NVARCHAR(50) NULL,
        CredentialId NVARCHAR(100) NULL,
        CredentialUrl NVARCHAR(500) NULL,
        CertificateFileUrl NVARCHAR(500) NULL,
        DisplayOrder INT NOT NULL DEFAULT 0,
        Status NVARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_Certificates_Status ON Certificates(Status);
END
GO

-- 15. ACHIEVEMENTS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Achievements')
BEGIN
    CREATE TABLE Achievements (
        AchievementID BIGINT IDENTITY(1,1) PRIMARY KEY,
        Title NVARCHAR(200) NOT NULL,
        Organization NVARCHAR(200) NULL,
        AchievementDate NVARCHAR(50) NULL,
        Description NVARCHAR(MAX) NOT NULL,
        DisplayOrder INT NOT NULL DEFAULT 0,
        Status NVARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_Achievements_Status ON Achievements(Status);
END
GO

-- 16. EXPERIENCE TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Experience')
BEGIN
    CREATE TABLE Experience (
        ExperienceID BIGINT IDENTITY(1,1) PRIMARY KEY,
        JobTitle NVARCHAR(150) NOT NULL,
        CompanyName NVARCHAR(200) NOT NULL,
        Location NVARCHAR(100) NOT NULL,
        EmploymentType NVARCHAR(50) NULL,
        StartDate NVARCHAR(50) NOT NULL,
        EndDate NVARCHAR(50) NULL,
        IsCurrentRole BIT NOT NULL DEFAULT 0,
        Responsibilities NVARCHAR(MAX) NOT NULL,
        TechnologiesUsed NVARCHAR(300) NULL,
        DisplayOrder INT NOT NULL DEFAULT 0,
        Status NVARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_Experience_Status ON Experience(Status);
END
GO

-- 17. TESTIMONIALS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Testimonials')
BEGIN
    CREATE TABLE Testimonials (
        TestimonialID BIGINT IDENTITY(1,1) PRIMARY KEY,
        ClientName NVARCHAR(100) NOT NULL,
        ClientTitle NVARCHAR(150) NULL,
        ClientCompany NVARCHAR(150) NULL,
        ClientAvatarUrl NVARCHAR(500) NULL,
        TestimonialText NVARCHAR(MAX) NOT NULL,
        Rating INT NOT NULL DEFAULT 5,
        ApprovalStatus NVARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, ARCHIVED
        DisplayOrder INT NOT NULL DEFAULT 0,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_Testimonials_ApprovalStatus ON Testimonials(ApprovalStatus);
END
GO

-- 18. CONTACT MESSAGES TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ContactMessages')
BEGIN
    CREATE TABLE ContactMessages (
        MessageID BIGINT IDENTITY(1,1) PRIMARY KEY,
        FullName NVARCHAR(100) NOT NULL,
        Email NVARCHAR(120) NOT NULL,
        Subject NVARCHAR(200) NOT NULL,
        Message NVARCHAR(MAX) NOT NULL,
        IsRead BIT NOT NULL DEFAULT 0,
        Status NVARCHAR(20) NOT NULL DEFAULT 'NEW', -- NEW, READ, ARCHIVED, DELETED
        ClientIp NVARCHAR(50) NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        ReadAt DATETIME2 NULL
    );
    CREATE NONCLUSTERED INDEX IX_ContactMessages_Status ON ContactMessages(Status);
    CREATE NONCLUSTERED INDEX IX_ContactMessages_IsRead ON ContactMessages(IsRead);
END
GO

-- 19. SOCIAL LINKS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'SocialLinks')
BEGIN
    CREATE TABLE SocialLinks (
        LinkId BIGINT IDENTITY(1,1) PRIMARY KEY,
        PlatformName NVARCHAR(50) NOT NULL,
        Url NVARCHAR(500) NOT NULL,
        IconClass NVARCHAR(50) NOT NULL,
        DisplayOrder INT NOT NULL DEFAULT 0,
        IsActive BIT NOT NULL DEFAULT 1,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_SocialLinks_Active ON SocialLinks(IsActive);
END
GO

-- 20. WEBSITE SETTINGS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'WebsiteSettings')
BEGIN
    CREATE TABLE WebsiteSettings (
        SettingKey NVARCHAR(100) PRIMARY KEY,
        SettingValue NVARCHAR(MAX) NULL,
        SettingGroup NVARCHAR(50) NOT NULL,
        Description NVARCHAR(255) NULL,
        UpdatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_WebsiteSettings_Group ON WebsiteSettings(SettingGroup);
END
GO

-- 21. AUDIT LOGS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'AuditLogs')
BEGIN
    CREATE TABLE AuditLogs (
        LogID BIGINT IDENTITY(1,1) PRIMARY KEY,
        AdminUsername NVARCHAR(50) NOT NULL,
        Action NVARCHAR(50) NOT NULL,       -- CREATE, UPDATE, DELETE, LOGIN, LOGOUT, FAILED_LOGIN, UPLOAD
        EntityName NVARCHAR(50) NOT NULL,
        EntityId NVARCHAR(50) NULL,
        Details NVARCHAR(MAX) NULL,
        IpAddress NVARCHAR(50) NULL,
        Timestamp DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_AuditLogs_Timestamp ON AuditLogs(Timestamp DESC);
    CREATE NONCLUSTERED INDEX IX_AuditLogs_Admin ON AuditLogs(AdminUsername);
END
GO

-- 22. UPLOADED FILES METADATA TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'UploadedFiles')
BEGIN
    CREATE TABLE UploadedFiles (
        FileID BIGINT IDENTITY(1,1) PRIMARY KEY,
        OriginalFileName NVARCHAR(255) NOT NULL,
        StoredFileName NVARCHAR(255) NOT NULL UNIQUE,
        FileType NVARCHAR(100) NOT NULL,
        FileCategory NVARCHAR(50) NOT NULL, -- PROFILE_PHOTO, CV, PROJECT_IMAGE, CERTIFICATE
        FileSize BIGINT NOT NULL,
        FileExtension NVARCHAR(20) NOT NULL,
        StoragePath NVARCHAR(500) NOT NULL,
        UploadedBy NVARCHAR(50) NOT NULL,
        UploadedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_UploadedFiles_Category ON UploadedFiles(FileCategory);
END
GO

-- 23. ADMIN PASSKEY CREDENTIALS TABLE (WEBAUTHN / FIDO2)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'AdminPasskeyCredentials')
BEGIN
    CREATE TABLE AdminPasskeyCredentials (
        CredentialID NVARCHAR(255) PRIMARY KEY,
        AdminID BIGINT NOT NULL,
        PublicKey NVARCHAR(MAX) NOT NULL,
        SignCount BIGINT NOT NULL DEFAULT 0,
        CredentialTransports NVARCHAR(255) NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        LastUsedAt DATETIME2 NULL,
        RevokedAt DATETIME2 NULL,
        CONSTRAINT FK_PasskeyCred_Admin FOREIGN KEY (AdminID) REFERENCES Admins(AdminID) ON DELETE CASCADE
    );
    CREATE NONCLUSTERED INDEX IX_PasskeyCred_AdminID ON AdminPasskeyCredentials(AdminID);
END
GO

-- 24. ADMIN DEVICES TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'AdminDevices')
BEGIN
    CREATE TABLE AdminDevices (
        DeviceID BIGINT IDENTITY(1,1) PRIMARY KEY,
        AdminID BIGINT NOT NULL,
        CredentialID NVARCHAR(255) NOT NULL,
        DeviceName NVARCHAR(100) NOT NULL,
        DeviceType NVARCHAR(50) NOT NULL,
        RegisteredAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        LastUsedAt DATETIME2 NULL,
        RevokedAt DATETIME2 NULL,
        CONSTRAINT FK_AdminDevices_Admin FOREIGN KEY (AdminID) REFERENCES Admins(AdminID) ON DELETE CASCADE,
        CONSTRAINT FK_AdminDevices_Cred FOREIGN KEY (CredentialID) REFERENCES AdminPasskeyCredentials(CredentialID) ON DELETE NO ACTION
    );
    CREATE NONCLUSTERED INDEX IX_AdminDevices_AdminID ON AdminDevices(AdminID);
    CREATE NONCLUSTERED INDEX IX_AdminDevices_CredID ON AdminDevices(CredentialID);
END
GO

-- 25. WEBAUTHN CHALLENGES TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'WebAuthnChallenges')
BEGIN
    CREATE TABLE WebAuthnChallenges (
        ChallengeID BIGINT IDENTITY(1,1) PRIMARY KEY,
        AdminID BIGINT NULL,
        ChallengeValue NVARCHAR(255) NOT NULL UNIQUE,
        ChallengeType NVARCHAR(50) NOT NULL,
        ExpiresAt DATETIME2 NOT NULL,
        UsedAt DATETIME2 NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_WebAuthnChallenges_Value ON WebAuthnChallenges(ChallengeValue);
    CREATE NONCLUSTERED INDEX IX_WebAuthnChallenges_Expiry ON WebAuthnChallenges(ExpiresAt);
END
GO

-- 26. ADMIN RECOVERY CODES TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'AdminRecoveryCodes')
BEGIN
    CREATE TABLE AdminRecoveryCodes (
        RecoveryCodeID BIGINT IDENTITY(1,1) PRIMARY KEY,
        AdminID BIGINT NOT NULL,
        HashedRecoveryCode NVARCHAR(255) NOT NULL,
        UsedAt DATETIME2 NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_RecoveryCodes_Admin FOREIGN KEY (AdminID) REFERENCES Admins(AdminID) ON DELETE CASCADE
    );
    CREATE NONCLUSTERED INDEX IX_RecoveryCodes_AdminID ON AdminRecoveryCodes(AdminID);
END
GO

-- 27. ADMIN AUTHENTICATION LOGS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'AdminAuthenticationLogs')
BEGIN
    CREATE TABLE AdminAuthenticationLogs (
        LogID BIGINT IDENTITY(1,1) PRIMARY KEY,
        AdminID BIGINT NULL,
        AuthenticationMethod NVARCHAR(50) NOT NULL,
        DeviceInformation NVARCHAR(255) NULL,
        IPAddress NVARCHAR(50) NULL,
        AuthenticationStatus NVARCHAR(50) NOT NULL,
        EventTimestamp DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_AuthLogs_AdminID ON AdminAuthenticationLogs(AdminID);
    CREATE NONCLUSTERED INDEX IX_AuthLogs_Timestamp ON AdminAuthenticationLogs(EventTimestamp);
END
GO

-- MANDATORY REQUIREMENT: ZERO PORTFOLIO RECORDS INSERTED
-- Production schema is completely initialized with tables and indexes, but 0 content records.
