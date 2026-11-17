# Cybersecurity and Hardening Guide

## 1. Authentication & Hashing Architecture

### 1.1. BCrypt Password Hashing
- Administrator passwords are never stored in plaintext.
- BCrypt algorithm with a work factor (log rounds) of 12 (`BCryptPasswordEncoder(12)`).
- Protects against offline dictionary and rainbow table attacks.

### 1.2. JSON Web Token (JWT) Security
- Built on JJWT 0.12.x using HMAC-SHA256 signatures with a 256-bit secure secret key.
- Tokens carry user identity, assigned role authorities, and a 24-hour expiration timestamp.
- Validation includes cryptographic signature verification and expiration checking on every protected request.

---

## 2. Brute Force Protection & Rate Limiting

- `LoginRateLimiterService` monitors login attempts per username and client IP.
- After 5 consecutive failed attempts, the account is placed on an automatic 15-minute lockout.
- All failed and locked attempts generate an audit log record with the client IP address.

---

## 3. Administrator Provisioning Security

- Administrator registration is **never** publicly exposed.
- No default insecure credentials exist in the codebase.
- Two secure provisioning mechanisms:
  1. **CLI Flag Provisioning**: Run the backend with `--create-admin --admin-user=NAME --admin-pass=STRONG_PASS --admin-email=EMAIL`.
  2. **One-Time Web Setup Wizard**: If `Admins` table is completely empty (`count == 0`), the web UI displays a one-time provisioning wizard. Once the first administrator is created, this endpoint is permanently and irreversibly locked.

---

## 4. Multi-Layer File Upload Security

All uploads undergo strict verification before being written to disk:
1. **Extension Whitelisting**: Prohibits executable/script files (`.exe`, `.php`, `.jsp`, `.sh`, `.bat`, etc.). Allowed: `.jpg`, `.jpeg`, `.png`, `.webp`, `.pdf`.
2. **Directory Traversal Protection**: Filenames are sanitized and path traversal sequences (`..`, `/`, `\`) are stripped.
3. **Magic Byte / File Signature Verification**: Verifies true file headers:
   - PNG: `89 50 4E 47`
   - JPEG: `FF D8 FF`
   - GIF: `47 49 46 38`
   - PDF: `%PDF` (`25 50 44 46`)
4. **UUID File Renaming**: Every uploaded file is assigned a randomly generated UUID prefix to prevent file collision, overwriting, and predictable URL probing.
5. **Size Restrictions**: Max 5MB for images, 10MB for CV documents.

---

## 5. Web Application Protections

- **SQL Injection Prevention**: 100% of database queries use parameterized prepared statements managed through Hibernate ORM and Spring Data JPA.
- **Cross-Site Scripting (XSS)**: Client-side output escaping (`escapeHtml`) combined with Content Security Policy (CSP) headers.
- **Cross-Site Request Forgery (CSRF)**: All state-changing administrative operations require HTTP Bearer JWT tokens in authorization headers, which are immune to cross-origin credential reuse.
- **Anti-Spam Honeypot**: The contact form features an invisible honeypot field (`website`). Automated bot submissions that populate this hidden field are rejected.
- **Sensitive Actuator Protection**: Actuator management endpoints (`/actuator/**`) are restricted exclusively to administrators with `ROLE_ADMIN`.
