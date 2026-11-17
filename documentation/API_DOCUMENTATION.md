# RESTful API Documentation

## 1. API Architecture & Standards

- **Base URL**: `http://localhost:8080/api`
- **Data Format**: `application/json` (UTF-8)
- **Authentication**: HTTP Bearer JWT (`Authorization: Bearer <token>`)
- **Interactive Documentation**: Swagger UI available at `http://localhost:8080/swagger-ui.html`
- **OpenAPI 3 JSON Spec**: `http://localhost:8080/v3/api-docs`

All API endpoints return standard response wrappers:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-11-05T09:30:00"
}
```

---

## 2. Public Endpoints (No Authentication Required)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/public/portfolio` | Retrieve full published portfolio content from SQL Server |
| `POST` | `/api/public/contact` | Submit contact form inquiry (with honeypot spam protection) |
| `GET` | `/api/auth/status` | Check if initial first-admin setup is required |
| `POST` | `/api/auth/setup-first-admin` | Secure one-time master administrator provisioning |
| `POST` | `/api/auth/login` | Administrator login with BCrypt verification and rate limiting |
| `POST` | `/api/auth/request-password-reset` | Send account recovery email without revealing whether the address exists |
| `POST` | `/api/auth/reset-password` | Set a new password with the single-use email token |

### 2.1. Submit Contact Inquiry
**`POST /api/public/contact`**
```json
{
  "fullName": "Alice Johnson",
  "email": "alice@company.com",
  "subject": "Interview Request: Junior Developer",
  "message": "Hi Joash, we would like to invite you for an interview regarding a Software Developer position.",
  "website": ""
}
```

---

## 3. Administrator Endpoints (Requires `ROLE_ADMIN`)

Header required: `Authorization: Bearer <your-jwt-token>`

### 3.1. Dashboard & Audit
- `GET /api/admin/dashboard/stats`: Returns genuine SQL Server counts for all entities.
- `GET /api/admin/dashboard/audit-logs`: Returns top 50 recent administrative audit events.

### 3.2. Personal Profile & About
- `GET /api/admin/profile`: Get personal profile details.
- `POST /api/admin/profile`: Save or update profile information.
- `GET /api/admin/about`: Get about me biography.
- `POST /api/admin/about`: Save or update about me biography.

### 3.3. Project Management
- `GET /api/admin/projects`: List all projects (any state).
- `POST /api/admin/projects`: Create project.
- `PUT /api/admin/projects/{id}`: Update project.
- `DELETE /api/admin/projects/{id}`: Delete project.

### 3.4. Technical Skills & Categories
- `GET /api/admin/skill-categories`: Get categories with nested skills.
- `POST /api/admin/skill-categories`: Create new category.
- `DELETE /api/admin/skill-categories/{id}`: Delete category.
- `GET /api/admin/skills`: Flat list of all skills.
- `POST /api/admin/skills`: Add skill.
- `PUT /api/admin/skills/{id}`: Update skill.
- `DELETE /api/admin/skills/{id}`: Delete skill.

### 3.5. Services, Education, Experience, Certificates
- `GET / POST / PUT / DELETE /api/admin/services`
- `GET / POST / PUT / DELETE /api/admin/education`
- `GET / POST / PUT / DELETE /api/admin/experience`
- `GET / POST / PUT / DELETE /api/admin/certificates`
- `GET / POST / PUT / DELETE /api/admin/achievements`

### 3.6. Testimonials Review Workflow
- `GET /api/admin/testimonials`: List all testimonials.
- `POST /api/admin/testimonials`: Add testimonial.
- `PATCH /api/admin/testimonials/{id}/approve`: Approve testimonial for public display.
- `PATCH /api/admin/testimonials/{id}/reject`: Reject testimonial.
- `DELETE /api/admin/testimonials/{id}`: Delete testimonial.

### 3.7. Contact Messages
- `GET /api/admin/messages`: List all messages.
- `PATCH /api/admin/messages/{id}/read`: Mark as read.
- `PATCH /api/admin/messages/{id}/unread`: Mark as unread.
- `PATCH /api/admin/messages/{id}/archive`: Archive message.
- `DELETE /api/admin/messages/{id}`: Delete message.

### 3.8. Secure File Uploads
- `POST /api/admin/upload`: Multi-part file upload with magic number validation.
  - Form parameters: `file` (Multipart file), `category` (`PROFILE_PHOTO`, `PROJECT_IMAGE`, `CERTIFICATE`, `CV`).
