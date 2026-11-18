/* ==========================================================================
   PORTFOLIO MAIN FRONTEND CONTROLLER
   Dynamically populates all portfolio sections from SQL Server via REST API
   ========================================================================== */

document.addEventListener('DOMContentLoaded', function() {
    initNavigation();
    initTypingEffect();
    initContactForm();
    initScrollReveal();
    loadPortfolioData();
    applyPublicSettings();
    initRecommendationCharCount();
    updateDynamicSocialLinks();
});

/* ==========================================================================
   DYNAMIC SETTINGS & LIVE THEME APPLICATION
   ========================================================================== */
async function applyPublicSettings() {
    const res = await API.getPublicSettings();
    if (!res || !res.success || !res.data) return;

    const s = res.data;
    window.__portfolioSettings = s;
    const root = document.documentElement;

    // A. Apply Theme Colors
    if (s['appearance.primaryColor']) root.style.setProperty('--bg-body', s['appearance.primaryColor']);
    if (s['appearance.secondaryColor']) root.style.setProperty('--bg-secondary', s['appearance.secondaryColor']);
    if (s['appearance.accentColor']) {
        root.style.setProperty('--accent-electric', s['appearance.accentColor']);
        root.style.setProperty('--accent-blue', s['appearance.accentColor']);
    }
    if (s['appearance.textColor']) root.style.setProperty('--text-light', s['appearance.textColor']);
    if (s['appearance.fontFamily']) root.style.setProperty('--font-main', s['appearance.fontFamily']);
    if (s['appearance.buttonRadius']) root.style.setProperty('--btn-radius', s['appearance.buttonRadius']);

    // B. Apply Branding & Meta Text
    if (s['general.brandName']) {
        setText('brandName', s['general.brandName']);
    }
    if (s['general.ownerName']) {
        setText('heroName', s['general.ownerName']);
        setText('aboutName', s['general.ownerName']);
    }
    if (s['general.professionalTitle']) {
        setText('aboutTitle', s['general.professionalTitle']);
    }
    if (s['general.contactEmail']) {
        setText('contactEmail', s['general.contactEmail']);
        setText('aboutEmail', s['general.contactEmail']);
    }
    if (s['general.contactPhone']) {
        setText('contactPhone', s['general.contactPhone']);
    }
    if (s['general.physicalLocation']) {
        setText('contactLocation', s['general.physicalLocation']);
        setText('aboutLocation', s['general.physicalLocation']);
    }

    // C. Section Visibility
    const toggleSection = (id, showKey) => {
        const sec = document.getElementById(id);
        if (sec && s[showKey] === 'false') {
            sec.style.display = 'none';
            // Also hide navigation link
            const nav = document.querySelector(`.nav-links a[href*="#${id}"]`);
            if (nav && nav.parentElement) nav.parentElement.style.display = 'none';
        }
    };

    toggleSection('about', 'homepage.showAbout');
    toggleSection('skills', 'homepage.showSkills');
    toggleSection('services', 'homepage.showServices');
    toggleSection('projects', 'homepage.showProjects');
    toggleSection('education', 'homepage.showEducation');
    toggleSection('experience', 'homepage.showExperience');
    toggleSection('certificates', 'homepage.showCertificates');
    toggleSection('testimonials', 'homepage.showTestimonials');
    toggleSection('contact', 'homepage.showContact');

    // D. Maintenance Mode Screen & Messaging (Only activated if explicitly enabled by admin in Web Settings)
    const maintScreen = document.getElementById('maintenanceScreen');
    const isMaintenance = s['general.maintenanceMode'] === 'true' || s['general.maintenanceMode'] === true;
    if (maintScreen) {
        if (isMaintenance) {
            maintScreen.classList.add('active');
            maintScreen.style.display = 'flex';
            document.body.style.overflow = 'hidden';
            const msgEl = document.getElementById('maintenanceMessageText');
            if (msgEl) {
                msgEl.textContent = s['maintenance.maintenanceNotice'] || 'The portfolio is briefly undergoing scheduled updates. Please check back shortly.';
            }
        } else {
            maintScreen.classList.remove('active');
            maintScreen.style.display = 'none';
            document.body.style.overflow = '';
        }
    }

    // E. Dynamic Footer & Hero Social Links from Admin Settings
    updateDynamicSocialLinks();
}

function initRecommendationCharCount() {
    const textarea = document.getElementById('recMessage');
    const countSpan = document.getElementById('recCharCount');
    if (textarea && countSpan) {
        textarea.addEventListener('input', () => {
            countSpan.textContent = textarea.value.length;
        });
    }
}

/* ==========================================================================
   1. NAVIGATION & MOBILE MENU
   ========================================================================== */
function initNavigation() {
    const menuBtn = document.querySelector('.menu-toggle');
    const navLinks = document.querySelector('.nav-links');

    if (menuBtn && navLinks) {
        menuBtn.setAttribute('aria-expanded', 'false');

        const toggleMenu = (open) => {
            const isOpen = typeof open === 'boolean' ? open : !navLinks.classList.contains('active');
            navLinks.classList.toggle('active', isOpen);
            menuBtn.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
            menuBtn.innerHTML = isOpen ? '✕' : '☰';
            if (isOpen) {
                document.body.style.overflow = 'hidden';
            } else {
                document.body.style.overflow = '';
            }
        };

        menuBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            toggleMenu();
        });

        navLinks.querySelectorAll('a').forEach(link => {
            link.addEventListener('click', () => {
                toggleMenu(false);
            });
        });

        // Close when clicking outside of open mobile menu
        document.addEventListener('click', function(e) {
            if (navLinks.classList.contains('active') && !navLinks.contains(e.target) && !menuBtn.contains(e.target)) {
                toggleMenu(false);
            }
        });

        // Close on Escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && navLinks.classList.contains('active')) {
                toggleMenu(false);
            }
        });
    }

    // Dynamic Active Link Highlighting on Scroll (Scroll Spy)
    function updateActiveNavLink() {
        const scrollY = window.pageYOffset || document.documentElement.scrollTop;
        const totalHeight = document.documentElement.scrollHeight;
        const viewportHeight = window.innerHeight;

        // 1. Bottom of page threshold (highlight Contact)
        if (scrollY + viewportHeight >= totalHeight - 100) {
            highlightNav('contact');
            return;
        }

        // 2. Top of page threshold (highlight Home)
        if (scrollY < 140) {
            highlightNav('home');
            return;
        }

        // 3. Dynamic Section Viewport Intersection
        const sections = document.querySelectorAll('section[id]');
        let currentId = '';

        sections.forEach(sec => {
            const rect = sec.getBoundingClientRect();
            // A section is active if its top has reached under header (<= 140px) and bottom is still visible (>= 140px)
            if (rect.top <= 140 && rect.bottom >= 140) {
                currentId = sec.getAttribute('id');
            }
        });

        if (currentId) {
            highlightNav(currentId);
        }
    }

    function highlightNav(id) {
        if (!id) return;
        document.querySelectorAll('.nav-links a').forEach(link => {
            if (link.classList.contains('nav-button')) return;
            const href = link.getAttribute('href') || '';
            const isMatch = href === `#${id}` ||
                            href.endsWith(`#${id}`) ||
                            href === `${id}.html` ||
                            (id === 'home' && (href === '#home' || href === 'index.html' || href === './'));

            if (isMatch) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });
    }

    // High performance passive scroll listener
    window.addEventListener('scroll', updateActiveNavLink, { passive: true });
    window.addEventListener('resize', updateActiveNavLink, { passive: true });
    setTimeout(updateActiveNavLink, 250);

    // Smooth scroll navigation with header offset easing
    // Handles in-page links (#skills) and cross-page anchor links (index.html#skills)
    function smoothScrollToElement(targetEl, targetId) {
        if (!targetEl) return false;
        const headerOffset = 85;
        const elementPosition = targetEl.getBoundingClientRect().top;
        const offsetPosition = Math.max(0, elementPosition + (window.pageYOffset || document.documentElement.scrollTop) - headerOffset);

        window.scrollTo({
            top: offsetPosition,
            behavior: 'smooth'
        });

        if (targetId) {
            highlightNav(targetId);
            if (window.history && window.history.pushState) {
                window.history.pushState(null, null, '#' + targetId);
            }
        }
        return true;
    }

    // Universal anchor click handler across navbar, logo, buttons, and footer
    const anchorSelectors = '.nav-links a, a.logo, .hero-buttons a[href^="#"], .footer-nav a[href^="#"], .footer-links-list a[href^="#"], .footer-back-to-top, a[href="#home"]';
    document.querySelectorAll(anchorSelectors).forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            const href = this.getAttribute('href') || '';
            if (href.startsWith('admin/')) return; // Allow normal navigation to admin portal
            if (href === '#' || href === '') return;

            let targetId = '';
            if (href.startsWith('#')) {
                targetId = href.substring(1);
            } else if (href.includes('#')) {
                const parts = href.split('#');
                const currentPage = window.location.pathname.split('/').pop() || 'index.html';
                if (parts[0] === '' || parts[0] === 'index.html' || parts[0] === currentPage) {
                    targetId = parts[1];
                }
            }

            if (targetId) {
                const targetEl = document.getElementById(targetId);
                if (targetEl) {
                    e.preventDefault();
                    smoothScrollToElement(targetEl, targetId);
                } else if (href.startsWith('#')) {
                    // Element not found on this page (e.g. user clicked #skills from about.html)
                    e.preventDefault();
                    window.location.href = 'index.html#' + targetId;
                }
            }
        });
    });

    // Handle initial hash on page load or hash change
    function handleHashJump() {
        if (window.location.hash) {
            const hashId = window.location.hash.substring(1);
            const targetEl = document.getElementById(hashId);
            if (targetEl) {
                setTimeout(() => {
                    smoothScrollToElement(targetEl, hashId);
                }, 150);
            }
        }
    }

    window.addEventListener('load', handleHashJump);
    window.addEventListener('hashchange', handleHashJump);
}

/* ==========================================================================
   2. TYPING EFFECT
   ========================================================================== */
function initTypingEffect() {
    const typingElement = document.getElementById('typingHeadline');
    if (!typingElement) return;

    let phrases = ["Diploma in Computer Science", "Aspiring Software Developer", "Full-Stack Web Developer"];
    let phraseIndex = 0;
    let charIndex = 0;
    let isDeleting = false;

    function type() {
        const currentPhrase = phrases[phraseIndex];
        if (isDeleting) {
            typingElement.textContent = currentPhrase.substring(0, charIndex - 1);
            charIndex--;
        } else {
            typingElement.textContent = currentPhrase.substring(0, charIndex + 1);
            charIndex++;
        }

        let speed = isDeleting ? 40 : 90;

        if (!isDeleting && charIndex === currentPhrase.length) {
            speed = 2000;
            isDeleting = true;
        } else if (isDeleting && charIndex === 0) {
            isDeleting = false;
            phraseIndex = (phraseIndex + 1) % phrases.length;
            speed = 500;
        }

        setTimeout(type, speed);
    }

    type();
}

/* ==========================================================================
   3. LOAD GENUINE PORTFOLIO CONTENT FROM SQL SERVER
   ========================================================================== */
async function loadPortfolioData() {
    const res = await API.getPublicPortfolio();

    if (!res || !res.success || !res.data) {
        console.warn('Backend currently offline or unreachable. Displaying empty states.');
        renderEmptyStates();
        updateDynamicSocialLinks();
        return;
    }

    const data = res.data;
    window.__portfolioSocialLinks = data.socialLinks;
    window.__portfolioProfile = data.profile;

    // A. Render Personal Profile
    renderProfile(data.profile);

    // B. Render About Information
    renderAbout(data.about);

    // C. Render Skills
    renderSkills(data.skillCategories, data.skills);

    // D. Render Services
    renderServices(data.services);

    // E. Render Projects
    renderProjects(data.projects);

    // F. Render Education
    renderEducation(data.education);

    // G. Render Experience
    renderExperience(data.experience);

    // H. Render Certificates
    renderCertificates(data.certificates);

    // I. Render Testimonials
    renderTestimonials(data.testimonials);

    // J. Render Social Links
    renderSocialLinks(data.socialLinks);
}

function renderProfile(profile) {
    if (!profile) {
        // Placeholders when unconfigured
        setText('heroName', 'Joash Odiwuor');
        setText('heroHeadline', 'Diploma in Computer Science');
        setText('heroBio', 'I am a passionate Computer Science student interested in software development, web development, database management, and networking.');
        setText('aboutName', 'Joash Odiwuor');
        setText('aboutCourse', 'Diploma in Computer Science');
        setText('aboutLocation', 'Kenya');
        setText('aboutEmail', 'bevexj888@gmail.com');
        return;
    }

    setText('heroName', profile.fullName);
    setText('heroHeadline', profile.professionalHeadline || profile.professionalTitle);
    setText('heroBio', profile.bioSummary || 'Aspiring Software Developer and Web Developer.');

    setText('aboutName', profile.fullName);
    setText('aboutCourse', profile.professionalTitle);
    setText('aboutLocation', profile.location);
    setText('aboutEmail', profile.email);
    setText('contactEmail', profile.email);
    setText('contactPhone', profile.phone || '+254 108612398');
    setText('contactLocation', profile.location);

    if (profile.profilePhotoUrl) {
        const photoEl = document.getElementById('profilePhoto');
        if (photoEl) photoEl.src = profile.profilePhotoUrl;
        const aboutPhotoEl = document.getElementById('aboutProfilePhoto');
        if (aboutPhotoEl) aboutPhotoEl.src = profile.profilePhotoUrl;
    }

    if (profile.cvFileUrl) {
        const cvButtons = document.querySelectorAll('.cv-download-btn');
        cvButtons.forEach(btn => {
            btn.href = profile.cvFileUrl;
            btn.setAttribute('target', '_blank');
            btn.style.display = 'inline-flex';
        });
    }
}

function renderAbout(about) {
    if (!about) {
        setText('aboutParagraph1', 'Pursuing a Diploma in Computer Science with a strong focus on software engineering, database systems, and modern web application development.');
        return;
    }

    setText('aboutSectionTitle', about.sectionTitle);
    setText('aboutSubtitle', about.subtitle || 'Get To Know Me');
    setText('aboutParagraph1', about.paragraph1);

    if (about.paragraph2) {
        const p2 = document.getElementById('aboutParagraph2');
        if (p2) { p2.textContent = about.paragraph2; p2.style.display = 'block'; }
    }
    if (about.paragraph3) {
        const p3 = document.getElementById('aboutParagraph3');
        if (p3) { p3.textContent = about.paragraph3; p3.style.display = 'block'; }
    }
}

function renderSkills(categories, skills) {
    const container = document.getElementById('skillsContainer');
    if (!container) return;

    if ((!categories || categories.length === 0) && (!skills || skills.length === 0)) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">&lt;/&gt;</div>
                <h4>No technical skills available</h4>
                <p>Technical skills and proficiency ratings will appear once configured by the portfolio owner.</p>
            </div>`;
        return;
    }

    container.innerHTML = '';

    if (categories && categories.length > 0) {
        categories.forEach(cat => {
            const card = document.createElement('div');
            card.className = 'skill-card';

            let skillsHtml = '';
            if (cat.skills && cat.skills.length > 0) {
                skillsHtml = cat.skills.map(s => `
                    <div style="margin-bottom: 0.85rem;">
                        <div style="display:flex; justify-content:space-between; font-size:0.85rem; margin-bottom:0.25rem;">
                            <span>${escapeHtml(s.skillName)}</span>
                            <span>${s.proficiencyPercentage}%</span>
                        </div>
                        <div class="skill-bar">
                            <div class="skill-progress" style="width: ${s.proficiencyPercentage}%"></div>
                        </div>
                    </div>
                `).join('');
            } else {
                skillsHtml = '<p style="color:var(--text-muted); font-size:0.85rem;">No skills added in this category yet.</p>';
            }

            card.innerHTML = `
                <div class="skill-icon">${escapeHtml(cat.iconClass || '{ }')}</div>
                <h3>${escapeHtml(cat.categoryName)}</h3>
                <p>${escapeHtml(cat.description || '')}</p>
                <div class="skills-list">${skillsHtml}</div>
            `;
            container.appendChild(card);
        });
    }
}

function renderServices(services) {
    const container = document.getElementById('servicesContainer');
    if (!container) return;

    if (!services || services.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">💼</div>
                <h4>No professional services added yet</h4>
                <p>Freelance and professional services will be listed here once published.</p>
            </div>`;
        return;
    }

    container.innerHTML = services.map(s => `
        <div class="service-card">
            <div class="service-icon">${escapeHtml(s.iconClass || '⚡')}</div>
            <h3>${escapeHtml(s.title)}</h3>
            <p>${escapeHtml(s.shortDescription)}</p>
        </div>
    `).join('');
}

function renderProjects(projects) {
    const container = document.getElementById('projectsContainer');
    if (!container) return;

    if (!projects || projects.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">🚀</div>
                <h4>No projects have been added yet</h4>
                <p>Projects, source code repositories, and live demonstration links will appear here once published.</p>
            </div>`;
        return;
    }

    container.innerHTML = projects.map(p => {
        const techBadges = p.technologies
            ? p.technologies.split(',').map(t => `<span>${escapeHtml(t.trim())}</span>`).join('')
            : '';

        const imageHtml = p.featuredImageUrl
            ? `<img src="${p.featuredImageUrl}" alt="${escapeHtml(p.title)}" loading="lazy">`
            : `<div style="font-size:2.5rem; color:var(--accent-electric); opacity:0.6;">&lt;/&gt;</div>`;

        return `
            <div class="project-card">
                <div class="project-image">
                    <span class="badge-cat">${escapeHtml(p.category)}</span>
                    ${imageHtml}
                </div>
                <div class="project-content">
                    <h3>${escapeHtml(p.title)}</h3>
                    <p>${escapeHtml(p.shortDescription)}</p>
                    <div class="project-technologies">${techBadges}</div>
                    <div class="project-links">
                        ${p.projectLink ? `<a href="${p.projectLink}" target="_blank" class="project-link">Live Demo →</a>` : ''}
                        ${p.githubUrl ? `<a href="${p.githubUrl}" target="_blank" class="project-link">GitHub Code ↗</a>` : ''}
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function renderEducation(educationList) {
    const container = document.getElementById('educationContainer');
    if (!container) return;

    if (!educationList || educationList.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">🎓</div>
                <h4>No education records added yet</h4>
                <p>Academic milestones, diplomas, and institutions will be presented here.</p>
            </div>`;
        return;
    }

    container.innerHTML = educationList.map(e => `
        <div class="timeline-item">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
                <span class="date">${escapeHtml(e.startDate)} – ${e.current ? 'Present' : escapeHtml(e.endDate || '')}</span>
                <h3>${escapeHtml(e.degreeOrDiploma)}</h3>
                <h4>${escapeHtml(e.institutionName)}, ${escapeHtml(e.location)}</h4>
                <p>${escapeHtml(e.description || e.fieldOfStudy)}</p>
            </div>
        </div>
    `).join('');
}

function renderExperience(experienceList) {
    const container = document.getElementById('experienceContainer');
    if (!container) return;

    if (!experienceList || experienceList.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">💼</div>
                <h4>No professional experience records added yet</h4>
                <p>Work history and internships will be shown here once updated.</p>
            </div>`;
        return;
    }

    container.innerHTML = experienceList.map(exp => `
        <div class="timeline-item">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
                <span class="date">${escapeHtml(exp.startDate)} – ${exp.currentRole ? 'Present' : escapeHtml(exp.endDate || '')}</span>
                <h3>${escapeHtml(exp.jobTitle)}</h3>
                <h4>${escapeHtml(exp.companyName)} • ${escapeHtml(exp.location)}</h4>
                <p>${escapeHtml(exp.responsibilities)}</p>
                ${exp.technologiesUsed ? `<p style="margin-top:0.5rem; font-size:0.85rem; color:var(--accent-electric);"><strong>Stack:</strong> ${escapeHtml(exp.technologiesUsed)}</p>` : ''}
            </div>
        </div>
    `).join('');
}

function renderCertificates(certs) {
    const container = document.getElementById('certificatesContainer');
    if (!container) return;

    if (!certs || certs.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">📜</div>
                <h4>No certificates uploaded</h4>
                <p>Industry certifications and verification credentials will appear here.</p>
            </div>`;
        return;
    }

    container.innerHTML = certs.map(c => `
        <div class="cert-card">
            <h3>${escapeHtml(c.certificateTitle)}</h3>
            <h4>${escapeHtml(c.issuingOrganization)} • Issued ${escapeHtml(c.issueDate)}</h4>
            ${c.credentialUrl ? `<a href="${c.credentialUrl}" target="_blank" class="project-link">Verify Credential →</a>` : ''}
        </div>
    `).join('');
}

function renderTestimonials(testimonials) {
    const container = document.getElementById('testimonialsContainer');
    if (!container) return;

    if (!testimonials || testimonials.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="grid-column: 1/-1; text-align: center; padding: 50px 20px; background: rgba(17, 34, 64, 0.4); border-radius: 12px; border: 1px dashed rgba(255,255,255,0.1);">
                <div style="font-size: 2.8rem; margin-bottom: 12px;">🌟</div>
                <h4 style="color: #fff; margin-bottom: 6px;">No recommendations published yet</h4>
                <p style="color: var(--text-light); max-width: 500px; margin: 0 auto 20px;">
                    Have we collaborated, built a project, or studied together? Be the first to share your experience!
                </p>
                <button type="button" class="btn primary-btn" onclick="openRecommendationModal()">
                    ✍️ Share Your Experience
                </button>
            </div>`;
        return;
    }

    container.innerHTML = testimonials.map(t => {
        const rating = t.rating || 5;
        const stars = '★'.repeat(rating);
        const name = t.fullName || t.clientName || 'Anonymous Collaborator';
        const position = t.position || t.clientTitle || '';
        const organization = t.organization || t.clientCompany || '';
        const message = t.message || t.testimonialText || '';
        const avatar = (t.photoUrl || t.clientAvatarUrl)
            ? `<img src="${t.photoUrl || t.clientAvatarUrl}" alt="${escapeHtml(name)}" class="testimonial-avatar" width="48" height="48" loading="lazy" style="width:48px; height:48px; border-radius:50%; object-fit:cover;">`
            : `<div style="width:48px; height:48px; border-radius:50%; background:rgba(0,210,255,0.2); color:#00d2ff; font-weight:700; display:flex; align-items:center; justify-content:center; font-size:1.15rem;">${escapeHtml(name.charAt(0))}</div>`;

        return `
            <div class="testimonial-card" style="background: var(--bg-card, #112240); padding: 25px; border-radius: 12px; border: 1px solid rgba(255,255,255,0.08); display:flex; flex-direction:column; justify-content:space-between;">
                <div>
                    ${t.relationship ? `<span style="display:inline-block; font-size:0.75rem; font-weight:600; padding:2px 8px; border-radius:10px; background:rgba(0,210,255,0.15); color:var(--accent-electric,#00d2ff); margin-bottom:10px;">${escapeHtml(t.relationship)}</span>` : ''}
                    <div class="rating-stars" style="color: #f59e0b; margin-bottom: 12px;">${stars}</div>
                    <p class="testimonial-text" style="color: var(--text-light); font-style: italic; line-height: 1.6; margin-bottom: 18px;">
                        "${escapeHtml(message)}"
                    </p>
                </div>
                <div class="testimonial-author" style="display: flex; align-items: center; gap: 14px; border-top: 1px solid rgba(255,255,255,0.06); padding-top: 15px;">
                    ${avatar}
                    <div>
                        <h4 style="color: #fff; margin: 0 0 2px; font-size: 0.98rem;">${escapeHtml(name)}</h4>
                        <p style="color: var(--text-muted, #94a3b8); margin: 0; font-size: 0.82rem;">
                            ${escapeHtml(position)}${organization ? ` • ${escapeHtml(organization)}` : ''}
                        </p>
                        ${t.organizationWebsite ? `<a href="${t.organizationWebsite}" target="_blank" rel="noopener" style="color:var(--accent-electric); font-size:0.8rem; text-decoration:none;">Visit Website ↗</a>` : ''}
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

/* ==========================================================================
   PUBLIC RECOMMENDATION SUBMISSION MODAL
   ========================================================================== */
function openRecommendationModal() {
    const modal = document.getElementById('recommendationModal');
    if (!modal) return;
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
    loadRecCaptcha();
}

function closeRecommendationModal() {
    const modal = document.getElementById('recommendationModal');
    if (!modal) return;
    modal.style.display = 'none';
    document.body.style.overflow = '';
}

async function loadRecCaptcha() {
    const qSpan = document.getElementById('recCaptchaQuestion');
    const tokenInput = document.getElementById('recCaptchaToken');
    const answerInput = document.getElementById('recCaptchaAnswer');
    if (!qSpan) return;

    if (answerInput) answerInput.value = '';
    qSpan.textContent = 'Loading challenge...';

    try {
        const res = await API.getTestimonialCaptcha();
        if (res && res.success && res.data && res.data.token) {
            qSpan.textContent = res.data.question;
            if (tokenInput) tokenInput.value = res.data.token;
            return;
        }
    } catch (e) {
        console.warn('Could not fetch server CAPTCHA, using client challenge', e);
    }

    // High-reliability fallback challenge
    const a = Math.floor(Math.random() * 8) + 2;
    const b = Math.floor(Math.random() * 8) + 2;
    const ts = Date.now();
    const token = btoa(`${ts}:${a + b}`);
    qSpan.textContent = `What is ${a} + ${b}?`;
    if (tokenInput) tokenInput.value = token;
}

async function handleRecommendationSubmit(event) {
    event.preventDefault();

    const alertBox = document.getElementById('recAlert');
    const submitBtn = document.getElementById('recSubmitBtn');
    const consent = document.getElementById('recConsent').checked;

    if (!consent) {
        alertBox.className = 'alert-box error';
        alertBox.textContent = 'Publication consent is mandatory to submit your testimonial.';
        alertBox.style.display = 'block';
        return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'Submitting... 🚀';

    const payload = {
        fullName: document.getElementById('recFullName').value.trim(),
        email: document.getElementById('recEmail').value.trim(),
        organization: document.getElementById('recOrganization').value.trim(),
        position: document.getElementById('recPosition').value.trim(),
        relationship: document.getElementById('recRelationship').value,
        organizationWebsite: document.getElementById('recOrgWebsite').value.trim(),
        photoUrl: document.getElementById('recPhotoUrl').value.trim(),
        message: document.getElementById('recMessage').value.trim(),
        consentToPublish: true,
        website: document.getElementById('recHoneypot') ? document.getElementById('recHoneypot').value : '',
        captchaToken: document.getElementById('recCaptchaToken') ? document.getElementById('recCaptchaToken').value : '',
        captchaAnswer: document.getElementById('recCaptchaAnswer') ? document.getElementById('recCaptchaAnswer').value.trim() : ''
    };

    try {
        const res = await API.submitTestimonial(payload);
        if (res && res.success) {
            alertBox.className = 'alert-box success';
            alertBox.innerHTML = '<strong>Thank you for sharing your experience!</strong><br>Your recommendation has been received and will be reviewed before publication.';
            alertBox.style.display = 'block';
            document.getElementById('recommendationForm').reset();
            setTimeout(() => {
                closeRecommendationModal();
                alertBox.style.display = 'none';
            }, 3000);
        } else {
            alertBox.className = 'alert-box error';
            alertBox.textContent = res ? (res.message || 'Submission failed. Please check inputs.') : 'Connection error';
            alertBox.style.display = 'block';
            loadRecCaptcha();
        }
    } catch (err) {
        alertBox.className = 'alert-box error';
        alertBox.textContent = 'Network error submitting recommendation.';
        alertBox.style.display = 'block';
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Submit Recommendation 🚀';
    }
}

/* ==========================================================================
   DYNAMIC SOCIAL LINKS & FOOTER LINKS (Strictly Managed by Admin)
   ========================================================================== */
function isGenuineUrl(url) {
    if (!url || typeof url !== 'string') return false;
    const u = url.trim();
    if (!u || u === '#' || u === 'https://' || u === 'http://') return false;
    // Filter out bare default root domain placeholders without handle/profile
    const bareRoots = [
        'https://github.com', 'https://github.com/', 
        'https://linkedin.com', 'https://linkedin.com/', 
        'https://twitter.com', 'https://twitter.com/', 
        'https://x.com', 'https://x.com/'
    ];
    if (bareRoots.includes(u.toLowerCase())) return false;
    return true;
}

function getSocialIcon(platformName, iconClass) {
    if (iconClass && iconClass.length <= 4 && !iconClass.startsWith('fa-')) {
        return iconClass; // custom emoji or short symbol
    }
    const p = (platformName || '').toLowerCase().trim();
    if (p.includes('email') || p.includes('mail')) return '✉️';
    if (p.includes('github') || p.includes('git')) return '🐙';
    if (p.includes('linkedin') || p.includes('linkin')) return '💼';
    if (p.includes('twitter') || p.includes('twiter') || p === 'x') return '🐦';
    if (p.includes('whatsapp') || p.includes('wa')) return '💬';
    if (p.includes('youtube')) return '▶️';
    if (p.includes('instagram') || p.includes('insta')) return '📸';
    if (p.includes('discord')) return '🎮';
    if (p.includes('telegram')) return '✈️';
    if (p.includes('phone') || p.includes('call') || p.includes('tel')) return '📞';
    if (p.includes('portfolio') || p.includes('web') || p.includes('site')) return '🌐';
    return '🔗';
}

function updateDynamicSocialLinks() {
    const s = window.__portfolioSettings || {};
    const dbLinks = window.__portfolioSocialLinks || [];
    const prof = window.__portfolioProfile || {};

    const list = [];
    const seen = new Set();

    // 1. ONLY links explicitly added and enabled by admin in Admin Dashboard -> Social Links table
    if (Array.isArray(dbLinks) && dbLinks.length > 0) {
        dbLinks.filter(l => l && l.active !== false && isGenuineUrl(l.url)).forEach(l => {
            const norm = (l.platformName || '').toLowerCase().trim();
            if (norm) seen.add(norm);
            list.push({
                platform: l.platformName || 'Social Link',
                url: l.url,
                icon: getSocialIcon(l.platformName, l.iconClass)
            });
        });
    }

    // 2. Augment from Admin Website Settings or Profile ONLY if explicitly configured with actual handles
    const email = s['contact.publicEmail'] || s['general.contactEmail'] || prof.email;
    const github = s['contact.githubUrl'] || prof.gitHubUrl;
    const linkedin = s['contact.linkedinUrl'] || prof.linkedInUrl;
    const twitter = s['contact.twitterUrl'] || prof.twitterUrl;
    const whatsapp = s['contact.whatsapp'] || prof.phone;
    const youtube = s['contact.youtubeUrl'];

    if (!seen.has('email') && email && email.includes('@') && !email.includes('example.com')) {
        list.push({
            platform: 'Email',
            url: email.startsWith('mailto:') ? email : `mailto:${email}`,
            icon: '✉️'
        });
        seen.add('email');
    }

    if (!seen.has('github') && isGenuineUrl(github)) {
        list.push({
            platform: 'GitHub',
            url: github,
            icon: '🐙'
        });
        seen.add('github');
    }

    if (!seen.has('linkedin') && !seen.has('linkin') && isGenuineUrl(linkedin)) {
        list.push({
            platform: 'LinkedIn',
            url: linkedin,
            icon: '💼'
        });
        seen.add('linkedin');
    }

    if (!seen.has('twitter') && !seen.has('twiter') && !seen.has('x') && isGenuineUrl(twitter)) {
        list.push({
            platform: 'Twitter / X',
            url: twitter,
            icon: '🐦'
        });
        seen.add('twitter');
    }

    if (!seen.has('whatsapp') && whatsapp && whatsapp.trim().length >= 7) {
        const clean = whatsapp.replace(/[^0-9]/g, '');
        if (clean.length >= 7) {
            list.push({
                platform: 'WhatsApp',
                url: `https://wa.me/${clean}`,
                icon: '💬'
            });
            seen.add('whatsapp');
        }
    }

    if (!seen.has('youtube') && isGenuineUrl(youtube)) {
        list.push({
            platform: 'YouTube',
            url: youtube,
            icon: '▶️'
        });
        seen.add('youtube');
    }

    // Render into all Footer Social containers (.footer-social)
    const footerContainers = document.querySelectorAll('.footer-social');
    footerContainers.forEach(container => {
        if (list.length === 0) {
            container.innerHTML = '';
            return;
        }
        container.innerHTML = list.map(item => `
            <a href="${escapeHtml(item.url)}" 
               ${item.url.startsWith('mailto:') ? '' : 'target="_blank" rel="noopener noreferrer"'} 
               class="footer-social-link" 
               title="${escapeHtml(item.platform)}"
               aria-label="${escapeHtml(item.platform)}">
                ${item.icon}
            </a>
        `).join('');
    });

    // Render into all Hero Social containers (.dynamic-social-links)
    const heroContainers = document.querySelectorAll('.dynamic-social-links');
    heroContainers.forEach(container => {
        if (list.length === 0) {
            container.innerHTML = '';
            return;
        }
        container.innerHTML = list.filter(item => !item.url.startsWith('mailto:')).map(item => `
            <a href="${escapeHtml(item.url)}" 
               target="_blank" 
               rel="noopener noreferrer" 
               title="${escapeHtml(item.platform)}">
                ${escapeHtml(item.platform)}
            </a>
        `).join('');
    });

    // Update Footer Contact Info block with admin email (if configured)
    document.querySelectorAll('.footer-contact-info').forEach(info => {
        const mailLink = info.querySelector('.footer-mail-link, a[href^="mailto:"]');
        const emailItem = info.querySelector('.footer-email-item');
        if (email && email.includes('@') && !email.includes('example.com')) {
            if (mailLink) {
                mailLink.href = email.startsWith('mailto:') ? email : `mailto:${email}`;
                mailLink.textContent = email.replace('mailto:', '');
            }
            if (emailItem) emailItem.style.display = 'flex';
        } else {
            if (emailItem) emailItem.style.display = 'none';
        }
    });

    // Toggle footer social label based on whether admin added active links
    document.querySelectorAll('.footer-social-block').forEach(block => {
        const label = block.querySelector('.footer-social-label');
        if (label) {
            label.style.display = list.length > 0 ? 'block' : 'none';
        }
    });

    // Update Footer Contact Info location
    const location = s['general.physicalLocation'] || prof.location;
    if (location) {
        document.querySelectorAll('.footer-contact-item').forEach(item => {
            const icon = item.querySelector('.fci');
            if (icon && icon.textContent.includes('📍')) {
                const textSpan = item.querySelector('.fci-value') || item.querySelector('span:not(.fci)');
                if (textSpan) textSpan.textContent = location;
            }
        });
    }
}

function renderSocialLinks(links) {
    if (links) {
        window.__portfolioSocialLinks = links;
    }
    updateDynamicSocialLinks();
}

function renderEmptyStates() {
    renderSkills([], []);
    renderServices([]);
    renderProjects([]);
    renderEducation([]);
    renderExperience([]);
    renderCertificates([]);
    renderTestimonials([]);
}

/* ==========================================================================
   4. CONTACT FORM SUBMISSION
   ========================================================================== */
function initContactForm() {
    const form = document.getElementById('contactForm');
    if (!form) return;

    form.addEventListener('submit', async function(e) {
        e.preventDefault();

        const btn = form.querySelector('button[type="submit"]');
        const originalText = btn.textContent;
        btn.disabled = true;
        btn.textContent = 'Sending...';

        const payload = {
            fullName: document.getElementById('contactName').value.trim(),
            email: document.getElementById('contactEmailInput').value.trim(),
            subject: document.getElementById('contactSubject').value.trim(),
            message: document.getElementById('contactMessage').value.trim(),
            website: document.getElementById('contactWebsite') ? document.getElementById('contactWebsite').value : ''
        };

        const result = await API.sendContactMessage(payload);

        btn.disabled = false;
        btn.textContent = originalText;

        if (result && result.success) {
            showToast('Thank you! Your message has been sent successfully.', 'success');
            form.reset();
        } else {
            showToast(result.message || 'Failed to send message. Please try again.', 'error');
        }
    });
}

function setText(id, text) {
    const el = document.getElementById(id);
    if (el && text) el.textContent = text;
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>'"]/g, tag => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        "'": '&#39;',
        '"': '&quot;'
    }[tag] || tag));
}

/* ==========================================================================
   SCROLL REVEAL ANIMATIONS
   ========================================================================== */
function initScrollReveal() {
    const revealElements = document.querySelectorAll('.section-container, .project-card, .service-card, .skill-category-card, .timeline-item, .cert-card, .testimonial-card');
    revealElements.forEach(el => el.classList.add('reveal'));

    if ('IntersectionObserver' in window) {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('show');
                }
            });
        }, { threshold: 0.1 });

        revealElements.forEach(el => observer.observe(el));
    } else {
        revealElements.forEach(el => el.classList.add('show'));
    }
}

/* ==========================================================================
   PERFORMANCE: Intersection Observer for section-by-section loading
   ========================================================================== */
(function lazyLoadSections() {
    if (!('IntersectionObserver' in window)) return;
    const sections = document.querySelectorAll('section, .section, [data-lazy-load]');
    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                entry.target.classList.add('section-visible');
                observer.unobserve(entry.target);
            }
        });
    }, { rootMargin: '100px 0px', threshold: 0.05 });
    sections.forEach((s) => observer.observe(s));
})();

/* Register service worker with immediate activation */
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('./sw.js', { updateViaCache: 'none' })
            .then((reg) => {
                // Force new SW to activate immediately
                if (reg.waiting) reg.waiting.postMessage('SKIP_WAITING');
                reg.addEventListener('updatefound', () => {
                    const newWorker = reg.installing;
                    if (newWorker) {
                        newWorker.addEventListener('statechange', () => {
                            if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
                                newWorker.postMessage('SKIP_WAITING');
                            }
                        });
                    }
                });
            })
            .catch(() => {});
    });
}

/* ==========================================================================
   INSTANT PUBLISH — Version polling for real-time public portal updates
   Polls /api/public/version every 15 seconds. When admin publishes changes,
   the version bumps and we reload portfolio data without a full page refresh.
   ========================================================================== */
(function startVersionPolling() {
    if (typeof API_BASE === 'undefined') return;

    let knownVersion = null;
    let pollInterval = null;

    async function checkVersion() {
        try {
            const res = await fetch(API_BASE + '/api/public/version', {
                cache: 'no-store',
                signal: AbortSignal.timeout ? AbortSignal.timeout(5000) : undefined
            });
            if (!res.ok) return;
            const data = await res.json();
            const serverVersion = data.version || data.data?.version;

            if (knownVersion === null) {
                // First poll — just record baseline version
                knownVersion = serverVersion;
                return;
            }

            if (serverVersion !== knownVersion) {
                knownVersion = serverVersion;
                console.log('[PublishSync] New version detected:', serverVersion, '— reloading portfolio data...');

                // Evict SW API caches for public endpoints
                if ('caches' in window) {
                    const cacheNames = await caches.keys();
                    for (const name of cacheNames) {
                        const cache = await caches.open(name);
                        const keys = await cache.keys();
                        for (const req of keys) {
                            if (req.url.includes('/api/public/')) {
                                await cache.delete(req);
                            }
                        }
                    }
                }

                // Re-fetch all portfolio data and re-render page sections
                await reloadPublicData();
            }
        } catch (err) {
            // Silently ignore network errors during polling
        }
    }

    async function reloadPublicData() {
        try {
            // Re-run the same data loading functions used on first load
            if (typeof loadPortfolioData === 'function') await loadPortfolioData();
            if (typeof applyPublicSettings === 'function') await applyPublicSettings();
            if (typeof updateDynamicSocialLinks === 'function') await updateDynamicSocialLinks();
            // Briefly flash a subtle "Updated" badge so users see fresh content
            showUpdateBadge();
        } catch (err) {
            console.warn('[PublishSync] Data reload failed:', err);
        }
    }

    function showUpdateBadge() {
        let badge = document.getElementById('publishSyncBadge');
        if (!badge) {
            badge = document.createElement('div');
            badge.id = 'publishSyncBadge';
            badge.style.cssText = [
                'position:fixed', 'bottom:1.2rem', 'right:1.2rem',
                'background:linear-gradient(135deg,#38BDF8,#2563EB)',
                'color:#fff', 'padding:0.45rem 1rem', 'border-radius:999px',
                'font-size:0.78rem', 'font-weight:600', 'letter-spacing:0.03em',
                'box-shadow:0 4px 24px rgba(56,189,248,0.4)',
                'z-index:9999', 'opacity:0',
                'transition:opacity 0.4s ease',
                'pointer-events:none'
            ].join(';');
            badge.textContent = '✦ Content Updated';
            document.body.appendChild(badge);
        }
        badge.style.opacity = '1';
        setTimeout(() => { badge.style.opacity = '0'; }, 3000);
    }

    // Start polling after a short delay (don't compete with initial load)
    setTimeout(() => {
        checkVersion(); // baseline
        pollInterval = setInterval(checkVersion, 15000); // every 15s
    }, 3000);

    // Pause polling when tab is hidden, resume when visible
    document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
            clearInterval(pollInterval);
        } else {
            checkVersion(); // immediate check on tab focus
            pollInterval = setInterval(checkVersion, 15000);
        }
    });
})();
