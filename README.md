# Enterprise Personal Portfolio Management System

A complete, professional, secure, responsive, and production-ready **Personal Portfolio Management System** designed for a **Diploma in Computer Science student and aspiring software developer**.

Built with a **Java 25 Spring Boot 3** backend, **Microsoft SQL Server** relational database, **Spring Security** authentication, and a responsive **Dark Navy & Electric Blue** frontend interface.

---

## 🌟 Key Highlights

- **Dual-Purpose Design**:
  - High-impact, responsive public presentation for employment opportunities, recruiters, and clients.
  - Dedicated, separate administrator dashboard allowing complete content management without touching code.
- **Strict Production Zero-Fake-Data Architecture**:
  - Initialized with an empty production schema. Zero fabricated projects, skills, services, or fake statistics.
  - Returns genuine database statistics (`0` if no records) and clean, informative empty-state messages.
- **Enterprise-Grade Security**:
  - BCrypt password hashing (cost factor 12) & stateless JJWT authentication.
  - 15-minute lockout rate limiting after 5 consecutive failed login attempts.
  - One-time master administrator provisioning wizard that locks permanently after initial setup.
  - Magic-byte file validation, directory traversal prevention, and extension whitelisting.
- **Complete Public Modules**:
  - Hero introduction with typing effect, About Me biography, Technical Skills with categories & proficiency bars, Professional Services, Project Portfolio with GitHub & demo links, Education, Experience, Certifications, Approved Testimonials, Functional Contact Form with spam honeypot, and downloadable CV.
- **Robust Administrator Dashboard**:
  - Complete Create, Read, Update, and Delete (CRUD) operations across 12 distinct portfolio modules.
  - Support for `DRAFT`, `PUBLISHED`, `UNPUBLISHED`, and `ARCHIVED` publication states.
  - Review and approval workflow for client testimonials.
  - Real-time security and administrative audit logging.

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Backend Framework** | Java 25, Spring Boot 3.5.16 |
| **Security & Auth** | Spring Security 6, JJWT 0.12.6, BCrypt |
| **Persistence & ORM** | Spring Data JPA, Hibernate 6, Jakarta Persistence |
| **Relational Database** | Microsoft SQL Server (`PortfolioDB`) |
| **API Documentation** | OpenAPI 3, Swagger UI (`springdoc-openapi`) |
| **Build System** | Apache Maven 3.9 & Maven Wrapper (`mvnw`) |
| **Frontend** | Semantic HTML5, CSS3 (Custom Dark Navy Theme), ES6 JavaScript (Fetch API) |
| **Testing** | JUnit 5, Spring Boot Test, MockMvc, H2 In-Memory DB |

---

## 📁 Project Directory Structure

```
portfolio-management-system/
│
├── frontendport/                     # Public client & Admin UI
│   ├── index.html                    # Single-page complete portfolio
│   ├── about.html                    # Dedicated About Me subpage
│   ├── projects.html                 # Dedicated Projects subpage
│   ├── services.html                 # Dedicated Services subpage
│   ├── contact.html                  # Dedicated Contact subpage
│   ├── assets/
│   │   ├── css/
│   │   │   ├── style.css             # Dark navy, electric blue core theme
│   │   │   └── responsive.css        # Mobile, tablet, desktop breakpoints
│   │   └── js/
│   │       ├── api.js                # API configuration and fetch wrapper
│   │       └── main.js               # Dynamic DOM rendering & empty states
│   └── admin/
│       ├── login.html                # Login & One-time first-admin setup
│       ├── dashboard.html            # Complete administrative dashboard
│       ├── css/
│       │   └── admin.css             # Admin dashboard dark styling
│       └── js/
│           ├── admin-auth.js         # Session & token lifecycle management
│           └── admin-dashboard.js    # CRUD handlers for all 12 modules
│
├── backend/                          # Java Spring Boot 3 enterprise application
│   ├── pom.xml                       # Maven configuration with all dependencies
│   ├── mvnw / mvnw.cmd               # Cross-platform Maven wrapper
│   └── src/
│       ├── main/
│       │   ├── java/com/portfolio/
│       │   │   ├── PortfolioApplication.java
│       │   │   ├── config/           # OpenAPI, WebMvc, FileStorageProperties
│       │   │   ├── security/         # SecurityConfig, JWT, UserDetails, RateLimiter
│       │   │   ├── entity/           # 20 JPA entities mapped to SQL Server
│       │   │   ├── repository/       # 20 Spring Data JPA repositories
│       │   │   ├── dto/              # Request / Response DTOs
│       │   │   ├── service/          # Business logic, audit logging, file storage
│       │   │   ├── controller/       # 16 RESTful API controllers
│       │   │   └── exception/        # Global exception handling (@RestControllerAdvice)
│       │   └── resources/
│       │       ├── application.yml   # Production configuration
│       │       └── application-test.yml # Isolated testing configuration
│       └── test/                     # Unit and integration test suites
│
├── database/
│   ├── schema.sql                    # Production SQL Server schema
│   └── migrations/                   # Flyway-compatible migrations
│
├── uploads/                          # Stored media (profiles, projects, certificates, cv)
├── documentation/                    # In-depth architectural & deployment guides
│   ├── ARCHITECTURE.md
│   ├── DATABASE_DESIGN.md
│   ├── API_DOCUMENTATION.md
│   ├── SECURITY_GUIDE.md
│   └── DEPLOYMENT_GUIDE.md
├── README.md
└── .gitignore
```

---

## 🚀 Quick Start Guide

### 1. Initialize SQL Server Database
Run the schema script against your SQL Server instance:
```powershell
sqlcmd -S localhost -d PortfolioDB -U portfolio_admin -P "StrongPassword123!" -i database\schema.sql
```

### 2. Build and Run the Backend
Using the included Maven wrapper from the `backend/` directory:
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

To start the updated backend JAR and automatically restart it after repeated health-check failures, run `powershell -ExecutionPolicy Bypass -File .\Start-Backend.ps1` from the project root. If an older portfolio JAR is already healthy, add `-Restart` to replace it with the current recovery JAR. The script defaults to the `mysql` profile for XAMPP/MySQL; use `-DatabaseProfile prod` to run with SQL Server instead. Keep the PowerShell window open; the script writes backend output to `backend\logs\`.

### 3. Initialize First Administrator
On your first run, open:
`http://localhost:8080/admin/login.html` (or `frontendport/admin/login.html`)
The system will detect zero administrators and present the **Initial Setup Wizard**. Once you create your master administrator, the wizard automatically and permanently locks.

### 4. Populate Your Genuine Content
Log in to the **Administrator Dashboard** and populate your personal biography, projects, skills, education, and credentials through the dashboard. All changes will immediately reflect on the public portfolio!

---

## 📄 License
This project is open-source and available under the Apache 2.0 License.
