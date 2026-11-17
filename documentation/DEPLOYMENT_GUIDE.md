# Installation & Deployment Guide

This guide covers complete local setup and production deployment of the **Portfolio Management System** using Java 25 LTS, Spring Boot 3, and Microsoft SQL Server.

---

## 1. Prerequisites

1. **Java Development Kit (JDK 25 LTS or later)**
   - Check with: `java -version`
2. **Microsoft SQL Server (2019, 2022, 2025, or SQL Server Express)**
   - SQL Server Management Studio (SSMS) or Azure Data Studio
3. **Apache Maven 3.8+** (or use included `mvnw` wrapper)
4. **Web Browser** (Chrome, Edge, Firefox)

---

## 2. Microsoft SQL Server Setup

### Step 2.1: Enable TCP/IP in SQL Server Configuration Manager
1. Open **SQL Server Configuration Manager**.
2. Expand **SQL Server Network Configuration** → **Protocols for MSSQLSERVER** (or `SQLEXPRESS`).
3. Ensure **TCP/IP** is set to **Enabled**.
4. Double-click **TCP/IP**, open the **IP Addresses** tab, scroll to **IPAll**, and ensure **TCP Port** is set to `1433`.
5. Restart the SQL Server service.

### Step 2.2: Create the Database & User
Open **SQL Server Management Studio (SSMS)** or `sqlcmd` and run:

```sql
-- 1. Create the database
CREATE DATABASE [PortfolioDB];
GO

USE [PortfolioDB];
GO

-- 2. Create login and user
CREATE LOGIN portfolio_admin WITH PASSWORD = 'StrongPassword123!', CHECK_POLICY = ON;
GO

CREATE USER portfolio_admin FOR LOGIN portfolio_admin;
GO

ALTER ROLE db_owner ADD MEMBER portfolio_admin;
GO
```

### Step 2.3: Execute Schema Initialization
Run the initialization script from `database/schema.sql`:
```powershell
sqlcmd -S localhost -d PortfolioDB -U portfolio_admin -P "StrongPassword123!" -i database\schema.sql
```
*(Note: Zero portfolio content records are inserted. The database structure is prepared with tables and indexes).*

---

## 3. Backend Build and Execution

### Option A: Using Maven Wrapper (Recommended)
Navigate to the `backend/` directory:
```powershell
cd backend
.\mvnw.cmd clean package -DskipTests
```

### Option B: Running via Spring Boot CLI
```powershell
.\mvnw.cmd spring-boot:run
```

### Option C: Running the Compiled JAR
```powershell
java -jar target\portfolio-management-system-1.0.0.jar
```

### Option D: First Administrator CLI Provisioning
You can securely create your master administrator during initial startup:
```powershell
java -jar target\portfolio-management-system-1.0.0.jar `
  --create-admin `
  --admin-user=joash `
  --admin-pass="YourStrongPassword123!" `
  --admin-email="bevexj888@gmail.com" `
  --admin-name="Joash Odiwuor"
```

---

## 4. Environment Variables Configuration

Set these environment variables in production to override defaults:

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `DB_HOST` | `localhost` | SQL Server hostname or IP address |
| `DB_PORT` | `1433` | SQL Server TCP port |
| `DB_NAME` | `PortfolioDB` | Database catalog name |
| `DB_USER` | `portfolio_admin` | SQL Server login username |
| `DB_PASSWORD` | `StrongPassword123!` | SQL Server login password |
| `JWT_SECRET` | *(256-bit default)* | 256-bit cryptographically random key |
| `PORT` | `8080` | Backend HTTP port |
| `UPLOAD_DIR` | `uploads` | Directory for stored media |
| `ENABLE_EMAIL_NOTIFICATIONS` | `false` | Enable/disable SMTP notifications |
| `NOTIFICATION_EMAIL` | `bevexj888@gmail.com`| Inquiries target email |
| `SMTP_HOST` | `smtp.gmail.com` | SMTP server used for admin recovery email |
| `SMTP_PORT` | `587` | SMTP server port |
| `SMTP_USERNAME` | *(empty)* | SMTP account username |
| `SMTP_PASSWORD` | *(empty)* | SMTP app password or credential |
| `PASSWORD_RESET_FROM` | `SMTP_USERNAME` | Verified sender address for recovery email |
| `PASSWORD_RESET_URL` | `http://localhost:8080/admin/login.html` | Login page URL; set to the public HTTPS address in production |

---

### Administrator account recovery

The login page's **Forgot username or password?** action emails the username and a single-use password-reset link to the administrator's registered email address. Configure working SMTP credentials and `PASSWORD_RESET_URL` before relying on recovery in production. Reset links expire after 15 minutes; requests are rate-limited, and responses do not reveal whether an email belongs to an administrator.

## 5. IDE Setup Instructions

### 5.1. IntelliJ IDEA
1. Open IntelliJ IDEA → **File** → **Open...**
2. Select the `backend` folder (or the root folder containing `backend/pom.xml`).
3. IntelliJ will detect the Maven project. Wait for dependencies to download.
4. Set Project SDK to **Java 25** (**File** → **Project Structure** → **SDKs**).
5. Open `com.portfolio.PortfolioApplication.java` and click the green **Run** arrow.

### 5.2. Visual Studio Code
1. Install the **Extension Pack for Java** and **Spring Boot Extension Pack**.
2. Open the project folder in VS Code.
3. Open `backend/src/main/java/com/portfolio/PortfolioApplication.java`.
4. Click **Run** or press `F5`.

---

## 6. Accessing the Application

- **Public Portfolio**: Open `frontendport/index.html` in your browser (or visit `http://localhost:8080/index.html`).
- **Administrator Dashboard**: Open `frontendport/admin/login.html` (or `http://localhost:8080/admin/login.html`).
- **Interactive Swagger Documentation**: `http://localhost:8080/swagger-ui.html`.
- **System Health Endpoint**: `http://localhost:8080/actuator/health`.
