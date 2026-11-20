/* ==========================================================================
   FIRST-PARTY PRIVACY-CONSCIOUS ANALYTICS CLIENT
   Respects Kenya Data Protection Act 2019 & Visitor Consent
   ========================================================================== */

(function() {
    'use strict';

    let visitorSessionId = (function() {
        try {
            let s = sessionStorage.getItem('joash_session_id');
            if (!s) {
                s = 's_' + Math.random().toString(36).substring(2, 12);
                sessionStorage.setItem('joash_session_id', s);
            }
            return s;
        } catch (e) {
            return 's_default';
        }
    })();

    function canTrack() {
        try {
            const raw = localStorage.getItem('joash_privacy_consent');
            if (!raw) return false; // Default opt-out until consented
            const consent = JSON.parse(raw);
            return consent && consent.analytics === true;
        } catch (e) {
            return false;
        }
    }

    function sendEvent(eventType, eventData = {}) {
        if (!canTrack()) return;
        if (typeof API_BASE === 'undefined') return;

        const payload = {
            eventType: eventType,
            pageUrl: window.location.pathname || '/',
            referrer: document.referrer || '',
            sessionId: visitorSessionId,
            eventData: eventData
        };

        fetch(`${API_BASE}/api/public/analytics/event`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        }).catch(() => {});
    }

    // Track initial page view
    function trackPageView() {
        sendEvent('page_view', {
            title: document.title,
            screenWidth: window.innerWidth,
            language: navigator.language
        });
    }

    // Attach interaction observers
    function initInteractionTracking() {
        // Track outbound social clicks and CV downloads
        document.addEventListener('click', function(e) {
            const link = e.target.closest('a');
            if (!link) return;
            const href = link.getAttribute('href') || '';

            if (href.includes('github.com')) {
                sendEvent('github_click', { href });
            } else if (href.includes('linkedin.com')) {
                sendEvent('linkedin_click', { href });
            } else if (href.includes('wa.me') || href.includes('whatsapp.com')) {
                sendEvent('whatsapp_click', { href });
            } else if (href.startsWith('mailto:')) {
                sendEvent('email_click', { href });
            } else if (href.includes('cv') || href.endsWith('.pdf') || link.classList.contains('cv-btn')) {
                sendEvent('resume_download', { href });
            }

            // Project link tracking
            const projectCard = link.closest('.project-card') || link.closest('[data-project-id]');
            if (projectCard) {
                const title = projectCard.querySelector('h3, .project-title')?.textContent || 'Project';
                sendEvent('project_view', { title: title.trim() });
            }
        });
    }

    // Expose global tracker
    window.trackPortfolioEvent = sendEvent;

    // Listen for consent updates
    window.addEventListener('cookie-consent-updated', function(e) {
        if (e.detail && e.detail.analytics) {
            trackPageView();
        }
    });

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            if (canTrack()) trackPageView();
            initInteractionTracking();
        });
    } else {
        if (canTrack()) trackPageView();
        initInteractionTracking();
    }
})();
