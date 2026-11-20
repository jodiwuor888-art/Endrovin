/* ==========================================================================
   COOKIE CONSENT & DATA PRIVACY MANAGER
   Compliant with Kenya Data Protection Act, 2019 & International Principles
   ========================================================================== */

(function() {
    'use strict';

    const STORAGE_KEY = 'joash_privacy_consent';
    const CONSENT_VERSION = '1.0';

    function getStoredConsent() {
        try {
            const raw = localStorage.getItem(STORAGE_KEY);
            return raw ? JSON.parse(raw) : null;
        } catch (e) {
            return null;
        }
    }

    function saveConsent(preferences, decision) {
        const record = {
            consentId: 'c_' + Math.random().toString(36).substring(2, 11),
            necessary: true,
            functional: !!preferences.functional,
            analytics: !!preferences.analytics,
            marketing: !!preferences.marketing,
            decision: decision,
            policyVersion: CONSENT_VERSION,
            timestamp: new Date().toISOString()
        };
        try {
            localStorage.setItem(STORAGE_KEY, JSON.stringify(record));
        } catch (e) {}

        // Send to public backend audit log
        if (typeof API_BASE !== 'undefined') {
            fetch(`${API_BASE}/api/public/privacy/consent`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(record)
            }).catch(() => {});
        }

        // Dispatch event for analytics/functional modules
        window.dispatchEvent(new CustomEvent('cookie-consent-updated', { detail: record }));
        hideConsentBanner();
        hideConsentModal();
    }

    function initCookieConsent() {
        const stored = getStoredConsent();
        if (!stored) {
            showConsentBanner();
        } else {
            window.dispatchEvent(new CustomEvent('cookie-consent-updated', { detail: stored }));
        }
    }

    function showConsentBanner() {
        if (document.getElementById('cookieConsentBanner')) return;
        const banner = document.createElement('div');
        banner.id = 'cookieConsentBanner';
        banner.style.cssText = `
            position: fixed;
            bottom: 20px;
            left: 20px;
            right: 20px;
            max-width: 580px;
            margin: 0 auto;
            background: rgba(10, 16, 31, 0.95);
            border: 1px solid rgba(56, 189, 248, 0.3);
            border-radius: 12px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.6);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            padding: 18px 22px;
            z-index: 99999;
            color: #f1f5f9;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            font-size: 0.88rem;
            line-height: 1.5;
            animation: consentSlideUp 0.35s ease-out;
        `;

        banner.innerHTML = `
            <div style="display:flex; align-items:flex-start; gap:12px; margin-bottom:12px;">
                <span style="font-size:1.5rem;">🛡️</span>
                <div>
                    <strong style="color:#fff; font-size:0.95rem; display:block; margin-bottom:2px;">Privacy &amp; Cookie Preferences</strong>
                    <span style="color:#94a3b8; font-size:0.84rem;">
                        This portfolio respects your privacy under the Kenya Data Protection Act, 2019. We use strictly necessary cookies for security, and optional privacy-conscious cookies to analyze visitor engagement.
                    </span>
                </div>
            </div>
            <div style="display:flex; justify-content:flex-end; gap:8px; flex-wrap:wrap;">
                <button type="button" id="btnRejectCookies" style="background:transparent; border:1px solid rgba(255,255,255,0.2); color:#cbd5e1; padding:6px 14px; border-radius:6px; cursor:pointer; font-size:0.82rem;">Reject Optional</button>
                <button type="button" id="btnCustomCookies" style="background:rgba(56,189,248,0.12); border:1px solid rgba(56,189,248,0.4); color:#38bdf8; padding:6px 14px; border-radius:6px; cursor:pointer; font-size:0.82rem;">Preferences</button>
                <button type="button" id="btnAcceptCookies" style="background:#0284c7; border:none; color:#fff; font-weight:600; padding:6px 16px; border-radius:6px; cursor:pointer; font-size:0.82rem;">Accept All</button>
            </div>
        `;

        document.body.appendChild(banner);

        document.getElementById('btnAcceptCookies').onclick = function() {
            saveConsent({ functional: true, analytics: true, marketing: false }, 'ACCEPTED_ALL');
        };
        document.getElementById('btnRejectCookies').onclick = function() {
            saveConsent({ functional: false, analytics: false, marketing: false }, 'REJECTED_OPTIONAL');
        };
        document.getElementById('btnCustomCookies').onclick = function() {
            showConsentModal();
        };
    }

    function hideConsentBanner() {
        const b = document.getElementById('cookieConsentBanner');
        if (b) b.remove();
    }

    function showConsentModal() {
        if (document.getElementById('cookiePreferencesModal')) {
            document.getElementById('cookiePreferencesModal').style.display = 'flex';
            return;
        }

        const stored = getStoredConsent() || { functional: true, analytics: true, marketing: false };
        const modal = document.createElement('div');
        modal.id = 'cookiePreferencesModal';
        modal.style.cssText = `
            position: fixed; inset: 0; background: rgba(0,0,0,0.7);
            display: flex; align-items: center; justify-content: center;
            z-index: 100000; padding: 20px;
        `;

        modal.innerHTML = `
            <div style="background:#0a101f; border:1px solid rgba(56,189,248,0.3); border-radius:14px; max-width:480px; width:100%; padding:22px; color:#fff; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
                    <strong style="font-size:1.05rem;">Privacy &amp; Cookie Preferences</strong>
                    <button type="button" id="btnClosePrefModal" style="background:none; border:none; color:#94a3b8; font-size:1.3rem; cursor:pointer;">&times;</button>
                </div>
                <div style="display:flex; flex-direction:column; gap:12px; margin-bottom:18px; font-size:0.85rem;">
                    <div style="background:rgba(255,255,255,0.03); padding:10px 12px; border-radius:8px; display:flex; justify-content:space-between; align-items:center;">
                        <div>
                            <strong style="display:block; color:#fff;">Strictly Necessary</strong>
                            <span style="color:#94a3b8; font-size:0.78rem;">Required for security, CSRF protection, and core navigation.</span>
                        </div>
                        <span style="color:#10b981; font-weight:600; font-size:0.8rem;">Always Active</span>
                    </div>

                    <div style="background:rgba(255,255,255,0.03); padding:10px 12px; border-radius:8px; display:flex; justify-content:space-between; align-items:center;">
                        <div>
                            <strong style="display:block; color:#fff;">Analytics &amp; Usage</strong>
                            <span style="color:#94a3b8; font-size:0.78rem;">First-party anonymous metrics to understand viewed projects and portfolio reach.</span>
                        </div>
                        <input type="checkbox" id="prefAnalytics" ${stored.analytics ? 'checked' : ''} style="width:18px; height:18px; accent-color:#0284c7;">
                    </div>

                    <div style="background:rgba(255,255,255,0.03); padding:10px 12px; border-radius:8px; display:flex; justify-content:space-between; align-items:center;">
                        <div>
                            <strong style="display:block; color:#fff;">Functional</strong>
                            <span style="color:#94a3b8; font-size:0.78rem;">Saves user interface theme and display customizations.</span>
                        </div>
                        <input type="checkbox" id="prefFunctional" ${stored.functional ? 'checked' : ''} style="width:18px; height:18px; accent-color:#0284c7;">
                    </div>
                </div>

                <div style="display:flex; justify-content:flex-end; gap:8px;">
                    <button type="button" id="btnSavePreferences" style="background:#0284c7; border:none; color:#fff; font-weight:600; padding:8px 18px; border-radius:6px; cursor:pointer; font-size:0.85rem;">Save Preferences</button>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        document.getElementById('btnClosePrefModal').onclick = hideConsentModal;
        document.getElementById('btnSavePreferences').onclick = function() {
            const ana = document.getElementById('prefAnalytics').checked;
            const func = document.getElementById('prefFunctional').checked;
            saveConsent({ functional: func, analytics: ana, marketing: false }, 'CUSTOMIZED');
        };
    }

    function hideConsentModal() {
        const m = document.getElementById('cookiePreferencesModal');
        if (m) m.style.display = 'none';
    }

    // Expose global methods
    window.openPrivacyPreferences = showConsentModal;

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initCookieConsent);
    } else {
        initCookieConsent();
    }
})();
