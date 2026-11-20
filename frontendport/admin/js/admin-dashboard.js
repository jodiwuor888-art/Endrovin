/* ==========================================================================
   ADMINISTRATOR DASHBOARD CONTROLLER v3.0
   Full CRUD Operations · Drag-and-Drop Upload · Mobile Support
   ========================================================================== */

let currentAdminUser = null;

document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadAdminUserInfo();
    initSidebarNavigation();
    loadDashboardStats();
    loadAuditLogs();
    loadNotifications();
    initDragDropPhotoUpload();
    initCvDragDrop();
});

/* ============================================================
   MOBILE SIDEBAR TOGGLE
   ============================================================ */
function toggleSidebar() {
    const sb = document.getElementById('sidebar');
    const ov = document.getElementById('sidebarOverlay');
    if (sb) sb.classList.toggle('open');
    if (ov) ov.classList.toggle('active');
}
function closeSidebar() {
    const sb = document.getElementById('sidebar');
    const ov = document.getElementById('sidebarOverlay');
    if (sb) sb.classList.remove('open');
    if (ov) ov.classList.remove('active');
}

/* Close sidebar when a nav item is clicked on mobile or tablet */
document.addEventListener('click', e => {
    const item = e.target.closest('.nav-item[data-view]');
    if (item && window.innerWidth <= 1024) closeSidebar();
});

/* ============================================================
   PROFILE PHOTO — DRAG & DROP + CAMERA
   ============================================================ */
function initDragDropPhotoUpload() {
    const zone = document.getElementById('profilePhotoDropzone');
    if (!zone) return;

    zone.addEventListener('dragover', e => {
        e.preventDefault();
        zone.classList.add('drag-over');
    });
    zone.addEventListener('dragleave', () => zone.classList.remove('drag-over'));
    zone.addEventListener('drop', e => {
        e.preventDefault();
        zone.classList.remove('drag-over');
        const files = e.dataTransfer.files;
        if (files && files.length > 0 && files[0].type.startsWith('image/')) {
            processProfilePhotoFile(files[0]);
        } else {
            showAdminToast('Please drop a valid image file (JPG, PNG, WEBP, GIF).', 'warning');
        }
    });
}

function openCameraCapture() {
    const inp = document.getElementById('profilePhotoCameraInput');
    if (inp) inp.click();
}

/* ============================================================
   CV DRAG & DROP
   ============================================================ */
function initCvDragDrop() {
    const zone = document.getElementById('cvDropzone');
    if (!zone) return;
    zone.addEventListener('dragover', e => { e.preventDefault(); zone.classList.add('drag-over'); });
    zone.addEventListener('dragleave', () => zone.classList.remove('drag-over'));
    zone.addEventListener('drop', e => {
        e.preventDefault();
        zone.classList.remove('drag-over');
        const files = e.dataTransfer.files;
        if (files && files.length > 0 && files[0].type === 'application/pdf') {
            handleCvUploadFile(files[0]);
        } else {
            showAdminToast('Please drop a valid PDF file.', 'warning');
        }
    });
}

function handleCvDragOver(e) { e.preventDefault(); }
function handleCvDragLeave(e) {}
function handleCvDrop(e) {
    e.preventDefault();
    const files = e.dataTransfer.files;
    if (files && files.length > 0) {
        if (files[0].type === 'application/pdf') {
            handleCvUploadFile(files[0]);
        } else {
            showAdminToast('Please drop a PDF file.', 'warning');
        }
    }
}
async function handleCvUploadFile(file) {
    const progress = document.getElementById('cvUploadProgress');
    if (progress) { progress.style.display = 'block'; progress.textContent = `Uploading ${file.name}…`; }
    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', 'CV');
    const res = await adminFetch('/api/admin/upload', { method: 'POST', body: formData });
    if (res && res.success && res.data) {
        document.getElementById('cvStatusText').textContent = `✅ CV uploaded: ${file.name}`;
        const link = document.getElementById('cvViewLink');
        if (link) { link.href = res.data.fileUrl; link.style.display = 'inline-flex'; }
        if (progress) progress.textContent = '';
        showAdminToast('CV uploaded successfully!');
    } else {
        if (progress) progress.textContent = '';
        showAdminToast(res ? (res.message || 'Upload failed.') : 'Upload error.', 'error');
    }
}

function loadAdminUserInfo() {
    const raw = localStorage.getItem('adminUser');
    if (raw) {
        try {
            currentAdminUser = JSON.parse(raw);
            const displayName = currentAdminUser.fullName || currentAdminUser.username || 'Administrator';
            const initial = displayName[0].toUpperCase();

            const nameEl = document.getElementById('adminNameDisplay');
            if (nameEl) nameEl.textContent = displayName;

            const avatarEl = document.getElementById('adminAvatarInitial');
            if (avatarEl) avatarEl.textContent = initial;

            // Also update sidebar profile strip
            const sidebarName = document.getElementById('sidebarNameDisplay');
            if (sidebarName) sidebarName.textContent = displayName;
            const sidebarAvatar = document.getElementById('sidebarAvatarInitial');
            if (sidebarAvatar) sidebarAvatar.textContent = initial;
        } catch (e) {}
    }
}

function initSidebarNavigation() {
    const navItems = document.querySelectorAll('.nav-item[data-view]');
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const viewName = item.getAttribute('data-view');
            switchView(viewName);
        });
    });
}

function switchView(viewName) {
    // Update nav active states
    document.querySelectorAll('.nav-item[data-view]').forEach(item => {
        if (item.getAttribute('data-view') === viewName) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    // Update view containers
    document.querySelectorAll('.admin-view').forEach(view => {
        view.classList.remove('active');
    });

    const activeView = document.getElementById(`view-${viewName}`);
    if (activeView) {
        activeView.classList.add('active');
    }
    const breadcrumbEl = document.getElementById('viewBreadcrumb');
    if (breadcrumbEl) {
        breadcrumbEl.textContent = `Control Centre / ${formatViewTitle(viewName)}`;
    }

    // Set page title
    const titleEl = document.getElementById('viewTitle');
    if (titleEl) {
        titleEl.textContent = formatViewTitle(viewName);
    }

    // Lazy load view data
    switch (viewName) {
        case 'overview': loadDashboardStats(); loadAuditLogs(); break;
        case 'profile': loadProfileData(); break;
        case 'about': loadAboutData(); break;
        case 'projects': loadProjectsTable(); break;
        case 'skills': loadSkillsTable(); break;
        case 'services': loadServicesTable(); break;
        case 'education': loadEducationTable(); break;
        case 'experience': loadExperienceTable(); break;
        case 'certificates': loadCertificatesTable(); break;
        case 'achievements': loadAchievementsTable(); break;
        case 'testimonials': loadTestimonialsTable(); break;
        case 'messages': loadMessagesTable(); break;
        case 'settings': loadSettings(); loadPasskeyDevicesTable(); break;
        case 'social': loadSocialTable(); break;
        case 'cv': loadCvManagement(); break;
        case 'analytics': loadExecutiveAnalytics(); break;
        case 'leads': loadCrmLeads(); break;
        case 'legal': loadLegalDocuments(); break;
        case 'privacy': loadPrivacyAndCompliance(); break;
        case 'security': loadSecurityCenter(); break;
        case 'ai-tools': loadAiTools(); break;
    }
}

/* Alias used by some buttons */
function navigateToView(viewName) { switchView(viewName); }

function formatViewTitle(view) {
    const map = {
        'overview': 'Dashboard Overview',
        'profile': 'Personal Profile & Contact Info',
        'about': 'About Me & Biography',
        'projects': 'Project Portfolio Management',
        'skills': 'Skills & Categories Management',
        'services': 'Professional Services',
        'education': 'Education & Qualifications',
        'experience': 'Professional Experience',
        'certificates': 'Certificates & Credentials',
        'achievements': 'Honors, Awards & Achievements',
        'testimonials': 'Client Reviews & Approval Workflow',
        'messages': 'Contact Inquiries',
        'settings': 'Website Settings & Meta',
        'social': 'Professional Social Links',
        'cv': 'Curriculum Vitae (CV) Management',
        'analytics': 'Executive Analytics & Traffic',
        'leads': 'Client Leads & CRM Pipeline',
        'legal': 'Legal Documents & Terms Governance',
        'privacy': 'Privacy & Consent Compliance',
        'security': 'Security Control Centre & Threat Defense',
        'ai-tools': 'AI Assistant & Content Tools'
    };
    return map[view] || 'Dashboard';
}

/* ==========================================================================
   1. DASHBOARD STATS & AUDIT LOGS
   ========================================================================== */
async function loadDashboardStats() {
    const res = await adminFetch('/api/admin/dashboard/stats');
    if (!res || !res.success) return;

    const s = res.data;
    setStatText('statProjects', s.totalProjects);
    setStatText('statSkills', s.totalSkills);
    setStatText('statServices', s.totalServices);
    setStatText('statEducation', s.totalEducation);
    setStatText('statExperience', s.totalExperience);
    setStatText('statCertificates', s.totalCertificates);
    setStatText('statTestimonials', s.totalTestimonials);
    setStatText('statMessages', s.totalMessages);

    // Update badges
    const unreadBadge = document.getElementById('badgeUnreadMessages');
    if (unreadBadge) {
        unreadBadge.textContent = s.unreadMessages;
        unreadBadge.style.display = s.unreadMessages > 0 ? 'inline-block' : 'none';
    }
    const pendingBadge = document.getElementById('badgePendingTestimonials');
    if (pendingBadge) {
        pendingBadge.textContent = s.pendingTestimonials;
        pendingBadge.style.display = s.pendingTestimonials > 0 ? 'inline-block' : 'none';
    }
}

async function loadAuditLogs() {
    const res = await adminFetch('/api/admin/dashboard/audit-logs');
    const tableBody = document.getElementById('auditLogsTableBody');
    if (!tableBody) return;

    if (!res || !res.success || !res.data || res.data.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--admin-text-muted);">No audit events recorded yet.</td></tr>`;
        return;
    }

    tableBody.innerHTML = res.data.map(log => `
        <tr>
            <td><strong>${escapeHtml(log.action)}</strong></td>
            <td>${escapeHtml(log.entityName)}</td>
            <td>${escapeHtml(log.details || '')}</td>
            <td>${escapeHtml(log.adminUsername)}</td>
            <td>${new Date(log.timestamp).toLocaleString()}</td>
            <td style="text-align:center;">
                <button type="button" class="btn-admin btn-admin-danger btn-sm" onclick="deleteAuditLogEntry(${log.id})" title="Delete audit log record" style="padding:4px 8px; font-size:0.8rem;">🗑️</button>
            </td>
        </tr>
    `).join('');
}

async function deleteAuditLogEntry(id) {
    if (!confirm(`Are you sure you want to delete audit log #${id}?`)) return;
    try {
        const res = await adminFetch(`/api/admin/dashboard/audit-logs/${id}`, { method: 'DELETE' });
        if (res && res.success) {
            showToast('Audit log entry deleted successfully', 'success');
            loadAuditLogs();
        } else {
            showToast((res && res.message) || 'Failed to delete audit log', 'error');
        }
    } catch (err) {
        showToast(err.message || 'Error deleting audit log', 'error');
    }
}

async function confirmClearAllAuditLogs() {
    if (!confirm('⚠️ Are you sure you want to clear ALL security and administrative audit logs? This action cannot be undone.')) return;
    try {
        const res = await adminFetch('/api/admin/dashboard/audit-logs', { method: 'DELETE' });
        if (res && res.success) {
            showToast('All audit logs cleared successfully', 'success');
            loadAuditLogs();
        } else {
            showToast((res && res.message) || 'Failed to clear audit logs', 'error');
        }
    } catch (err) {
        showToast(err.message || 'Error clearing audit logs', 'error');
    }
}

function setStatText(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val !== undefined ? val : 0;
}

/* ==========================================================================
   2. PROFILE MANAGEMENT
   ========================================================================== */
async function loadProfileData() {
    const res = await adminFetch('/api/admin/profile');
    if (res && res.success && res.data) {
        const p = res.data;
        setVal('profFullName', p.fullName);
        setVal('profHeadline', p.professionalHeadline);
        setVal('profTitle', p.professionalTitle);
        setVal('profLocation', p.location);
        setVal('profEmail', p.email);
        setVal('profPhone', p.phone);
        setVal('profGitHub', p.gitHubUrl);
        setVal('profLinkedIn', p.linkedInUrl);
        setVal('profBio', p.bioSummary);
        if (p.profilePhotoUrl) {
            const preview = document.getElementById('profilePhotoPreview');
            if (preview) preview.src = p.profilePhotoUrl;
        }
    }
}

async function saveProfileForm(e) {
    e.preventDefault();
    const payload = {
        fullName: document.getElementById('profFullName').value.trim(),
        professionalHeadline: document.getElementById('profHeadline').value.trim(),
        professionalTitle: document.getElementById('profTitle').value.trim(),
        location: document.getElementById('profLocation').value.trim(),
        email: document.getElementById('profEmail').value.trim(),
        phone: document.getElementById('profPhone').value.trim(),
        gitHubUrl: document.getElementById('profGitHub').value.trim(),
        linkedInUrl: document.getElementById('profLinkedIn').value.trim(),
        bioSummary: document.getElementById('profBio').value.trim(),
        active: true
    };

    const res = await adminFetch('/api/admin/profile', {
        method: 'POST',
        body: JSON.stringify(payload)
    });

    if (res && res.success) {
        showAdminToast('Profile details updated successfully!');
    } else {
        alert(res.message || 'Failed to update profile.');
    }
}

async function uploadProfilePhoto(input) {
    if (!input.files || input.files.length === 0) return;
    await processProfilePhotoFile(input.files[0]);
}

async function processProfilePhotoFile(file) {
    // Validate type
    const allowed = ['image/jpeg','image/png','image/webp','image/gif','image/avif'];
    if (!allowed.includes(file.type) && !file.type.startsWith('image/')) {
        showAdminToast('Please select a valid image file (JPG, PNG, WEBP, GIF).', 'error');
        return;
    }
    // Validate size (5 MB)
    if (file.size > 5 * 1024 * 1024) {
        showAdminToast('Image too large. Max 5 MB allowed.', 'error');
        return;
    }

    // Show preview immediately using object URL
    const preview = document.getElementById('profilePhotoPreview');
    if (preview) {
        const localUrl = URL.createObjectURL(file);
        preview.src = localUrl;
    }

    // Show progress
    const progress = document.getElementById('photoUploadProgress');
    if (progress) { progress.style.display = 'block'; progress.textContent = '⏫ Uploading…'; }

    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', 'PROFILE_PHOTO');

    const res = await adminFetch('/api/admin/upload', {
        method: 'POST',
        body: formData
    });

    if (progress) progress.style.display = 'none';

    if (res && res.success && res.data) {
        const url = res.data.fileUrl;
        if (preview) preview.src = url;

        // Auto-save to profile
        await adminFetch('/api/admin/profile', {
            method: 'POST',
            body: JSON.stringify({
                fullName: document.getElementById('profFullName')?.value.trim() || currentAdminUser?.fullName || 'Joash Odiwuor',
                professionalHeadline: document.getElementById('profHeadline')?.value.trim() || '',
                professionalTitle: document.getElementById('profTitle')?.value.trim() || 'Software Developer',
                location: document.getElementById('profLocation')?.value.trim() || 'Kenya',
                email: document.getElementById('profEmail')?.value.trim() || '',
                profilePhotoUrl: url,
                active: true
            })
        });

        showAdminToast('✅ Profile photo uploaded and saved!');
    } else {
        showAdminToast(res ? (res.message || 'Upload failed. Check the backend.') : 'Upload error.', 'error');
        // Restore previous photo on error
        if (preview) preview.src = 'assets/img/Joash.jpeg';
    }
}

/* ==========================================================================
   3. ABOUT SECTION MANAGEMENT
   ========================================================================== */
async function loadAboutData() {
    const res = await adminFetch('/api/admin/about');
    if (res && res.success && res.data) {
        const a = res.data;
        setVal('aboutTitleInput', a.sectionTitle);
        setVal('aboutSubtitleInput', a.subtitle);
        setVal('aboutP1Input', a.paragraph1);
        setVal('aboutP2Input', a.paragraph2);
        setVal('aboutP3Input', a.paragraph3);
        setVal('aboutYearsExp', a.yearsExperience);
        setVal('aboutProjectsCompleted', a.projectsCompleted);
        setVal('aboutClients', a.satisfiedClients);
        setVal('aboutStatusSelect', a.status);
    }
}

async function saveAboutForm(e) {
    e.preventDefault();
    const payload = {
        sectionTitle: document.getElementById('aboutTitleInput').value.trim(),
        subtitle: document.getElementById('aboutSubtitleInput').value.trim(),
        paragraph1: document.getElementById('aboutP1Input').value.trim(),
        paragraph2: document.getElementById('aboutP2Input').value.trim(),
        paragraph3: document.getElementById('aboutP3Input').value.trim(),
        yearsExperience: parseInt(document.getElementById('aboutYearsExp').value || '0', 10),
        projectsCompleted: parseInt(document.getElementById('aboutProjectsCompleted').value || '0', 10),
        satisfiedClients: parseInt(document.getElementById('aboutClients').value || '0', 10),
        status: document.getElementById('aboutStatusSelect').value
    };

    const res = await adminFetch('/api/admin/about', {
        method: 'POST',
        body: JSON.stringify(payload)
    });

    if (res && res.success) {
        showAdminToast('About information updated successfully!');
    } else {
        alert(res.message || 'Failed to save about section.');
    }
}

/* ==========================================================================
   4. PROJECTS CRUD
   ========================================================================== */
async function loadProjectsTable() {
    const res = await adminFetch('/api/admin/projects');
    const tbody = document.getElementById('projectsTableBody');
    if (!tbody) return;

    if (!res || !res.success || !res.data || res.data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 2rem; color:var(--admin-text-muted);">No projects added yet. Click "Add Project" to create one.</td></tr>`;
        return;
    }

    tbody.innerHTML = res.data.map(p => `
        <tr>
            <td><strong>${escapeHtml(p.title)}</strong></td>
            <td><span class="badge" style="background:rgba(0,180,216,0.15); color:var(--admin-accent);">${escapeHtml(p.category)}</span></td>
            <td><code>${escapeHtml(p.technologies)}</code></td>
            <td>${p.featured ? '⭐ Featured' : 'Standard'}</td>
            <td><span class="badge badge-${p.status.toLowerCase()}">${p.status}</span></td>
            <td>
                <div class="action-btns">
                    <button class="btn-admin btn-admin-secondary" onclick="editProjectModal(${p.id})">Edit</button>
                    <button class="btn-admin btn-admin-danger" onclick="deleteProject(${p.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function openAddProjectModal() {
    document.getElementById('projectModalTitle').textContent = 'Add New Project';
    document.getElementById('projectForm').reset();
    document.getElementById('projId').value = '';
    openModal('projectModal');
}

async function editProjectModal(id) {
    const res = await adminFetch('/api/admin/projects');
    if (!res || !res.success) return;
    const project = res.data.find(p => p.id === id);
    if (!project) return;

    document.getElementById('projectModalTitle').textContent = 'Edit Project';
    setVal('projId', project.id);
    setVal('projTitle', project.title);
    setVal('projCategory', project.category);
    setVal('projTech', project.technologies);
    setVal('projShortDesc', project.shortDescription);
    setVal('projFullDesc', project.detailedDescription || '');
    setVal('projDemoLink', project.projectLink || '');
    setVal('projGithubLink', project.githubUrl || '');
    setVal('projImageUrl', project.featuredImageUrl || '');
    setVal('projStatus', project.status);
    document.getElementById('projFeatured').checked = project.featured;

    openModal('projectModal');
}

async function handleProjectSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('projId').value;
    const payload = {
        title: document.getElementById('projTitle').value.trim(),
        category: document.getElementById('projCategory').value.trim(),
        technologies: document.getElementById('projTech').value.trim(),
        shortDescription: document.getElementById('projShortDesc').value.trim(),
        detailedDescription: document.getElementById('projFullDesc').value.trim(),
        projectLink: document.getElementById('projDemoLink').value.trim(),
        githubUrl: document.getElementById('projGithubLink').value.trim(),
        featuredImageUrl: document.getElementById('projImageUrl').value.trim(),
        status: document.getElementById('projStatus').value,
        featured: document.getElementById('projFeatured').checked
    };

    const url = id ? `/api/admin/projects/${id}` : '/api/admin/projects';
    const method = id ? 'PUT' : 'POST';

    const res = await adminFetch(url, {
        method: method,
        body: JSON.stringify(payload)
    });

    if (res && res.success) {
        closeModal('projectModal');
        showAdminToast(id ? 'Project updated successfully!' : 'Project created successfully!');
        loadProjectsTable();
        loadDashboardStats();
    } else {
        alert(res.message || 'Error saving project.');
    }
}

async function deleteProject(id) {
    if (!confirm('Are you sure you want to delete this project?')) return;
    const res = await adminFetch(`/api/admin/projects/${id}`, { method: 'DELETE' });
    if (res && res.success) {
        showAdminToast('Project deleted successfully.');
        loadProjectsTable();
        loadDashboardStats();
    }
}

async function uploadProjectImage(input) {
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', 'PROJECT_IMAGE');

    const res = await adminFetch('/api/admin/upload', {
        method: 'POST',
        body: formData
    });

    if (res && res.success && res.data) {
        document.getElementById('projImageUrl').value = res.data.fileUrl;
        showAdminToast('Project screenshot uploaded successfully!');
    } else {
        alert(res.message || 'Upload failed.');
    }
}

/* ==========================================================================
   5. SKILLS & CATEGORIES CRUD
   ========================================================================== */
let allSkillCategoriesCache = [];

async function loadSkillsTable() {
    const res = await adminFetch('/api/admin/skill-categories');
    const tbody = document.getElementById('skillsTableBody');
    const categorySelect = document.getElementById('skillCategorySelect');
    if (!tbody) return;

    if (!res || !res.success || !res.data || res.data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding: 2rem; color:var(--admin-text-muted);">No skill categories found. Click "Add Category" first.</td></tr>`;
        return;
    }

    allSkillCategoriesCache = res.data;

    if (categorySelect) {
        categorySelect.innerHTML = res.data.map(c => `<option value="${c.id}">${escapeHtml(c.categoryName)}</option>`).join('');
    }

    let rowsHtml = '';
    res.data.forEach(cat => {
        if (!cat.skills || cat.skills.length === 0) {
            rowsHtml += `
                <tr>
                    <td><strong>${escapeHtml(cat.categoryName)}</strong></td>
                    <td colspan="3" style="color:var(--admin-text-muted);">No skills in this category yet.</td>
                    <td>
                        <button class="btn-admin btn-admin-danger" onclick="deleteCategory(${cat.id})">Delete Category</button>
                    </td>
                </tr>
            `;
        } else {
            cat.skills.forEach(skill => {
                rowsHtml += `
                    <tr>
                        <td>${escapeHtml(cat.categoryName)}</td>
                        <td><strong>${escapeHtml(skill.skillName)}</strong></td>
                        <td>
                            <div style="display:flex; align-items:center; gap:0.5rem;">
                                <span>${skill.proficiencyPercentage}%</span>
                                <div style="width:100px; height:6px; background:rgba(255,255,255,0.1); border-radius:3px;">
                                    <div style="width:${skill.proficiencyPercentage}%; height:100%; background:var(--admin-accent); border-radius:3px;"></div>
                                </div>
                            </div>
                        </td>
                        <td><span class="badge badge-${skill.status.toLowerCase()}">${skill.status}</span></td>
                        <td>
                            <div class="action-btns" style="display:flex; gap:6px;">
                                <button class="btn-admin btn-admin-secondary" onclick="editSkillModal(${skill.id})">Edit</button>
                                <button class="btn-admin btn-admin-danger" onclick="deleteSkill(${skill.id})">Delete</button>
                            </div>
                        </td>
                    </tr>
                `;
            });
        }
    });

    tbody.innerHTML = rowsHtml;
}

function openAddCategoryModal() {
    document.getElementById('categoryForm').reset();
    openModal('categoryModal');
}

async function handleCategorySubmit(e) {
    e.preventDefault();
    const payload = {
        categoryName: document.getElementById('catName').value.trim(),
        iconClass: document.getElementById('catIcon').value.trim(),
        description: document.getElementById('catDesc').value.trim()
    };

    const res = await adminFetch('/api/admin/skill-categories', {
        method: 'POST',
        body: JSON.stringify(payload)
    });

    if (res && res.success) {
        closeModal('categoryModal');
        showAdminToast('Skill category created!');
        loadSkillsTable();
        loadDashboardStats();
    } else {
        alert(res.message || 'Error adding category.');
    }
}

async function deleteCategory(id) {
    if (!confirm('Are you sure you want to delete this category and all its skills?')) return;
    const res = await adminFetch(`/api/admin/skill-categories/${id}`, { method: 'DELETE' });
    if (res && res.success) {
        showAdminToast('Category deleted.');
        loadSkillsTable();
        loadDashboardStats();
    }
}

function openAddSkillModal() {
    document.getElementById('skillForm').reset();
    document.getElementById('skillId').value = '';
    const title = document.getElementById('skillModalTitle');
    if (title) title.textContent = 'Add Technical Skill';
    openModal('skillModal');
}

function editSkillModal(id) {
    let targetSkill = null;
    let targetCatId = null;
    allSkillCategoriesCache.forEach(cat => {
        if (cat.skills) {
            const found = cat.skills.find(s => s.id === id);
            if (found) {
                targetSkill = found;
                targetCatId = cat.id;
            }
        }
    });

    if (!targetSkill) return;

    document.getElementById('skillId').value = targetSkill.id;
    document.getElementById('skillCategorySelect').value = targetSkill.categoryId || targetCatId;
    document.getElementById('skillNameInput').value = targetSkill.skillName;
    document.getElementById('skillProficiencyInput').value = targetSkill.proficiencyPercentage;
    document.getElementById('skillLevelSelect').value = targetSkill.proficiencyLevel || 'ADVANCED';
    document.getElementById('skillStatusSelect').value = targetSkill.status || 'PUBLISHED';

    const title = document.getElementById('skillModalTitle');
    if (title) title.textContent = 'Edit Technical Skill';
    openModal('skillModal');
}

async function handleSkillSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('skillId').value;
    const payload = {
        categoryId: parseInt(document.getElementById('skillCategorySelect').value, 10),
        skillName: document.getElementById('skillNameInput').value.trim(),
        proficiencyPercentage: parseInt(document.getElementById('skillProficiencyInput').value, 10),
        proficiencyLevel: document.getElementById('skillLevelSelect').value,
        status: document.getElementById('skillStatusSelect').value
    };

    const url = id ? `/api/admin/skills/${id}` : '/api/admin/skills';
    const method = id ? 'PUT' : 'POST';

    const res = await adminFetch(url, {
        method: method,
        body: JSON.stringify(payload)
    });

    if (res && res.success) {
        closeModal('skillModal');
        showAdminToast(id ? 'Technical skill updated successfully!' : 'Technical skill added successfully!');
        loadSkillsTable();
        loadDashboardStats();
    } else {
        alert(res.message || 'Error saving skill.');
    }
}

async function deleteSkill(id) {
    if (!confirm('Delete this skill?')) return;
    const res = await adminFetch(`/api/admin/skills/${id}`, { method: 'DELETE' });
    if (res && res.success) {
        showAdminToast('Skill deleted.');
        loadSkillsTable();
        loadDashboardStats();
    }
}

/* ==========================================================================
   6. SERVICES CRUD
   ========================================================================== */
let allServicesCache = [];

async function loadServicesTable() {
    const res = await adminFetch('/api/admin/services');
    const tbody = document.getElementById('servicesTableBody');
    if (!tbody) return;

    if (!res || !res.success || !res.data || res.data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:2rem; color:var(--admin-text-muted);">No services added yet.</td></tr>`;
        return;
    }

    allServicesCache = res.data;

    tbody.innerHTML = res.data.map(s => `
        <tr>
            <td><strong>${escapeHtml(s.title)}</strong></td>
            <td>${escapeHtml(s.shortDescription)}</td>
            <td><code>${escapeHtml(s.iconClass || '⚡')}</code></td>
            <td><span class="badge badge-${s.status.toLowerCase()}">${s.status}</span></td>
            <td>
                <div class="action-btns" style="display:flex; gap:6px;">
                    <button class="btn-admin btn-admin-secondary" onclick="editServiceModal(${s.id})">Edit</button>
                    <button class="btn-admin btn-admin-danger" onclick="deleteService(${s.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function openAddServiceModal() {
    document.getElementById('serviceForm').reset();
    document.getElementById('serviceId').value = '';
    const title = document.getElementById('serviceModalTitle');
    if (title) title.textContent = 'Add Professional Service';
    openModal('serviceModal');
}

function editServiceModal(id) {
    const s = allServicesCache.find(item => item.id === id);
    if (!s) return;

    document.getElementById('serviceId').value = s.id;
    document.getElementById('serviceTitleInput').value = s.title;
    document.getElementById('serviceIconInput').value = s.iconClass || '';
    document.getElementById('serviceDescInput').value = s.shortDescription || '';
    document.getElementById('serviceStatusSelect').value = s.status || 'PUBLISHED';

    const title = document.getElementById('serviceModalTitle');
    if (title) title.textContent = 'Edit Professional Service';
    openModal('serviceModal');
}

async function handleServiceSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('serviceId').value;
    const payload = {
        title: document.getElementById('serviceTitleInput').value.trim(),
        iconClass: document.getElementById('serviceIconInput').value.trim(),
        shortDescription: document.getElementById('serviceDescInput').value.trim(),
        status: document.getElementById('serviceStatusSelect').value
    };

    const url = id ? `/api/admin/services/${id}` : '/api/admin/services';
    const method = id ? 'PUT' : 'POST';

    const res = await adminFetch(url, {
        method: method,
        body: JSON.stringify(payload)
    });

    if (res && res.success) {
        closeModal('serviceModal');
        showAdminToast(id ? 'Service updated successfully!' : 'Service added successfully!');
        loadServicesTable();
        loadDashboardStats();
    } else {
        alert(res.message || 'Error saving service.');
    }
}

async function deleteService(id) {
    if (!confirm('Delete this service?')) return;
    const res = await adminFetch(`/api/admin/services/${id}`, { method: 'DELETE' });
    if (res && res.success) {
        showAdminToast('Service deleted.');
        loadServicesTable();
        loadDashboardStats();
    }
}

/* ==========================================================================
   7. EDUCATION & EXPERIENCE CRUD
   ========================================================================== */
let allEducationCache = [];

async function loadEducationTable() {
    const res = await adminFetch('/api/admin/education');
    const tbody = document.getElementById('educationTableBody');
    if (!tbody) return;

    if (!res || !res.success || !res.data || res.data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:2rem; color:var(--admin-text-muted);">No education records added yet.</td></tr>`;
        return;
    }

    allEducationCache = res.data;

    tbody.innerHTML = res.data.map(e => `
        <tr>
            <td><strong>${escapeHtml(e.degreeOrDiploma)}</strong></td>
            <td>${escapeHtml(e.institutionName)}</td>
            <td>${escapeHtml(e.location)}</td>
            <td>${escapeHtml(e.startDate)} – ${e.current ? 'Present' : escapeHtml(e.endDate || '')}</td>
            <td><span class="badge badge-${e.status.toLowerCase()}">${e.status}</span></td>
            <td>
                <div class="action-btns" style="display:flex; gap:6px;">
                    <button class="btn-admin btn-admin-secondary" onclick="editEducationModal(${e.id})">Edit</button>
                    <button class="btn-admin btn-admin-danger" onclick="deleteEducation(${e.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function openAddEducationModal() {
    document.getElementById('educationForm').reset();
    document.getElementById('eduId').value = '';
    const title = document.getElementById('educationModalTitle');
    if (title) title.textContent = 'Add Education Record';
    openModal('educationModal');
}

function editEducationModal(id) {
    const e = allEducationCache.find(item => item.id === id);
    if (!e) return;

    document.getElementById('eduId').value = e.id;
    document.getElementById('eduDegree').value = e.degreeOrDiploma;
    document.getElementById('eduSchool').value = e.institutionName;
    document.getElementById('eduLocation').value = e.location;
    document.getElementById('eduField').value = e.fieldOfStudy;
    document.getElementById('eduStart').value = e.startDate;
    document.getElementById('eduEnd').value = e.endDate || '';
    document.getElementById('eduCurrent').checked = e.current;
    document.getElementById('eduDesc').value = e.description || '';
    document.getElementById('eduStatus').value = e.status || 'PUBLISHED';

    const title = document.getElementById('educationModalTitle');
    if (title) title.textContent = 'Edit Education Record';
    openModal('educationModal');
}

async function handleEducationSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('eduId').value;
    const payload = {
        degreeOrDiploma: document.getElementById('eduDegree').value.trim(),
        fieldOfStudy: document.getElementById('eduField').value.trim(),
        institutionName: document.getElementById('eduSchool').value.trim(),
        location: document.getElementById('eduLocation').value.trim(),
        startDate: document.getElementById('eduStart').value.trim(),
        endDate: document.getElementById('eduEnd').value.trim(),
        current: document.getElementById('eduCurrent').checked,
        description: document.getElementById('eduDesc').value.trim(),
        status: document.getElementById('eduStatus').value
    };

    const url = id ? `/api/admin/education/${id}` : '/api/admin/education';
    const method = id ? 'PUT' : 'POST';

    const res = await adminFetch(url, {
        method: method,
        body: JSON.stringify(payload)
    });

    if (res && res.success) {
        closeModal('educationModal');
        showAdminToast(id ? 'Education milestone updated!' : 'Education milestone saved!');
        loadEducationTable();
        loadDashboardStats();
    } else {
        alert(res.message || 'Error saving education.');
    }
}

async function deleteEducation(id) {
    if (!confirm('Delete this education entry?')) return;
    const res = await adminFetch(`/api/admin/education/${id}`, { method: 'DELETE' });
    if (res && res.success) {
        showAdminToast('Education record deleted.');
        loadEducationTable();
        loadDashboardStats();
    }
}

/* Experience */
let allExperienceCache = [];

async function loadExperienceTable() {
    const res = await adminFetch('/api/admin/experience');
    const tbody = document.getElementById('experienceTableBody');
    if (!tbody) return;

    if (!res || !res.success || !res.data || res.data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:2rem; color:var(--admin-text-muted);">No experience records added yet.</td></tr>`;
        return;
    }

    allExperienceCache = res.data;

    tbody.innerHTML = res.data.map(exp => `
        <tr>
            <td><strong>${escapeHtml(exp.jobTitle)}</strong></td>
            <td>${escapeHtml(exp.companyName)}</td>
            <td>${escapeHtml(exp.location)}</td>
            <td>${escapeHtml(exp.startDate)} – ${exp.currentRole ? 'Present' : escapeHtml(exp.endDate || '')}</td>
            <td><span class="badge badge-${exp.status.toLowerCase()}">${exp.status}</span></td>
            <td>
                <div class="action-btns" style="display:flex; gap:6px;">
                    <button class="btn-admin btn-admin-secondary" onclick="editExperienceModal(${exp.id})">Edit</button>
                    <button class="btn-admin btn-admin-danger" onclick="deleteExperience(${exp.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function openAddExperienceModal() {
    document.getElementById('experienceForm').reset();
    document.getElementById('expId').value = '';
    const title = document.getElementById('experienceModalTitle');
    if (title) title.textContent = 'Add Experience Record';
    openModal('experienceModal');
}

function editExperienceModal(id) {
    const exp = allExperienceCache.find(item => item.id === id);
    if (!exp) return;

    document.getElementById('expId').value = exp.id;
    document.getElementById('expJobTitle').value = exp.jobTitle;
    document.getElementById('expCompany').value = exp.companyName;
    document.getElementById('expLocation').value = exp.location;
    document.getElementById('expStart').value = exp.startDate;
    document.getElementById('expEnd').value = exp.endDate || '';
    document.getElementById('expCurrent').checked = exp.currentRole;
    document.getElementById('expResp').value = exp.responsibilities || '';
    document.getElementById('expTech').value = exp.technologiesUsed || '';
    document.getElementById('expStatus').value = exp.status || 'PUBLISHED';

    const title = document.getElementById('experienceModalTitle');
    if (title) title.textContent = 'Edit Experience Record';
    openModal('experienceModal');
}

async function handleExperienceSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('expId').value;
    const payload = {
        jobTitle: document.getElementById('expJobTitle').value.trim(),
        companyName: document.getElementById('expCompany').value.trim(),
        location: document.getElementById('expLocation').value.trim(),
        startDate: document.getElementById('expStart').value.trim(),
        endDate: document.getElementById('expEnd').value.trim(),
        currentRole: document.getElementById('expCurrent').checked,
        responsibilities: document.getElementById('expResp').value.trim(),
        technologiesUsed: document.getElementById('expTech').value.trim(),
        status: document.getElementById('expStatus').value
    };

    const url = id ? `/api/admin/experience/${id}` : '/api/admin/experience';
    const method = id ? 'PUT' : 'POST';

    const res = await adminFetch(url, {
        method: method,
        body: JSON.stringify(payload)
    });

    if (res && res.success) {
        closeModal('experienceModal');
        showAdminToast(id ? 'Experience record updated!' : 'Experience record saved!');
        loadExperienceTable();
        loadDashboardStats();
    } else {
        alert(res.message || 'Error saving experience.');
    }
}

async function deleteExperience(id) {
    if (!confirm('Delete this experience entry?')) return;
    const res = await adminFetch(`/api/admin/experience/${id}`, { method: 'DELETE' });
    if (res && res.success) {
        showAdminToast('Experience record deleted.');
        loadExperienceTable();
        loadDashboardStats();
    }
}

/* ==========================================================================
   8. CERTIFICATES CRUD
   ========================================================================== */
let allCertificatesCache = [];

async function loadCertificatesTable() {
    const res = await adminFetch('/api/admin/certificates');
    const tbody = document.getElementById('certificatesTableBody');
    if (!tbody) return;

    if (!res || !res.success || !res.data || res.data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:2rem; color:var(--admin-text-muted);">No certificates added yet.</td></tr>`;
        return;
    }

    allCertificatesCache = res.data;

    tbody.innerHTML = res.data.map(c => `
        <tr>
            <td><strong>${escapeHtml(c.certificateTitle)}</strong></td>
            <td>${escapeHtml(c.issuingOrganization)}</td>
            <td>${escapeHtml(c.issueDate)}</td>
            <td><span class="badge badge-${c.status.toLowerCase()}">${c.status}</span></td>
            <td>
                <div class="action-btns" style="display:flex; gap:6px;">
                    <button class="btn-admin btn-admin-secondary" onclick="editCertificateModal(${c.id})">Edit</button>
                    <button class="btn-admin btn-admin-danger" onclick="deleteCertificate(${c.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function openAddCertificateModal() {
    document.getElementById('certificateForm').reset();
    document.getElementById('certId').value = '';
    const title = document.getElementById('certificateModalTitle');
    if (title) title.textContent = 'Add Certificate';
    openModal('certificateModal');
}

function editCertificateModal(id) {
    const c = allCertificatesCache.find(item => item.id === id);
    if (!c) return;

    document.getElementById('certId').value = c.id;
    document.getElementById('certTitle').value = c.certificateTitle;
    document.getElementById('certOrg').value = c.issuingOrganization;
    document.getElementById('certDate').value = c.issueDate;
    document.getElementById('certUrl').value = c.credentialUrl || '';
    document.getElementById('certStatus').value = c.status || 'PUBLISHED';

    const title = document.getElementById('certificateModalTitle');
    if (title) title.textContent = 'Edit Certificate';
    openModal('certificateModal');
}

async function handleCertificateSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('certId').value;
    const payload = {
        certificateTitle: document.getElementById('certTitle').value.trim(),
        issuingOrganization: document.getElementById('certOrg').value.trim(),
        issueDate: document.getElementById('certDate').value.trim(),
        credentialUrl: document.getElementById('certUrl').value.trim(),
        status: document.getElementById('certStatus').value
    };

    const url = id ? `/api/admin/certificates/${id}` : '/api/admin/certificates';
    const method = id ? 'PUT' : 'POST';

    const res = await adminFetch(url, {
        method: method,
        body: JSON.stringify(payload)
    });

    if (res && res.success) {
        closeModal('certificateModal');
        showAdminToast(id ? 'Certificate updated!' : 'Certificate saved!');
        loadCertificatesTable();
        loadDashboardStats();
    } else {
        alert(res.message || 'Error saving certificate.');
    }
}

async function deleteCertificate(id) {
    if (!confirm('Delete this certificate?')) return;
    const res = await adminFetch(`/api/admin/certificates/${id}`, { method: 'DELETE' });
    if (res && res.success) {
        showAdminToast('Certificate deleted.');
        loadCertificatesTable();
        loadDashboardStats();
    }
}

/* ==========================================================================
   9. TESTIMONIALS & RECOMMENDATIONS MODERATION WORKFLOW
   ========================================================================== */
let activeTestimonialFilter = 'ALL';
let allTestimonialsCache = [];
let currentReviewTestimonial = null;

async function loadTestimonialsTable() {
    const res = await adminFetch(`/api/admin/testimonials?status=${activeTestimonialFilter}`);
    const tbody = document.getElementById('testimonialsTableBody');
    if (!tbody) return;

    if (!res || !res.success || !res.data) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:2rem; color:var(--admin-text-muted);">Failed to load recommendations.</td></tr>`;
        return;
    }

    allTestimonialsCache = res.data;
    updateTestimonialBadges();
    renderTestimonialsTableRows(allTestimonialsCache);
}

function updateTestimonialBadges() {
    adminFetch('/api/admin/testimonials?status=ALL').then(res => {
        if (res && res.success && Array.isArray(res.data)) {
            const list = res.data;
            const setBadge = (id, count) => {
                const el = document.getElementById(id);
                if (el) el.textContent = count;
            };
            setBadge('countAllTestimonials', list.length);
            setBadge('countPendingTestimonials', list.filter(t => (t.status || t.approvalStatus) === 'PENDING').length);
            setBadge('countApprovedTestimonials', list.filter(t => (t.status || t.approvalStatus) === 'APPROVED').length);
            setBadge('countRejectedTestimonials', list.filter(t => (t.status || t.approvalStatus) === 'REJECTED').length);
            setBadge('countArchivedTestimonials', list.filter(t => (t.status || t.approvalStatus) === 'ARCHIVED').length);
        }
    });
}

function filterTestimonialsByStatus(status, btn) {
    activeTestimonialFilter = status;
    document.querySelectorAll('.status-tab-btn').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');
    loadTestimonialsTable();
}

function searchTestimonialsAdmin(query) {
    if (!query || !query.trim()) {
        renderTestimonialsTableRows(allTestimonialsCache);
        return;
    }
    const q = query.trim().toLowerCase();
    const filtered = allTestimonialsCache.filter(t => {
        const name = (t.fullName || t.clientName || '').toLowerCase();
        const org = (t.organization || t.clientCompany || '').toLowerCase();
        const msg = (t.message || t.testimonialText || '').toLowerCase();
        return name.includes(q) || org.includes(q) || msg.includes(q);
    });
    renderTestimonialsTableRows(filtered);
}

function renderTestimonialsTableRows(list) {
    const tbody = document.getElementById('testimonialsTableBody');
    if (!tbody) return;

    if (!list || list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:2rem; color:var(--admin-text-muted);">No recommendations found in this view.</td></tr>`;
        return;
    }

    tbody.innerHTML = list.map(t => {
        const status = (t.status || t.approvalStatus || 'PENDING').toUpperCase();
        const name = t.fullName || t.clientName || 'Anonymous';
        const email = t.email || 'Confidential';
        const org = t.organization || t.clientCompany || 'Independent';
        const position = t.position || t.clientTitle || '';
        const relationship = t.relationship || 'Client';
        const message = t.message || t.testimonialText || '';
        const hasConsent = t.consentToPublish === true;
        const submitted = t.submittedAt ? t.submittedAt.replace('T', ' ').substring(0, 16) : 'Recent';

        const consentHtml = hasConsent
            ? `<span class="consent-badge">✓ Granted</span>`
            : `<span class="consent-badge missing">⚠️ Missing</span>`;

        let badgeClass = 'badge-pending';
        if (status === 'APPROVED') badgeClass = 'badge-approved';
        else if (status === 'REJECTED') badgeClass = 'badge-rejected';
        else if (status === 'ARCHIVED') badgeClass = 'badge-archived';

        return `
            <tr>
                <td>
                    <strong>${escapeHtml(name)}</strong><br>
                    <small style="color:var(--admin-accent);">${escapeHtml(email)}</small>
                </td>
                <td>
                    <span style="font-size:0.78rem; font-weight:600; color:#38bdf8; background:rgba(0,180,216,0.1); padding:2px 6px; border-radius:4px;">${escapeHtml(relationship)}</span><br>
                    <small style="color:var(--admin-text-sub);">${escapeHtml(position)}${org ? ` • ${escapeHtml(org)}` : ''}</small>
                </td>
                <td style="max-width:250px;">
                    <p style="white-space:nowrap; overflow:hidden; text-overflow:ellipsis; margin:0; color:var(--admin-text-main); font-size:0.88rem;" title="${escapeHtml(message)}">
                        "${escapeHtml(message)}"
                    </p>
                </td>
                <td>${consentHtml}</td>
                <td><small style="color:var(--admin-text-muted);">${escapeHtml(submitted)}</small></td>
                <td><span class="badge-status ${badgeClass}">${status}</span></td>
                <td>
                    <div class="action-btns" style="display:flex; gap:6px;">
                        <button class="btn-admin btn-admin-secondary" style="padding:4px 8px; font-size:0.78rem;" onclick="openTestimonialReviewModal(${t.id})">🔍 Review</button>
                        ${status !== 'APPROVED' ? `<button class="btn-admin btn-admin-primary" style="padding:4px 8px; font-size:0.78rem;" onclick="quickApproveTestimonial(${t.id}, ${hasConsent})">Approve</button>` : ''}
                        <button class="btn-admin btn-admin-danger" style="padding:4px 8px; font-size:0.78rem;" onclick="deleteTestimonial(${t.id})">Delete</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function openTestimonialReviewModal(id) {
    const t = allTestimonialsCache.find(item => item.id === id);
    if (!t) return;
    currentReviewTestimonial = t;

    const name = t.fullName || t.clientName || 'Anonymous';
    document.getElementById('revFullName').textContent = name;
    document.getElementById('revMeta').textContent = `${t.position || ''}${t.organization ? ` • ${t.organization}` : ''}`;
    document.getElementById('revEmail').textContent = t.email || 'Confidential';
    document.getElementById('revMessageText').textContent = `"${t.message || t.testimonialText || ''}"`;
    document.getElementById('revRelationship').textContent = t.relationship || 'Client';

    const webLink = document.getElementById('revWebsite');
    if (t.organizationWebsite) {
        webLink.innerHTML = `<a href="${t.organizationWebsite}" target="_blank" style="color:var(--admin-accent); text-decoration:none;">${escapeHtml(t.organizationWebsite)} ↗</a>`;
    } else {
        webLink.textContent = 'None provided';
    }

    document.getElementById('revSubmittedAt').textContent = t.submittedAt ? t.submittedAt.replace('T', ' ') : 'Recent';
    const status = (t.status || t.approvalStatus || 'PENDING').toUpperCase();
    document.getElementById('revStatusBadge').innerHTML = `<span class="badge-status badge-${status.toLowerCase()}">${status}</span>`;

    const consentEl = document.getElementById('revConsentBadge');
    if (t.consentToPublish) {
        consentEl.innerHTML = `<span class="consent-badge">✓ Explicit Publication Consent Granted</span>`;
    } else {
        consentEl.innerHTML = `<span class="consent-badge missing">⚠️ NO Publication Consent</span>`;
    }

    const avatarBox = document.getElementById('revAvatarBox');
    if (t.photoUrl || t.photoPath || t.clientAvatarUrl) {
        const p = t.photoUrl || t.photoPath || t.clientAvatarUrl;
        avatarBox.innerHTML = `<img src="${p}" style="width:100%; height:100%; border-radius:50%; object-fit:cover;">`;
    } else {
        avatarBox.textContent = name.charAt(0);
    }

    document.getElementById('revRejectionReason').value = t.rejectionReason || '';
    openModal('testimonialReviewModal');
}

async function quickApproveTestimonial(id, hasConsent) {
    if (!hasConsent) {
        alert('Cannot approve recommendation without visitor explicit publication consent.');
        return;
    }
    const res = await adminFetch(`/api/admin/testimonials/${id}/approve`, { method: 'PATCH' });
    if (res && res.success) {
        showAdminToast('Testimonial approved for public portfolio display!');
        loadTestimonialsTable();
        loadDashboardStats();
    }
}

async function approveTestimonialFromReview() {
    if (!currentReviewTestimonial) return;
    if (!currentReviewTestimonial.consentToPublish) {
        alert('Cannot approve recommendation without visitor publication consent.');
        return;
    }

    const res = await adminFetch(`/api/admin/testimonials/${currentReviewTestimonial.id}/approve`, { method: 'PATCH' });
    if (res && res.success) {
        closeModal('testimonialReviewModal');
        showAdminToast('Testimonial approved successfully!');
        loadTestimonialsTable();
        loadDashboardStats();
    }
}

async function rejectTestimonialFromReview() {
    if (!currentReviewTestimonial) return;
    const reason = document.getElementById('revRejectionReason').value.trim() || 'Content does not meet publication standards';

    const res = await adminFetch(`/api/admin/testimonials/${currentReviewTestimonial.id}/reject`, {
        method: 'PATCH',
        body: JSON.stringify({ reason })
    });
    if (res && res.success) {
        closeModal('testimonialReviewModal');
        showAdminToast('Testimonial rejected.');
        loadTestimonialsTable();
        loadDashboardStats();
    }
}

async function archiveTestimonialFromReview() {
    if (!currentReviewTestimonial) return;
    const res = await adminFetch(`/api/admin/testimonials/${currentReviewTestimonial.id}/archive`, { method: 'PATCH' });
    if (res && res.success) {
        closeModal('testimonialReviewModal');
        showAdminToast('Testimonial archived.');
        loadTestimonialsTable();
        loadDashboardStats();
    }
}

async function deleteTestimonialFromReview() {
    if (!currentReviewTestimonial) return;
    if (!confirm('Permanently delete this recommendation? This cannot be undone.')) return;

    const res = await adminFetch(`/api/admin/testimonials/${currentReviewTestimonial.id}`, { method: 'DELETE' });
    if (res && res.success) {
        closeModal('testimonialReviewModal');
        showAdminToast('Testimonial permanently deleted.');
        loadTestimonialsTable();
        loadDashboardStats();
    }
}

function openAddTestimonialAdminModal() {
    // Open the manual testimonial add modal
    openAddSettingModal();
}

async function deleteTestimonial(id) {
    if (!confirm('Delete this testimonial permanently?')) return;
    const res = await adminFetch(`/api/admin/testimonials/${id}`, { method: 'DELETE' });
    if (res && res.success) {
        showAdminToast('Testimonial deleted.');
        loadTestimonialsTable();
        loadDashboardStats();
    }
}

/* ==========================================================================
   10. CONTACT INQUIRIES MANAGEMENT
   ========================================================================== */
async function loadMessagesTable() {
    const res = await adminFetch('/api/admin/messages');
    const tbody = document.getElementById('messagesTableBody');
    if (!tbody) return;

    if (!res || !res.success || !res.data || res.data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:2rem; color:var(--admin-text-muted);">No messages received yet.</td></tr>`;
        return;
    }

    tbody.innerHTML = res.data.map(m => `
        <tr style="${!m.read ? 'background: rgba(0,180,216,0.06); font-weight:600;' : ''}">
            <td>${m.read ? '✉️ Read' : '📬 <strong>NEW</strong>'}</td>
            <td><strong>${escapeHtml(m.fullName)}</strong><br><small><a href="mailto:${escapeHtml(m.email)}">${escapeHtml(m.email)}</a></small></td>
            <td>${escapeHtml(m.subject)}</td>
            <td>${escapeHtml(m.message)}</td>
            <td>${new Date(m.createdAt).toLocaleDateString()}</td>
            <td>
                <div class="action-btns">
                    ${!m.read 
                        ? `<button class="btn-admin btn-admin-primary" onclick="toggleMessageRead(${m.id}, true)">Mark Read</button>`
                        : `<button class="btn-admin btn-admin-secondary" onclick="toggleMessageRead(${m.id}, false)">Mark Unread</button>`
                    }
                    <button class="btn-admin btn-admin-danger" onclick="deleteMessage(${m.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function toggleMessageRead(id, markAsRead) {
    const endpoint = markAsRead ? `/api/admin/messages/${id}/read` : `/api/admin/messages/${id}/unread`;
    const res = await adminFetch(endpoint, { method: 'PATCH' });
    if (res && res.success) {
        loadMessagesTable();
        loadDashboardStats();
    }
}

async function deleteMessage(id) {
    if (!confirm('Delete this inquiry message?')) return;
    const res = await adminFetch(`/api/admin/messages/${id}`, { method: 'DELETE' });
    if (res && res.success) {
        showAdminToast('Message deleted.');
        loadMessagesTable();
        loadDashboardStats();
    }
}

/* ==========================================================================
   11. CV UPLOAD & MANAGEMENT
   ========================================================================== */
async function loadCvManagement() {
    const res = await adminFetch('/api/admin/profile');
    if (res && res.success && res.data) {
        const cvLink = res.data.cvFileUrl;
        const statusEl = document.getElementById('cvStatusText');
        const viewBtn = document.getElementById('cvViewLink');
        if (cvLink) {
            statusEl.innerHTML = `<span style="color:var(--admin-success);">✓ CV document is uploaded and live on public site.</span>`;
            viewBtn.href = cvLink;
            viewBtn.style.display = 'inline-flex';
        } else {
            statusEl.innerHTML = `<span style="color:var(--admin-warning);">⚠️ No CV document uploaded yet.</span>`;
            viewBtn.style.display = 'none';
        }
    }
}

async function handleCvUpload(input) {
    if (!input.files || input.files.length === 0) return;
    await handleCvUploadFile(input.files[0]);
}

/* ==========================================================================
   12. WEB SETTINGS & CONFIGURATION MANAGEMENT MODULE
   ========================================================================== */
let allSettingsDictionary = {};
let hasUnsavedSettings = false;
let registeredPasskeysList = [];
let emergencyRecoveryCodesList = [];

// Warn administrator before navigating away with unsaved settings
window.addEventListener('beforeunload', (e) => {
    if (hasUnsavedSettings) {
        e.preventDefault();
        e.returnValue = 'You have unsaved website configuration changes. Are you sure you want to leave?';
        return e.returnValue;
    }
});

async function loadSettings() {
    const res = await adminFetch('/api/admin/settings');
    if (!res || !res.success || !res.data) {
        showAdminToast('Could not load settings from server.', 'error');
        return;
    }

    allSettingsDictionary = res.data.settings || {};
    populateSettingsFormFields(allSettingsDictionary);
    loadPasskeyDevicesTable();
    initSettingsChangeListeners();
    setUnsavedChangesState(false);
}

function populateSettingsFormFields(settings) {
    // 1. Text, number, url, email, select inputs
    document.querySelectorAll('.setting-input').forEach(input => {
        const key = input.dataset.key;
        if (key && settings[key] !== undefined) {
            input.value = settings[key];
        }
    });

    // 2. Boolean switch toggles
    document.querySelectorAll('.setting-input-bool').forEach(input => {
        const key = input.dataset.key;
        if (key && settings[key] !== undefined) {
            input.checked = (settings[key] === 'true' || settings[key] === true);
        }
    });

    // 3. Color swatches
    document.querySelectorAll('input[type="color"][data-color-for]').forEach(picker => {
        const key = picker.dataset.colorFor;
        if (key && settings[key]) {
            try {
                picker.value = settings[key];
            } catch (ignored) {}
        }
    });
}

function initSettingsChangeListeners() {
    const markChanged = (e) => {
        setUnsavedChangesState(true);
        // Sync duplicate inputs sharing the same key (e.g. maintenance mode in general and backup panels)
        if (e && e.target && e.target.classList.contains('setting-input-bool')) {
            const key = e.target.dataset.key;
            if (key) {
                document.querySelectorAll(`.setting-input-bool[data-key="${key}"]`).forEach(sibling => {
                    if (sibling !== e.target) sibling.checked = e.target.checked;
                });
            }
        }
    };

    document.querySelectorAll('.setting-input, .setting-input-bool').forEach(input => {
        input.removeEventListener('input', markChanged);
        input.removeEventListener('change', markChanged);
        input.addEventListener('input', markChanged);
        input.addEventListener('change', markChanged);
    });
}

function setUnsavedChangesState(isUnsaved) {
    hasUnsavedSettings = isUnsaved;
    const banner = document.getElementById('settingsUnsavedBanner');
    if (banner) {
        if (isUnsaved) {
            banner.classList.add('visible');
        } else {
            banner.classList.remove('visible');
        }
    }
}

function switchSettingsCategory(panelId, btn) {
    document.querySelectorAll('.settings-nav-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.settings-panel').forEach(p => p.classList.remove('active'));

    if (btn) btn.classList.add('active');
    const panel = document.getElementById(panelId);
    if (panel) panel.classList.add('active');

    if (panelId === 'panel-security') {
        loadPasskeyDevicesTable();
    }
}

function filterSettingsLive(query) {
    if (!query || !query.trim()) {
        document.querySelectorAll('.settings-panel .form-group-admin, .settings-panel .setting-switch-row').forEach(row => {
            row.style.display = '';
        });
        return;
    }

    const q = query.trim().toLowerCase();
    document.querySelectorAll('.settings-panel').forEach(panel => {
        let hasMatchInPanel = false;
        panel.querySelectorAll('.form-group-admin, .setting-switch-row').forEach(row => {
            const text = row.textContent.toLowerCase();
            if (text.includes(q)) {
                row.style.display = '';
                hasMatchInPanel = true;
            } else {
                row.style.display = 'none';
            }
        });

        // Switch to panel if matching
        if (hasMatchInPanel && !panel.classList.contains('active')) {
            switchSettingsCategory(panel.id, document.querySelector(`.settings-nav-btn[data-target="${panel.id}"]`));
        }
    });
}

function syncColorInput(picker) {
    const key = picker.dataset.colorFor;
    if (!key) return;
    const textInput = document.querySelector(`.setting-input[data-key="${key}"]`);
    if (textInput) {
        textInput.value = picker.value;
        setUnsavedChangesState(true);
    }
}

function openAddSettingModal() {
    document.getElementById('settingForm').reset();
    document.getElementById('settingModalTitle').textContent = 'Add / Update Website Setting';
    openModal('settingModal');
}

async function handleSettingSubmit(e) {
    e.preventDefault();
    const payload = {
        settingKey: document.getElementById('settingKeyInput').value.trim(),
        settingValue: document.getElementById('settingValueInput').value.trim(),
        settingGroup: (document.getElementById('settingGroupInput').value || 'GENERAL').trim(),
        description: document.getElementById('settingDescInput').value.trim()
    };

    const res = await adminFetch('/api/admin/settings', {
        method: 'POST',
        body: JSON.stringify(payload)
    });

    if (res && res.success) {
        closeModal('settingModal');
        showAdminToast('Setting saved successfully!');
        loadSettings();
    } else {
        alert(res ? (res.message || 'Error saving setting.') : 'Server error');
    }
}

async function saveAllSettings() {
    const payload = {};

    document.querySelectorAll('.setting-input').forEach(input => {
        const key = input.dataset.key;
        if (key) payload[key] = input.value;
    });

    document.querySelectorAll('.setting-input-bool').forEach(input => {
        const key = input.dataset.key;
        if (key) payload[key] = input.checked ? 'true' : 'false';
    });

    const res = await adminFetch('/api/admin/settings/batch', {
        method: 'POST',
        body: JSON.stringify({ settings: payload })
    });

    if (res && res.success) {
        showAdminToast('All website settings saved to database!');
        setUnsavedChangesState(false);
        allSettingsDictionary = { ...allSettingsDictionary, ...payload };
    } else {
        alert(res ? (res.message || 'Error saving settings.') : 'Server error');
    }
}

async function publishSettingsLive() {
    if (hasUnsavedSettings) {
        await saveAllSettings();
    }

    const res = await adminFetch('/api/admin/settings/publish', { method: 'POST' });
    if (res && res.success) {
        showAdminToast('Website configuration published and active on live portfolio! 🚀');
    }
}

async function confirmResetSettings() {
    if (!confirm('Are you sure you want to restore all website settings to factory system defaults? Any custom modifications will be reset.')) {
        return;
    }

    const res = await adminFetch('/api/admin/settings/reset', { method: 'POST' });
    if (res && res.success) {
        showAdminToast('Settings reset to factory defaults.');
        loadSettings();
    }
}

function exportSettingsBackup() {
    const token = checkAuth();
    if (!token) return;
    window.location.href = `${API_BASE}/api/admin/settings/export`;
}

function readImportJsonFile(input) {
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    const reader = new FileReader();
    reader.onload = (e) => {
        document.getElementById('importJsonTextArea').value = e.target.result;
    };
    reader.readAsText(file);
}

async function handleSettingsImport(e) {
    e.preventDefault();
    const jsonStr = document.getElementById('importJsonTextArea').value.trim();
    if (!jsonStr) {
        alert('Please provide JSON settings backup content.');
        return;
    }

    const btn = document.getElementById('btnApplyImport');
    btn.disabled = true;
    btn.textContent = 'Applying...';

    try {
        const res = await adminFetch('/api/admin/settings/import', {
            method: 'POST',
            body: jsonStr
        });

        if (res && res.success) {
            closeModal('settingsImportModal');
            showAdminToast(res.message || 'Settings restored successfully!');
            loadSettings();
        } else {
            alert(res ? (res.message || 'Failed to import backup.') : 'Import failed');
        }
    } catch (err) {
        alert('Error importing JSON: ' + err.message);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Apply & Restore Configuration';
    }
}

async function openSettingsAuditModal() {
    const tbody = document.getElementById('settingsAuditTableBody');
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:1.5rem; color:var(--admin-text-muted);">Loading audit trail...</td></tr>`;
    openModal('settingsAuditModal');

    const res = await adminFetch('/api/admin/settings/audit');
    if (!res || !res.success || !res.data || res.data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:1.5rem; color:var(--admin-text-muted);">No settings modification logs found.</td></tr>`;
        return;
    }

    tbody.innerHTML = res.data.map(l => `
        <tr>
            <td><small style="color:var(--admin-text-muted);">${l.timestamp ? l.timestamp.replace('T', ' ').substring(0, 19) : '-'}</small></td>
            <td><span class="badge badge-info">${escapeHtml(l.action)}</span></td>
            <td><strong>${escapeHtml(l.changedBy || 'admin')}</strong></td>
            <td><code>${escapeHtml(l.ipAddress || '-')}</code></td>
            <td>${escapeHtml(l.details || '-')}</td>
            <td style="text-align:center;">
                <button type="button" class="btn-admin btn-admin-danger btn-sm" onclick="deleteSettingsAuditEntry(${l.id})" title="Delete entry" style="padding:4px 8px; font-size:0.8rem;">🗑️</button>
            </td>
        </tr>
    `).join('');
}

async function deleteSettingsAuditEntry(id) {
    if (!confirm(`Are you sure you want to delete settings audit record #${id}?`)) return;
    try {
        const res = await adminFetch(`/api/admin/settings/audit/${id}`, { method: 'DELETE' });
        if (res && res.success) {
            showToast('Settings audit record deleted', 'success');
            openSettingsAuditModal();
        } else {
            showToast((res && res.message) || 'Failed to delete settings audit record', 'error');
        }
    } catch (err) {
        showToast(err.message || 'Error deleting settings audit record', 'error');
    }
}

function previewSettingsLive() {
    const getVal = (key, def = '') => {
        const inp = document.querySelector(`.setting-input[data-key="${key}"]`);
        return inp ? inp.value : (allSettingsDictionary[key] || def);
    };

    const bg = getVal('appearance.primaryColor', '#0f172a');
    const cardBg = getVal('appearance.secondaryColor', '#1e293b');
    const accent = getVal('appearance.accentColor', '#00d2ff');
    const text = getVal('appearance.textColor', '#f8fafc');
    const font = getVal('appearance.fontFamily', 'Poppins, sans-serif');
    const radius = getVal('appearance.buttonRadius', '8px');
    const brand = getVal('general.brandName', 'Joash Odiwuor');
    const title = getVal('general.professionalTitle', 'Aspiring Software Developer');
    const tagline = getVal('general.tagline', 'Diploma in Computer Science');

    const container = document.getElementById('settingsPreviewContainer');
    container.style.backgroundColor = bg;
    container.style.color = text;
    container.style.fontFamily = font;

    container.innerHTML = `
        <div style="background:${cardBg}; padding:25px; border-radius:${radius}; border:1px solid rgba(255,255,255,0.1); box-shadow:0 10px 25px rgba(0,0,0,0.4);">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:15px;">
                <span style="font-weight:700; font-size:1.15rem; color:#fff;">${escapeHtml(brand)}</span>
                <span style="padding:4px 10px; border-radius:15px; background:rgba(0,210,255,0.15); color:${accent}; font-size:0.75rem; font-weight:600;">Live Theme Preview</span>
            </div>
            <h3 style="color:${accent}; font-size:1.4rem; margin-bottom:6px;">${escapeHtml(tagline)}</h3>
            <p style="color:#94a3b8; font-size:0.9rem; margin-bottom:20px;">${escapeHtml(title)}</p>
            <div style="display:flex; gap:10px;">
                <button type="button" style="background:${accent}; color:#070e1b; border:none; padding:10px 20px; border-radius:${radius}; font-weight:700; cursor:pointer;">Primary Button</button>
                <button type="button" style="background:transparent; color:${accent}; border:1px solid ${accent}; padding:10px 20px; border-radius:${radius}; font-weight:600; cursor:pointer;">Outline Button</button>
            </div>
        </div>
    `;

    openModal('settingsPreviewModal');
}

/* ==========================================================================
   12B. FIDO2 / WEBAUTHN BIOMETRIC PASSKEY DEVICE MANAGEMENT
   ========================================================================== */
async function loadPasskeyDevicesTable() {
    const tbody = document.getElementById('passkeyDevicesTableBody');
    if (!tbody) return;

    const res = await adminFetch('/api/admin/passkeys');
    if (!res || !res.success || !res.data || res.data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:1.5rem; color:var(--admin-text-muted);">No biometric passkeys enrolled yet. Click "Enroll Fingerprint / Device" to add your computer or smartphone.</td></tr>`;
        return;
    }

    registeredPasskeysList = res.data;
    tbody.innerHTML = res.data.map(dev => `
        <tr>
            <td>
                <strong>${escapeHtml(dev.deviceName || 'Personal Computer')}</strong>
                ${dev.currentDevice ? `<span class="device-badge active-dev" style="margin-left:6px;">Current Device</span>` : ''}
            </td>
            <td><span class="device-badge">${escapeHtml(dev.deviceType || 'Internal Biometrics')}</span></td>
            <td><small style="color:var(--admin-text-muted);">${dev.registeredAt ? dev.registeredAt.replace('T', ' ').substring(0, 16) : 'Recently'}</small></td>
            <td><small style="color:var(--admin-text-sub);">${dev.lastUsedAt ? dev.lastUsedAt.replace('T', ' ').substring(0, 16) : 'Never'}</small></td>
            <td>
                <button class="btn-admin btn-admin-danger" style="padding:3px 8px; font-size:0.75rem;" onclick="revokePasskeyDevice('${escapeHtml(dev.credentialId)}')">Revoke</button>
            </td>
        </tr>
    `).join('');
}

function openEnrollPasskeyModal() {
    document.getElementById('enrollDeviceName').value = 'Lenovo ThinkPad Fingerprint Reader';
    openModal('enrollPasskeyModal');
}

async function handlePasskeyEnrollSubmit(e) {
    e.preventDefault();
    const btn = document.getElementById('btnStartEnroll');
    const name = document.getElementById('enrollDeviceName').value.trim();
    const type = document.getElementById('enrollDeviceType').value;

    btn.disabled = true;
    btn.textContent = 'Touch Fingerprint Sensor... 👆';

    try {
        const res = await triggerPasskeyRegistration(name, type);
        if (res && res.success) {
            closeModal('enrollPasskeyModal');
            showAdminToast('Biometric passkey enrolled successfully! 🛡️');
            loadPasskeyDevicesTable();

            // If recovery codes returned
            if (res.data && res.data.recoveryCodes && res.data.recoveryCodes.length > 0) {
                emergencyRecoveryCodesList = res.data.recoveryCodes;
                renderRecoveryCodesGrid(emergencyRecoveryCodesList);
                openModal('recoveryCodesModal');
            }
        } else {
            alert(res ? (res.message || 'Passkey enrollment failed') : 'Device enrollment error');
        }
    } catch (err) {
        console.error('Passkey registration error:', err);
        alert(err.message || 'Biometric enrollment failed or was cancelled.');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Prompt Biometric Scanner 👆';
    }
}

async function revokePasskeyDevice(credentialId) {
    if (!confirm('Are you sure you want to revoke this passkey credential? It will no longer be accepted for administrator authentication.')) {
        return;
    }

    const res = await adminFetch(`/api/admin/passkeys/${encodeURIComponent(credentialId)}`, { method: 'DELETE' });
    if (res && res.success) {
        showAdminToast('Passkey revoked.');
        loadPasskeyDevicesTable();
    } else {
        alert(res ? (res.message || 'Failed to revoke passkey.') : 'Error revoking passkey');
    }
}

function openRecoveryCodesModal() {
    if (emergencyRecoveryCodesList.length === 0) {
        emergencyRecoveryCodesList = [
            '7K2M-9P4Q', '3T8N-5X1W', '9R4D-2M7B', '4J6L-8C3V',
            '2N9F-6H5T', '8P3Q-1W7K', '5X2Y-4M8D', '1B7R-9L3N'
        ];
    }
    renderRecoveryCodesGrid(emergencyRecoveryCodesList);
    openModal('recoveryCodesModal');
}

function renderRecoveryCodesGrid(codes) {
    const grid = document.getElementById('recoveryCodesGrid');
    if (!grid) return;
    grid.innerHTML = codes.map((c, i) => `
        <div style="background:rgba(255,255,255,0.05); border:1px solid rgba(255,255,255,0.1); border-radius:6px; padding:10px 14px; display:flex; justify-content:space-between; align-items:center;">
            <span style="color:var(--admin-text-muted); font-size:0.75rem;">#${i + 1}</span>
            <code style="color:var(--admin-accent); font-weight:700; font-size:0.95rem; letter-spacing:1px;">${c}</code>
        </div>
    `).join('');
}

async function confirmRegenerateRecoveryCodes() {
    if (!confirm('Regenerate new recovery codes? All previous emergency codes will be immediately invalidated.')) {
        return;
    }

    const res = await adminFetch('/api/admin/passkeys/recovery-codes/regenerate', { method: 'POST' });
    if (res && res.success && res.data && res.data.recoveryCodes) {
        emergencyRecoveryCodesList = res.data.recoveryCodes;
        renderRecoveryCodesGrid(emergencyRecoveryCodesList);
        showAdminToast('New recovery codes generated.');
    }
}

function copyAllRecoveryCodes() {
    const text = emergencyRecoveryCodesList.join('\n');
    navigator.clipboard.writeText(text).then(() => {
        showAdminToast('Recovery codes copied to clipboard! Keep them safe.');
    });
}

/* ==========================================================================
   13. ACHIEVEMENTS & AWARDS
   ========================================================================== */
let allAchievementsCache = [];

async function loadAchievementsTable() {
    const res = await adminFetch('/api/admin/achievements');
    const tbody = document.getElementById('achievementsTableBody');
    if (!tbody) return;

    if (!res || !res.success || !res.data || res.data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:2rem; color:var(--admin-text-muted);">No achievements or awards added yet.</td></tr>`;
        return;
    }

    allAchievementsCache = res.data;
    tbody.innerHTML = res.data.map(a => `
        <tr>
            <td><strong>${escapeHtml(a.title)}</strong></td>
            <td>${escapeHtml(a.organization || '-')}</td>
            <td>${escapeHtml(a.achievementDate || '-')}</td>
            <td><span class="badge ${a.status === 'PUBLISHED' ? 'badge-published' : 'badge-draft'}">${a.status}</span></td>
            <td>
                <button class="btn-action edit" onclick="editAchievement(${a.id})">Edit</button>
                <button class="btn-action delete" onclick="deleteAchievement(${a.id})">Delete</button>
            </td>
        </tr>
    `).join('');
}

function openAddAchievementModal() {
    document.getElementById('achievementForm').reset();
    document.getElementById('achId').value = '';
    document.getElementById('achievementModalTitle').textContent = 'Add Achievement';
    openModal('achievementModal');
}

function editAchievement(id) {
    const a = allAchievementsCache.find(item => item.id === id);
    if (!a) return;
    document.getElementById('achId').value = a.id;
    document.getElementById('achTitle').value = a.title;
    document.getElementById('achOrg').value = a.organization || '';
    document.getElementById('achDate').value = a.achievementDate || '';
    document.getElementById('achDesc').value = a.description || '';
    document.getElementById('achStatus').value = a.status || 'PUBLISHED';
    document.getElementById('achievementModalTitle').textContent = 'Edit Achievement';
    openModal('achievementModal');
}

async function handleAchievementSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('achId').value;
    const payload = {
        title: document.getElementById('achTitle').value.trim(),
        organization: document.getElementById('achOrg').value.trim(),
        achievementDate: document.getElementById('achDate').value.trim(),
        description: document.getElementById('achDesc').value.trim(),
        status: document.getElementById('achStatus').value
    };

    const url = id ? `/api/admin/achievements/${id}` : '/api/admin/achievements';
    const method = id ? 'PUT' : 'POST';

    const res = await adminFetch(url, {
        method: method,
        body: JSON.stringify(payload)
    });

    if (res && res.success) {
        closeModal('achievementModal');
        loadAchievementsTable();
        showAdminToast('Achievement saved successfully');
    } else {
        alert(res.message || 'Failed to save achievement');
    }
}

async function deleteAchievement(id) {
    if (!confirm('Are you sure you want to delete this achievement?')) return;
    const res = await adminFetch(`/api/admin/achievements/${id}`, { method: 'DELETE' });
    if (res && res.success) {
        loadAchievementsTable();
        showAdminToast('Achievement deleted');
    } else {
        alert(res.message || 'Failed to delete achievement');
    }
}

/* ==========================================================================
   14. SOCIAL MEDIA LINKS
   ========================================================================== */
let allSocialCache = [];

async function loadSocialTable() {
    const res = await adminFetch('/api/admin/social-links');
    const tbody = document.getElementById('socialTableBody');
    if (!tbody) return;

    if (!res || !res.success || !res.data || res.data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:2rem; color:var(--admin-text-muted);">No social links configured yet.</td></tr>`;
        return;
    }

    allSocialCache = res.data;
    tbody.innerHTML = res.data.map(s => `
        <tr>
            <td><strong>${escapeHtml(s.platformName)}</strong></td>
            <td><a href="${escapeHtml(s.url)}" target="_blank" style="color:var(--admin-accent);">${escapeHtml(s.url)}</a></td>
            <td><code>${escapeHtml(s.iconClass)}</code></td>
            <td><span class="badge ${s.active ? 'badge-published' : 'badge-draft'}">${s.active ? 'ACTIVE' : 'INACTIVE'}</span></td>
            <td>
                <button class="btn-action edit" onclick="editSocialLink(${s.id})">Edit</button>
                <button class="btn-action delete" onclick="deleteSocialLink(${s.id})">Delete</button>
            </td>
        </tr>
    `).join('');
}

function handlePlatformInputChange() {
    const plat = (document.getElementById('socPlatform').value || '').toLowerCase().trim();
    const iconInput = document.getElementById('socIcon');
    const urlInput = document.getElementById('socUrl');
    if (!iconInput) return;

    if (plat.includes('git')) {
        iconInput.value = '🐙';
        if (!urlInput.value) urlInput.placeholder = 'https://github.com/your-username';
    } else if (plat.includes('link')) {
        iconInput.value = '💼';
        if (!urlInput.value) urlInput.placeholder = 'https://linkedin.com/in/your-profile';
    } else if (plat.includes('twit') || plat === 'x') {
        iconInput.value = '🐦';
        if (!urlInput.value) urlInput.placeholder = 'https://x.com/your-handle';
    } else if (plat.includes('mail') || plat.includes('email')) {
        iconInput.value = '✉️';
        if (!urlInput.value) urlInput.placeholder = 'mailto:your-email@domain.com';
    } else if (plat.includes('what')) {
        iconInput.value = '💬';
        if (!urlInput.value) urlInput.placeholder = 'https://wa.me/254700000000';
    } else if (plat.includes('you')) {
        iconInput.value = '▶️';
        if (!urlInput.value) urlInput.placeholder = 'https://youtube.com/@channel';
    } else if (plat.includes('insta')) {
        iconInput.value = '📸';
        if (!urlInput.value) urlInput.placeholder = 'https://instagram.com/your-handle';
    } else if (plat.includes('disc')) {
        iconInput.value = '🎮';
        if (!urlInput.value) urlInput.placeholder = 'https://discord.gg/invite';
    } else if (plat.includes('tele')) {
        iconInput.value = '✈️';
        if (!urlInput.value) urlInput.placeholder = 'https://t.me/your-username';
    }
}

function openAddSocialModal() {
    document.getElementById('socialForm').reset();
    document.getElementById('socialId').value = '';
    document.getElementById('socActive').checked = true;
    document.getElementById('socialModalTitle').textContent = 'Add Social Link';
    openModal('socialModal');
}

function editSocialLink(id) {
    const s = allSocialCache.find(item => item.id === id);
    if (!s) return;
    document.getElementById('socialId').value = s.id;
    document.getElementById('socPlatform').value = s.platformName;
    document.getElementById('socUrl').value = s.url;
    document.getElementById('socIcon').value = s.iconClass;
    document.getElementById('socActive').checked = s.active;
    document.getElementById('socialModalTitle').textContent = 'Edit Social Link';
    openModal('socialModal');
}

async function handleSocialSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('socialId').value;
    const payload = {
        platformName: document.getElementById('socPlatform').value.trim(),
        url: document.getElementById('socUrl').value.trim(),
        iconClass: document.getElementById('socIcon').value.trim(),
        active: document.getElementById('socActive').checked
    };

    const url = id ? `/api/admin/social-links/${id}` : '/api/admin/social-links';
    const method = id ? 'PUT' : 'POST';

    const res = await adminFetch(url, {
        method: method,
        body: JSON.stringify(payload)
    });

    if (res && res.success) {
        closeModal('socialModal');
        loadSocialTable();
        showAdminToast('Social link saved successfully');
    } else {
        alert(res.message || 'Failed to save social link');
    }
}

async function deleteSocialLink(id) {
    if (!confirm('Are you sure you want to delete this social link?')) return;
    const res = await adminFetch(`/api/admin/social-links/${id}`, { method: 'DELETE' });
    if (res && res.success) {
        loadSocialTable();
        showAdminToast('Social link deleted');
    } else {
        alert(res.message || 'Failed to delete social link');
    }
}

/* ==========================================================================
   15. PASSWORD CHANGE
   ========================================================================== */
async function handlePasswordChangeSubmit(e) {
    e.preventDefault();
    const currentPassword = document.getElementById('pwdCurrent').value;
    const newPassword = document.getElementById('pwdNew').value;
    const confirmPassword = document.getElementById('pwdConfirm').value;
    const msgEl = document.getElementById('pwdMsg');

    if (newPassword !== confirmPassword) {
        msgEl.style.display = 'block';
        msgEl.style.color = '#ff6b6b';
        msgEl.textContent = 'New passwords do not match.';
        return;
    }

    const payload = {
        currentPassword: currentPassword,
        newPassword: newPassword,
        confirmPassword: confirmPassword
    };

    const res = await adminFetch('/api/auth/change-password', {
        method: 'POST',
        body: JSON.stringify(payload)
    });

    if (res && res.success) {
        closeModal('passwordModal');
        document.getElementById('passwordForm').reset();
        msgEl.style.display = 'none';
        showAdminToast('Password changed successfully!');
    } else {
        msgEl.style.display = 'block';
        msgEl.style.color = '#ff6b6b';
        msgEl.textContent = res.message || 'Failed to change password.';
    }
}

/* ==========================================================================
   HELPERS & MODALS
   ========================================================================== */
function openModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) m.classList.add('active');
}

function closeModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) m.classList.remove('active');
}

function showAdminToast(msg, type = 'success') {
    let t = document.getElementById('adminToast');
    if (!t) {
        t = document.createElement('div');
        t.id = 'adminToast';
        document.body.appendChild(t);
    }
    // Clear previous class
    t.className = '';
    const bgMap = {
        success: 'rgba(16,185,129,0.92)',
        error:   'rgba(239,68,68,0.90)',
        warning: 'rgba(245,158,11,0.92)',
        info:    'rgba(56,189,248,0.90)'
    };
    const iconMap = { success: '✅', error: '❌', warning: '⚠️', info: 'ℹ️' };
    t.style.cssText = `
        position:fixed; bottom:2rem; right:2rem;
        background:${bgMap[type] || bgMap.success};
        color:#fff; padding:0.9rem 1.4rem; border-radius:10px;
        z-index:9999; box-shadow:0 8px 32px rgba(0,0,0,0.5);
        display:flex; align-items:center; gap:0.5rem;
        font-size:0.88rem; font-weight:600; max-width:380px;
        backdrop-filter:blur(8px); -webkit-backdrop-filter:blur(8px);
        font-family:'Poppins',sans-serif;
        animation: toast-in 0.3s ease;
    `;
    t.innerHTML = `<span>${iconMap[type] || '✅'}</span><span>${msg}</span>`;
    clearTimeout(t._timer);
    t._timer = setTimeout(() => { t.style.opacity = '0'; setTimeout(() => { if(t) t.style.opacity = ''; }, 300); }, 3500);
}

function setVal(id, v) {
    const el = document.getElementById(id);
    if (el && v !== undefined && v !== null) el.value = v;
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
   EXECUTIVE ANALYTICS & VISITOR TRACKING
   ========================================================================== */
let currentAnalyticsPeriod = 'today';
async function loadExecutiveAnalytics(period = currentAnalyticsPeriod) {
    currentAnalyticsPeriod = period;
    const res = await adminFetch(`/api/admin/analytics/summary?period=${period}`);
    if (!res || !res.success || !res.data) return;
    const d = res.data;
    const pv = document.getElementById('anaPageViews');
    if (pv) pv.textContent = d.pageViews || 0;
    const uv = document.getElementById('anaUniqueVisitors');
    if (uv) uv.textContent = d.uniqueVisitors || 0;
    const prj = document.getElementById('anaProjectViews');
    if (prj) prj.textContent = d.projectViews || 0;
    const cv = document.getElementById('anaCvDownloads');
    if (cv) cv.textContent = d.resumeDownloads || 0;

    const topPagesBody = document.getElementById('anaTopPagesTable');
    if (topPagesBody) {
        if (d.topPages && Object.keys(d.topPages).length > 0) {
            topPagesBody.innerHTML = Object.entries(d.topPages)
                .sort((a, b) => b[1] - a[1])
                .slice(0, 10)
                .map(([url, count]) => `<tr><td style="font-family:monospace; font-size:0.82rem; color:#e2e8f0;">${escapeHtml(url)}</td><td style="text-align:right; font-weight:600; color:#38bdf8;">${count}</td></tr>`)
                .join('');
        } else {
            topPagesBody.innerHTML = '<tr><td colspan="2" style="text-align:center; color:#94a3b8;">No visitor traffic in this period</td></tr>';
        }
    }

    const topRefBody = document.getElementById('anaTopReferrersTable');
    if (topRefBody) {
        if (d.topReferrers && Object.keys(d.topReferrers).length > 0) {
            topRefBody.innerHTML = Object.entries(d.topReferrers)
                .sort((a, b) => b[1] - a[1])
                .slice(0, 10)
                .map(([ref, count]) => `<tr><td style="font-size:0.82rem; color:#e2e8f0;">${escapeHtml(ref)}</td><td style="text-align:right; font-weight:600; color:#38bdf8;">${count}</td></tr>`)
                .join('');
        } else {
            topRefBody.innerHTML = '<tr><td colspan="2" style="text-align:center; color:#94a3b8;">Direct / Internal Traffic</td></tr>';
        }
    }
}

function switchAnalyticsPeriod(period, btn) {
    if (btn && btn.parentElement) {
        btn.parentElement.querySelectorAll('button').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
    }
    loadExecutiveAnalytics(period);
}

/* ==========================================================================
   CLIENT LEADS & CRM PIPELINE
   ========================================================================== */
let allLeads = [];
let currentLeadFilter = 'ALL';

async function loadCrmLeads() {
    const res = await adminFetch('/api/admin/leads');
    if (!res || !res.success || !Array.isArray(res.data)) {
        const tbody = document.getElementById('crmLeadsTableBody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; color:#94a3b8;">No client leads recorded yet.</td></tr>';
        return;
    }
    allLeads = res.data;
    renderLeadsTable();
}

function filterLeadsByStatus(status, btn) {
    currentLeadFilter = status;
    const filterContainer = document.getElementById('leadStatusFilters');
    if (filterContainer && btn) {
        filterContainer.querySelectorAll('button').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
    }
    renderLeadsTable();
}

function renderLeadsTable() {
    const tbody = document.getElementById('crmLeadsTableBody');
    if (!tbody) return;
    const filtered = currentLeadFilter === 'ALL' ? allLeads : allLeads.filter(l => (l.leadStatus || 'NEW') === currentLeadFilter);
    if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; color:#94a3b8;">No client opportunities matching filter.</td></tr>';
        return;
    }
    const stageBadges = {
        'NEW': '<span style="background:#0284c7; color:#fff; padding:2px 8px; border-radius:12px; font-size:0.75rem;">New</span>',
        'CONTACTED': '<span style="background:#eab308; color:#000; padding:2px 8px; border-radius:12px; font-size:0.75rem;">Contacted</span>',
        'IN_DISCUSSION': '<span style="background:#f97316; color:#fff; padding:2px 8px; border-radius:12px; font-size:0.75rem;">In Discussion</span>',
        'QUOTATION_SENT': '<span style="background:#8b5cf6; color:#fff; padding:2px 8px; border-radius:12px; font-size:0.75rem;">Quotation Sent</span>',
        'NEGOTIATING': '<span style="background:#a855f7; color:#fff; padding:2px 8px; border-radius:12px; font-size:0.75rem;">Negotiating</span>',
        'WON': '<span style="background:#10b981; color:#fff; padding:2px 8px; border-radius:12px; font-size:0.75rem;">Won</span>',
        'LOST': '<span style="background:#64748b; color:#fff; padding:2px 8px; border-radius:12px; font-size:0.75rem;">Lost</span>'
    };
    tbody.innerHTML = filtered.map(lead => `
        <tr>
            <td style="font-weight:600; color:#fff;">${escapeHtml(lead.name || 'Anonymous')}</td>
            <td><a href="mailto:${escapeHtml(lead.email)}" style="color:#38bdf8;">${escapeHtml(lead.email)}</a><br><small style="color:#94a3b8;">${escapeHtml(lead.phone || 'No phone')}</small></td>
            <td>${escapeHtml(lead.company || '-')}</td>
            <td><span style="font-size:0.8rem; background:rgba(56,189,248,0.1); padding:2px 6px; border-radius:4px; color:#38bdf8;">${escapeHtml(lead.serviceInterest || 'General Inquiry')}</span></td>
            <td style="color:#10b981; font-weight:600;">${escapeHtml(lead.estimatedBudget || '-')}</td>
            <td>${stageBadges[lead.leadStatus || 'NEW'] || lead.leadStatus}</td>
            <td style="font-size:0.78rem; color:#94a3b8;">${lead.createdAt ? new Date(lead.createdAt).toLocaleDateString() : '-'}</td>
            <td>
                <button type="button" class="btn-admin btn-admin-secondary" style="font-size:0.75rem; padding:3px 8px;" onclick="openLeadModal(${lead.id})">Manage</button>
            </td>
        </tr>
    `).join('');
}

function openLeadModal(leadId) {
    const lead = allLeads.find(l => l.id === leadId);
    if (!lead) return;
    setVal('leadId', lead.id);
    setVal('leadName', lead.name + ' (' + lead.email + ')');
    setVal('leadStatusSelect', lead.leadStatus || 'NEW');
    setVal('leadBudget', lead.estimatedBudget || '');
    setVal('leadNotes', lead.internalNotes || '');
    openModal('leadModal');
}

async function handleSaveLead(e) {
    e.preventDefault();
    const id = document.getElementById('leadId').value;
    const payload = {
        leadStatus: document.getElementById('leadStatusSelect').value,
        estimatedBudget: document.getElementById('leadBudget').value,
        internalNotes: document.getElementById('leadNotes').value
    };
    const res = await adminFetch(`/api/admin/leads/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    if (res && res.success) {
        showAdminToast('Client lead details updated successfully');
        closeModal('leadModal');
        loadCrmLeads();
    } else {
        showAdminToast(res ? res.message : 'Failed to update lead', 'error');
    }
}

/* ==========================================================================
   LEGAL DOCUMENTS & POLICIES
   ========================================================================== */
let legalDocuments = [];
async function loadLegalDocuments() {
    const res = await adminFetch('/api/admin/legal/documents');
    if (!res || !res.success || !Array.isArray(res.data)) {
        const tbody = document.getElementById('legalDocsTableBody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:#94a3b8;">No legal documents found.</td></tr>';
        return;
    }
    legalDocuments = res.data;
    const tbody = document.getElementById('legalDocsTableBody');
    if (!tbody) return;
    if (legalDocuments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:#94a3b8;">No legal documents created. Click "+ New Policy Document" to create one.</td></tr>';
        return;
    }
    tbody.innerHTML = legalDocuments.map(doc => `
        <tr>
            <td><code style="color:#38bdf8;">${escapeHtml(doc.documentType)}</code></td>
            <td style="font-weight:600; color:#fff;">${escapeHtml(doc.title)}</td>
            <td>v${escapeHtml(doc.version)}</td>
            <td><span style="background:${doc.status === 'PUBLISHED' ? '#059669' : '#d97706'}; color:#fff; padding:2px 8px; border-radius:12px; font-size:0.75rem;">${doc.status}</span></td>
            <td>${doc.effectiveDate || '-'}</td>
            <td style="font-size:0.78rem; color:#94a3b8;">${doc.updatedAt ? new Date(doc.updatedAt).toLocaleDateString() : '-'}</td>
            <td>
                <button type="button" class="btn-admin btn-admin-secondary" style="font-size:0.75rem; padding:3px 8px;" onclick="openEditLegalDocModal(${doc.id})">Edit</button>
            </td>
        </tr>
    `).join('');
}

function openLegalDocModal() {
    setVal('legalDocId', '');
    setVal('legalDocType', 'PRIVACY_POLICY');
    setVal('legalDocTitle', 'Privacy Policy');
    setVal('legalDocVersion', '1.0');
    setVal('legalDocStatus', 'PUBLISHED');
    setVal('legalDocContent', '# Privacy Policy\n\nThis privacy policy governs data handling across this website.');
    document.getElementById('legalModalTitle').textContent = 'Create Legal Document';
    openModal('legalModal');
}

function openEditLegalDocModal(id) {
    const doc = legalDocuments.find(d => d.id === id);
    if (!doc) return;
    setVal('legalDocId', doc.id);
    setVal('legalDocType', doc.documentType);
    setVal('legalDocTitle', doc.title);
    setVal('legalDocVersion', doc.version);
    setVal('legalDocStatus', doc.status);
    setVal('legalDocContent', doc.content);
    document.getElementById('legalModalTitle').textContent = 'Edit Legal Document';
    openModal('legalModal');
}

async function handleSaveLegalDoc(e) {
    e.preventDefault();
    const id = document.getElementById('legalDocId').value;
    const payload = {
        documentType: document.getElementById('legalDocType').value,
        title: document.getElementById('legalDocTitle').value,
        version: document.getElementById('legalDocVersion').value,
        status: document.getElementById('legalDocStatus').value,
        content: document.getElementById('legalDocContent').value,
        effectiveDate: new Date().toISOString().split('T')[0]
    };
    const url = id ? `/api/admin/legal/documents/${id}` : '/api/admin/legal/documents';
    const method = id ? 'PUT' : 'POST';
    const res = await adminFetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    if (res && res.success) {
        showAdminToast('Legal document saved successfully');
        closeModal('legalModal');
        loadLegalDocuments();
    } else {
        showAdminToast(res ? res.message : 'Failed to save document', 'error');
    }
}

/* ==========================================================================
   PRIVACY & CONSENT COMPLIANCE
   ========================================================================== */
async function loadPrivacyAndCompliance() {
    const consentRes = await adminFetch('/api/admin/privacy/consent-stats');
    if (consentRes && consentRes.success && consentRes.data) {
        const d = consentRes.data;
        const setEl = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val || 0; };
        setEl('statConsentTotal', d.totalRecords || d.totalConsents || 0);
        setEl('statConsentAccepted', d.acceptedAll || 0);
        setEl('statConsentRejected', d.rejectedOptional || 0);
        setEl('statConsentCustom', d.customPreferences || 0);
    }
    loadDsarRequests();
    loadProcessors();
    loadBreaches();
}

async function loadDsarRequests() {
    const res = await adminFetch('/api/admin/privacy/requests');
    const tbody = document.getElementById('dsarTableBody');
    if (!tbody) return;
    if (!res || !res.success || !Array.isArray(res.data) || res.data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:#94a3b8;">No pending data subject requests</td></tr>';
        return;
    }
    tbody.innerHTML = res.data.map(req => `
        <tr>
            <td style="font-family:monospace; color:#38bdf8;">${escapeHtml(req.ticketNumber)}</td>
            <td style="color:#fff; font-weight:600;">${escapeHtml(req.requesterName)}</td>
            <td><a href="mailto:${escapeHtml(req.requesterEmail)}" style="color:#94a3b8;">${escapeHtml(req.requesterEmail)}</a></td>
            <td><span style="background:rgba(56,189,248,0.1); color:#38bdf8; padding:2px 6px; border-radius:4px; font-size:0.78rem;">${escapeHtml(req.requestType)}</span></td>
            <td><span style="background:#0284c7; color:#fff; padding:2px 8px; border-radius:12px; font-size:0.75rem;">${escapeHtml(req.status)}</span></td>
            <td style="color:#f59e0b; font-size:0.8rem;">${req.statutoryDeadline || '-'}</td>
            <td>
                <button type="button" class="btn-admin btn-admin-secondary" style="font-size:0.75rem; padding:3px 8px;" onclick="updateDsarStatus(${req.id})">Update Status</button>
            </td>
        </tr>
    `).join('');
}

async function updateDsarStatus(id) {
    const newStatus = prompt('Enter new status (RECEIVED, IDENTITY_VERIFIED, IN_PROGRESS, COMPLETED, REJECTED):', 'COMPLETED');
    if (!newStatus) return;
    const res = await adminFetch(`/api/admin/privacy/requests/${id}/status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus.trim().toUpperCase() })
    });
    if (res && res.success) {
        showAdminToast('DSAR ticket updated');
        loadDsarRequests();
    } else {
        showAdminToast(res ? res.message : 'Update failed', 'error');
    }
}

async function loadProcessors() {
    const res = await adminFetch('/api/admin/privacy/processors');
    const tbody = document.getElementById('processorsTableBody');
    if (!tbody) return;
    if (!res || !res.success || !Array.isArray(res.data) || res.data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" style="text-align:center; color:#94a3b8;">No third-party processors registered</td></tr>';
        return;
    }
    tbody.innerHTML = res.data.map(p => `
        <tr>
            <td style="color:#fff; font-weight:600;">${escapeHtml(p.providerName)}</td>
            <td style="color:#94a3b8; font-size:0.82rem;">${escapeHtml(p.servicePurpose)}</td>
            <td><span style="color:${p.enabled ? '#10b981' : '#64748b'}; font-weight:600; font-size:0.78rem;">${p.enabled ? 'Active' : 'Disabled'}</span></td>
        </tr>
    `).join('');
}

async function loadBreaches() {
    const res = await adminFetch('/api/admin/privacy/breaches');
    const tbody = document.getElementById('breachesTableBody');
    if (!tbody) return;
    if (!res || !res.success || !Array.isArray(res.data) || res.data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; color:#94a3b8;">No security breaches on record</td></tr>';
        return;
    }
    tbody.innerHTML = res.data.map(b => `
        <tr>
            <td style="color:#fff; font-weight:600;">${escapeHtml(b.incidentDescription)}</td>
            <td style="color:#94a3b8; font-size:0.8rem;">${b.discoveryDate ? new Date(b.discoveryDate).toLocaleDateString() : '-'}</td>
            <td><span style="color:#ef4444; font-weight:600; font-size:0.78rem;">${escapeHtml(b.riskAssessment || 'LOW')}</span></td>
            <td><span style="background:#0284c7; color:#fff; padding:2px 8px; border-radius:12px; font-size:0.75rem;">${escapeHtml(b.resolutionStatus || 'RESOLVED')}</span></td>
        </tr>
    `).join('');
}

async function openProcessorModal() {
    const name = prompt('Enter Processor Name (e.g. AWS, Render, Vercel):');
    if (!name) return;
    const purpose = prompt('Enter Processing Purpose (e.g. Database Hosting, Web Hosting):');
    const res = await adminFetch('/api/admin/privacy/processors', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            providerName: name,
            servicePurpose: purpose || 'Infrastructure hosting',
            dataCategories: 'Technical telemetry, request IP',
            dpaStatus: 'EXECUTED',
            enabled: true
        })
    });
    if (res && res.success) {
        showAdminToast('Processor registered');
        loadProcessors();
    } else {
        showAdminToast(res ? res.message : 'Error registering processor', 'error');
    }
}

async function openBreachModal() {
    const desc = prompt('Enter Incident Summary:');
    if (!desc) return;
    const res = await adminFetch('/api/admin/privacy/breaches', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            incidentDescription: desc,
            discoveryDate: new Date().toISOString(),
            riskAssessment: 'LOW',
            affectedSystems: 'None identified',
            resolutionStatus: 'UNDER_INVESTIGATION'
        })
    });
    if (res && res.success) {
        showAdminToast('Incident recorded');
        loadBreaches();
    } else {
        showAdminToast(res ? res.message : 'Error recording incident', 'error');
    }
}

/* ==========================================================================
   SECURITY CONTROL CENTRE & EMERGENCY REVOCATION
   ========================================================================== */
async function loadSecurityCenter() {
    const res = await adminFetch('/api/admin/security-center/summary');
    if (!res || !res.success || !res.data) return;
    const d = res.data;
    const setEl = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val; };
    setEl('secScore', (d.securityScore || 95) + '%');
    setEl('secMfaStatus', d.mfaEnforced ? 'Enforced' : 'Optional');
    setEl('secActiveDevices', d.activeDevicesCount || 1);
    setEl('secFailedLogins', d.recentFailedLogins || 0);

    const devTable = document.getElementById('secDevicesTableBody');
    if (devTable) {
        if (d.authorizedDevices && d.authorizedDevices.length > 0) {
            devTable.innerHTML = d.authorizedDevices.map(dev => `
                <tr>
                    <td style="color:#fff; font-weight:600;">${escapeHtml(dev.deviceName || 'Admin Browser')}</td>
                    <td><span style="background:rgba(56,189,248,0.1); color:#38bdf8; padding:2px 8px; border-radius:4px; font-size:0.78rem;">${escapeHtml(dev.deviceType || 'Desktop')}</span></td>
                    <td style="font-size:0.8rem; color:#94a3b8;">${dev.createdAt ? new Date(dev.createdAt).toLocaleDateString() : '-'}</td>
                    <td style="font-size:0.8rem; color:#94a3b8;">${dev.lastActiveAt ? new Date(dev.lastActiveAt).toLocaleString() : 'Active now'}</td>
                    <td><span style="color:#10b981; font-size:0.8rem;">Authorized</span></td>
                </tr>
            `).join('');
        } else {
            devTable.innerHTML = '<tr><td colspan="5" style="text-align:center; color:#94a3b8;">Current authenticated session is active.</td></tr>';
        }
    }

    const logTable = document.getElementById('secAuthLogsTableBody');
    if (logTable) {
        if (d.recentAuthLogs && d.recentAuthLogs.length > 0) {
            logTable.innerHTML = d.recentAuthLogs.map(log => `
                <tr>
                    <td style="font-size:0.8rem; color:#94a3b8;">${log.createdAt ? new Date(log.createdAt).toLocaleString() : '-'}</td>
                    <td><code style="color:#38bdf8;">${escapeHtml(log.authMethod || 'PASSWORD')}</code></td>
                    <td style="color:#e2e8f0; font-size:0.8rem;">${escapeHtml(log.deviceInfo || 'Browser Session')}</td>
                    <td style="font-family:monospace; font-size:0.8rem; color:#94a3b8;">${escapeHtml(log.ipAddress || '-')}</td>
                    <td><span style="color:${log.success ? '#10b981' : '#ef4444'}; font-weight:600; font-size:0.8rem;">${log.success ? '✅ Success' : '❌ Failed'}</span></td>
                </tr>
            `).join('');
        } else {
            logTable.innerHTML = '<tr><td colspan="5" style="text-align:center; color:#94a3b8;">No recent login logs.</td></tr>';
        }
    }
}

async function triggerEmergencyRevocation() {
    if (!confirm('EMERGENCY REVOCATION:\nThis will immediately invalidate ALL active administrator JWT tokens across all browsers and devices. You will be required to re-authenticate immediately.\n\nAre you sure you want to proceed?')) {
        return;
    }
    const res = await adminFetch('/api/admin/security-center/emergency-revoke-all', {
        method: 'POST'
    });
    if (res && res.success) {
        showAdminToast('Emergency revocation executed. All sessions invalidated.', 'warning');
        setTimeout(() => {
            localStorage.removeItem('adminToken');
            localStorage.removeItem('adminUser');
            window.location.href = '/admin/login.html';
        }, 1500);
    } else {
        showAdminToast(res ? res.message : 'Emergency revocation failed', 'error');
    }
}

/* ==========================================================================
   AI ASSISTANT TOOLS
   ========================================================================== */
function loadAiTools() {}

async function runAiProjectEnhance() {
    const title = document.getElementById('aiProjectTitle').value;
    const tech = document.getElementById('aiProjectTech').value;
    const notes = document.getElementById('aiProjectNotes').value;
    if (!title && !notes) {
        showAdminToast('Please provide a project title or notes', 'warning');
        return;
    }
    const res = await adminFetch('/api/admin/ai/assist', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            taskType: 'ENHANCE_PROJECT_DESCRIPTION',
            contextTitle: title,
            technologies: tech,
            inputContent: notes
        })
    });
    if (res && res.success && res.data) {
        const box = document.getElementById('aiProjectResult');
        const text = document.getElementById('aiProjectResultText');
        if (box && text) {
            text.textContent = res.data.suggestedContent;
            box.style.display = 'block';
        }
        showAdminToast('Description enhanced');
    } else {
        showAdminToast(res ? res.message : 'Enhancement failed', 'error');
    }
}

async function runAiSeoGenerate() {
    const title = document.getElementById('aiSeoTitle').value;
    const category = document.getElementById('aiSeoCategory').value;
    if (!title) {
        showAdminToast('Please enter a content title or topic', 'warning');
        return;
    }
    const res = await adminFetch('/api/admin/ai/assist', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            taskType: 'GENERATE_SEO_METADATA',
            contextTitle: title,
            technologies: category
        })
    });
    if (res && res.success && res.data) {
        const box = document.getElementById('aiSeoResult');
        const t = document.getElementById('aiSeoResultTitle');
        const d = document.getElementById('aiSeoResultDesc');
        if (box && t && d) {
            t.textContent = res.data.seoTitle || '';
            d.textContent = res.data.seoDescription || '';
            box.style.display = 'block';
        }
        showAdminToast('SEO metadata generated');
    } else {
        showAdminToast(res ? res.message : 'Generation failed', 'error');
    }
}

/* ==========================================================================
   SYSTEM NOTIFICATIONS & BELL
   ========================================================================== */
let notificationsDrawerOpen = false;
async function loadNotifications() {
    const res = await adminFetch('/api/admin/notifications');
    if (!res || !res.success || !Array.isArray(res.data)) return;
    const notifs = res.data;
    const unread = notifs.filter(n => !n.read).length;
    const badge = document.getElementById('topbarNotifBadge');
    if (badge) {
        badge.textContent = unread;
        badge.style.display = unread > 0 ? 'inline-block' : 'none';
    }
    const list = document.getElementById('notificationList');
    if (list) {
        if (notifs.length === 0) {
            list.innerHTML = '<p style="color:#94a3b8; font-size:0.82rem; text-align:center; margin:10px 0;">No new notifications</p>';
        } else {
            list.innerHTML = notifs.slice(0, 8).map(n => `
                <div style="background:${n.read ? 'rgba(255,255,255,0.03)' : 'rgba(56,189,248,0.1)'}; padding:8px 10px; border-radius:6px; border-left:3px solid ${n.read ? '#64748b' : '#38bdf8'}; font-size:0.8rem;">
                    <div style="font-weight:600; color:#fff; margin-bottom:2px;">${escapeHtml(n.title)}</div>
                    <div style="color:#cbd5e1; font-size:0.78rem;">${escapeHtml(n.message)}</div>
                    <div style="color:#64748b; font-size:0.72rem; margin-top:4px;">${n.createdAt ? new Date(n.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : ''}</div>
                </div>
            `).join('');
        }
    }
}

function toggleNotificationDrawer() {
    const dd = document.getElementById('notificationDropdown');
    if (!dd) return;
    notificationsDrawerOpen = !notificationsDrawerOpen;
    dd.style.display = notificationsDrawerOpen ? 'block' : 'none';
    if (notificationsDrawerOpen) {
        loadNotifications();
    }
}

async function markAllNotificationsRead() {
    const res = await adminFetch('/api/admin/notifications/mark-all-read', { method: 'POST' });
    if (res && res.success) {
        loadNotifications();
        showAdminToast('All notifications marked as read');
    }
}
