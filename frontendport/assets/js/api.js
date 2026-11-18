/* ==========================================================================
   PORTFOLIO API CLIENT
   Connects frontend interfaces with Java Spring Boot REST endpoints
   ========================================================================== */

const API_BASE = (function() {
    const configuredBase = document.querySelector('meta[name="api-base-url"]');
    if (configuredBase && configuredBase.content.trim()) {
        return configuredBase.content.trim().replace(/\/+$/, '');
    }

    if (window.location.protocol === 'http:' || window.location.protocol === 'https:') {
        if (window.location.port === '8080') {
            return '';
        }

        return `${window.location.protocol}//${window.location.hostname || 'localhost'}:8080`;
    }

    return 'http://localhost:8080';
})();

const API = {
    // Public Endpoints
    async getPublicPortfolio() {
        try {
            const res = await fetch(`${API_BASE}/api/public/portfolio`, {
                method: 'GET',
                headers: { 'Accept': 'application/json' }
            });
            return await res.json();
        } catch (err) {
            console.error('API Error fetching portfolio:', err);
            return { success: false, message: 'Could not connect to backend service.' };
        }
    },

    async sendContactMessage(payload) {
        try {
            const res = await fetch(`${API_BASE}/api/public/contact`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(payload)
            });
            return await res.json();
        } catch (err) {
            console.error('API Error sending message:', err);
            const backendAddress = API_BASE || window.location.origin;
            return {
                success: false,
                message: `Cannot reach the contact service at ${backendAddress}. Start the backend and try again.`
            };
        }
    },

    // Auth & Status
    async getAuthStatus() {
        try {
            const res = await fetch(`${API_BASE}/api/auth/status`);
            return await res.json();
        } catch (err) {
            return { success: false, message: 'Server unavailable' };
        }
    },

    async login(username, password) {
        const res = await fetch(`${API_BASE}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        return await res.json();
    },

    async requestPasswordReset(email) {
        const res = await fetch(`${API_BASE}/api/auth/request-password-reset`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({ email })
        });
        return await res.json();
    },

    async resetPassword(token, newPassword) {
        const res = await fetch(`${API_BASE}/api/auth/reset-password`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({ token, newPassword })
        });
        return await res.json();
    },

    async setupFirstAdmin(payload) {
        const res = await fetch(`${API_BASE}/api/auth/setup-first-admin`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        return await res.json();
    },

    // Public Settings & Dynamic Theming
    async getPublicSettings() {
        try {
            const res = await fetch(`${API_BASE}/api/public/settings`, {
                headers: { 'Accept': 'application/json' }
            });
            return await res.json();
        } catch (err) {
            console.error('Error fetching public settings:', err);
            return { success: false, data: {} };
        }
    },

    // Public Testimonials & Recommendations
    async getApprovedTestimonials() {
        try {
            const res = await fetch(`${API_BASE}/api/public/testimonials`, {
                headers: { 'Accept': 'application/json' }
            });
            return await res.json();
        } catch (err) {
            console.error('Error fetching testimonials:', err);
            return { success: false, data: [] };
        }
    },

    async getTestimonialCaptcha() {
        try {
            const res = await fetch(`${API_BASE}/api/public/testimonials/captcha`, {
                headers: { 'Accept': 'application/json' }
            });
            return await res.json();
        } catch (err) {
            console.error('Error fetching captcha:', err);
            return { success: false };
        }
    },

    async submitTestimonial(payload) {
        try {
            const res = await fetch(`${API_BASE}/api/public/testimonials`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(payload)
            });
            return await res.json();
        } catch (err) {
            console.error('Error submitting testimonial:', err);
            return { success: false, message: 'Could not connect to recommendation service' };
        }
    },

    // WebAuthn Passkey Authentication (Step 2)
    async getPasskeyAuthOptions(username, pendingToken) {
        const headers = { 'Content-Type': 'application/json', 'Accept': 'application/json' };
        if (pendingToken) headers['Authorization'] = `Bearer ${pendingToken}`;

        const res = await fetch(`${API_BASE}/api/admin/passkey/authentication/options`, {
            method: 'POST',
            headers,
            body: JSON.stringify({ username })
        });
        return await res.json();
    },

    async verifyPasskeyAuth(verifyPayload, pendingToken) {
        const headers = { 'Content-Type': 'application/json', 'Accept': 'application/json' };
        if (pendingToken) headers['Authorization'] = `Bearer ${pendingToken}`;

        const res = await fetch(`${API_BASE}/api/admin/passkey/authentication/verify`, {
            method: 'POST',
            headers,
            body: JSON.stringify(verifyPayload)
        });
        return await res.json();
    },

    async verifyRecoveryCode(payload) {
        const res = await fetch(`${API_BASE}/api/admin/passkey/recovery-verify`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify(payload)
        });
        return await res.json();
    }
};

function showToast(message, type = 'success') {
    let toast = document.getElementById('globalToast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'globalToast';
        toast.className = 'alert-toast';
        document.body.appendChild(toast);
    }
    toast.className = `alert-toast ${type}`;
    toast.textContent = message;
    toast.style.display = 'block';

    setTimeout(() => {
        toast.style.display = 'none';
    }, 4000);
}

/* ==========================================================================
   PERFORMANCE LAYER — Request cache + backend warm-up
   ========================================================================== */

// In-memory response cache (avoids redundant API calls on the same page load)
const _apiCache = new Map();
const _apiCacheTTL = 30000; // 30 seconds

function _cachedFetch(key, fetchFn) {
    const hit = _apiCache.get(key);
    if (hit && Date.now() - hit.ts < _apiCacheTTL) return Promise.resolve(hit.data);
    return fetchFn().then((data) => {
        _apiCache.set(key, { data, ts: Date.now() });
        return data;
    });
}

// Backend warm-up: fires a lightweight HEAD ping as soon as possible
// so the Render free-tier container is awake before user interactions
(function warmUpBackend() {
    if (typeof API_BASE === 'undefined') return;
    setTimeout(() => {
        fetch(API_BASE + '/actuator/health', {
            method: 'GET',
            cache: 'no-store',
            signal: AbortSignal.timeout ? AbortSignal.timeout(8000) : undefined
        }).catch(() => {});
    }, 500);
})();

// Prefetch public portfolio data on load (prime the cache)
(function prefetchPortfolioData() {
    if (typeof API_BASE === 'undefined') return;
    setTimeout(() => {
        fetch(API_BASE + '/api/public/portfolio', {
            headers: { 'Accept': 'application/json' },
            cache: 'force-cache'
        }).catch(() => {});
    }, 800);
})();

/* ==========================================================================
   ADMIN PUBLISH API — Triggers instant cache eviction + cross-tab auto-refresh
   Called automatically after every successful admin save/update/delete.
   ========================================================================== */

// BroadcastChannel: signals all public tabs in the same browser instantly
const _publishChannel = (() => {
    try { return new BroadcastChannel('portfolio_publish'); } catch { return null; }
})();

Object.assign(API, {
    async publishChanges() {
        try {
            const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
            const headers = { 'Content-Type': 'application/json', 'Accept': 'application/json' };
            if (token) headers['Authorization'] = `Bearer ${token}`;
            const res = await fetch(`${API_BASE}/api/admin/publish`, {
                method: 'POST',
                headers,
                cache: 'no-store'
            });
            const data = await res.json();
            const version = data.version || data.data?.version || Date.now();
            console.log('[AutoRefresh] Published — version:', version);

            // 🔊 Broadcast to all public tabs in the same browser — instant refresh
            if (_publishChannel) {
                _publishChannel.postMessage({ type: 'PUBLISHED', version });
                console.log('[AutoRefresh] Broadcast sent to public tabs');
            }

            return data;
        } catch (err) {
            console.warn('[AutoRefresh] Publish trigger failed (non-critical):', err);
            return { published: false };
        }
    }
});

// ── Auto-intercept: wrap fetch to trigger publish on every successful admin write ──
(function patchAdminFetch() {
    const originalFetch = window.fetch;
    window.fetch = async function(resource, options = {}) {
        const response = await originalFetch(resource, options);

        const url = typeof resource === 'string' ? resource : resource?.url || '';
        const method = (options.method || 'GET').toUpperCase();
        const isAdminWrite = url.includes('/api/admin/') &&
                             ['POST', 'PUT', 'PATCH', 'DELETE'].includes(method) &&
                             !url.includes('/api/admin/publish') &&
                             !url.includes('/api/auth/') &&
                             !url.includes('/api/admin/passkey');

        if (isAdminWrite && response.ok) {
            const cloned = response.clone();
            // Trigger publish 200ms after save (non-blocking)
            setTimeout(() => {
                if (typeof API !== 'undefined' && typeof API.publishChanges === 'function') {
                    API.publishChanges().catch(() => {});
                }
            }, 200);
            return cloned;
        }
        return response;
    };
})();
