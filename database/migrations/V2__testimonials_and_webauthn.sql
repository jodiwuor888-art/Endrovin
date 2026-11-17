-- ============================================================================
-- MIGRATION V2: CLIENT RECOMMENDATIONS & WEBAUTHN BIOMETRIC PASSKEYS
-- Database: PortfolioDB (Microsoft SQL Server Compatible)
-- ============================================================================

-- 1. UPDATE / ENHANCE TESTIMONIALS TABLE
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Testimonials') AND name = 'FullName')
BEGIN
    -- If old structure exists, add missing columns or align column names
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Testimonials') AND name = 'ClientName')
    BEGIN
        EXEC sp_rename 'Testimonials.ClientName', 'FullName', 'COLUMN';
        EXEC sp_rename 'Testimonials.ClientTitle', 'Position', 'COLUMN';
        EXEC sp_rename 'Testimonials.ClientCompany', 'Organization', 'COLUMN';
        EXEC sp_rename 'Testimonials.ClientAvatarUrl', 'PhotoPath', 'COLUMN';
        EXEC sp_rename 'Testimonials.TestimonialText', 'Message', 'COLUMN';
        EXEC sp_rename 'Testimonials.ApprovalStatus', 'Status', 'COLUMN';
    END
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Testimonials') AND name = 'Email')
BEGIN
    ALTER TABLE Testimonials ADD Email NVARCHAR(150) NOT NULL DEFAULT '';
    ALTER TABLE Testimonials ADD Relationship NVARCHAR(50) NOT NULL DEFAULT 'Client';
    ALTER TABLE Testimonials ADD OrganizationWebsite NVARCHAR(255) NULL;
    ALTER TABLE Testimonials ADD ConsentToPublish BIT NOT NULL DEFAULT 1;
    ALTER TABLE Testimonials ADD SubmittedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME();
    ALTER TABLE Testimonials ADD ReviewedAt DATETIME2 NULL;
    ALTER TABLE Testimonials ADD ReviewedByAdminID BIGINT NULL;
    ALTER TABLE Testimonials ADD RejectionReason NVARCHAR(255) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Testimonials_Status' AND object_id = OBJECT_ID('Testimonials'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_Testimonials_Status ON Testimonials(Status);
END
GO

-- 2. PASSKEY CREDENTIALS TABLE
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

-- 3. ADMIN DEVICES TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'AdminDevices')
BEGIN
    CREATE TABLE AdminDevices (
        DeviceID BIGINT IDENTITY(1,1) PRIMARY KEY,
        AdminID BIGINT NOT NULL,
        CredentialID NVARCHAR(255) NOT NULL,
        DeviceName NVARCHAR(100) NOT NULL,
        DeviceType NVARCHAR(50) NOT NULL, -- 'ThinkPad Windows Hello', 'Android Smartphone', 'iPhone Biometric', 'Platform PIN'
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

-- 4. WEBAUTHN CHALLENGES TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'WebAuthnChallenges')
BEGIN
    CREATE TABLE WebAuthnChallenges (
        ChallengeID BIGINT IDENTITY(1,1) PRIMARY KEY,
        AdminID BIGINT NULL,
        ChallengeValue NVARCHAR(255) NOT NULL UNIQUE,
        ChallengeType NVARCHAR(50) NOT NULL, -- 'REGISTRATION', 'AUTHENTICATION'
        ExpiresAt DATETIME2 NOT NULL,
        UsedAt DATETIME2 NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_WebAuthnChallenges_Value ON WebAuthnChallenges(ChallengeValue);
    CREATE NONCLUSTERED INDEX IX_WebAuthnChallenges_Expiry ON WebAuthnChallenges(ExpiresAt);
END
GO

-- 5. ADMIN RECOVERY CODES TABLE
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

-- 6. ADMIN AUTHENTICATION LOGS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'AdminAuthenticationLogs')
BEGIN
    CREATE TABLE AdminAuthenticationLogs (
        LogID BIGINT IDENTITY(1,1) PRIMARY KEY,
        AdminID BIGINT NULL,
        AuthenticationMethod NVARCHAR(50) NOT NULL, -- 'PASSWORD', 'WEBAUTHN_BIOMETRIC', 'RECOVERY_CODE'
        DeviceInformation NVARCHAR(255) NULL,
        IPAddress NVARCHAR(50) NULL,
        AuthenticationStatus NVARCHAR(50) NOT NULL, -- 'SUCCESS', 'FAILED', 'PENDING_MFA'
        EventTimestamp DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE NONCLUSTERED INDEX IX_AuthLogs_AdminID ON AdminAuthenticationLogs(AdminID);
    CREATE NONCLUSTERED INDEX IX_AuthLogs_Timestamp ON AdminAuthenticationLogs(EventTimestamp);
END
GO
