/* ==========================================================================
   JOASH PORTFOLIO — TURBO SERVICE WORKER v3.0
   Strategy: Instant cache-first for static assets, stale-while-revalidate
   for pages, network-first with 3s timeout for API calls.
   ========================================================================== */

const CACHE_NAME = 'joash-portfolio-v3';
const STATIC_CACHE = 'joash-static-v3';
const API_CACHE = 'joash-api-v3';
const OFFLINE_URL = './offline.html';

const PRECACHE_ASSETS = [
    './',
    './index.html',
    './about.html',
    './projects.html',
    './services.html',
    './testimonials.html',
    './contact.html',
    './offline.html',
    './manifest.json',
    './assets/css/style.css',
    './assets/css/responsive.css',
    './assets/js/api.js',
    './assets/js/main.js',
    './assets/img/profile.png',
    './assets/img/Joash.jpeg'
];

// Install: pre-cache everything in parallel
self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(STATIC_CACHE)
            .then((cache) => cache.addAll(PRECACHE_ASSETS).catch(() => {}))
            .then(() => self.skipWaiting())
    );
});

// Activate: wipe ALL old caches immediately
self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((keys) =>
            Promise.all(keys
                .filter((k) => ![CACHE_NAME, STATIC_CACHE, API_CACHE].includes(k))
                .map((k) => caches.delete(k))
            )
        ).then(() => self.clients.claim())
    );
});

// Helper: fetch with timeout
function fetchWithTimeout(request, timeoutMs = 3000) {
    return new Promise((resolve, reject) => {
        const timer = setTimeout(() => reject(new Error('timeout')), timeoutMs);
        fetch(request).then(
            (res) => { clearTimeout(timer); resolve(res); },
            (err) => { clearTimeout(timer); reject(err); }
        );
    });
}

self.addEventListener('fetch', (event) => {
    const { request } = event;
    const url = new URL(request.url);

    // SECURITY: bypass SW for admin, auth, non-GET
    if (
        url.pathname.includes('/admin') ||
        url.pathname.includes('/api/admin') ||
        url.pathname.includes('/api/auth') ||
        request.headers.has('Authorization') ||
        request.method !== 'GET'
    ) { return; }

    // PUBLIC API: stale-while-revalidate (serve cache instantly, refresh in background)
    if (url.pathname.includes('/api/public/')) {
        event.respondWith((async () => {
            const cached = await caches.match(request);
            const networkFetch = fetchWithTimeout(request, 4000)
                .then((res) => {
                    if (res && res.status === 200) {
                        const clone = res.clone();
                        caches.open(API_CACHE).then((c) => c.put(request, clone));
                    }
                    return res;
                })
                .catch(() => null);
            // Return cached immediately if available, else wait for network
            return cached || await networkFetch || caches.match(OFFLINE_URL);
        })());
        return;
    }

    // HTML NAVIGATION: stale-while-revalidate
    if (request.mode === 'navigate') {
        event.respondWith((async () => {
            const cached = await caches.match(request);
            // Always attempt network refresh in background
            const networkFetch = fetch(request).then((res) => {
                if (res && res.status === 200) {
                    const clone = res.clone();
                    caches.open(STATIC_CACHE).then((c) => c.put(request, clone));
                }
                return res;
            }).catch(() => null);
            // If we have cache, return it immediately (fastest possible)
            if (cached) {
                networkFetch.catch(() => {});
                return cached;
            }
            return (await networkFetch) || (await caches.match(OFFLINE_URL));
        })());
        return;
    }

    // STATIC ASSETS (CSS/JS/fonts/images): Cache-first, instant
    event.respondWith((async () => {
        const cached = await caches.match(request);
        if (cached) {
            // Background update
            fetch(request).then((res) => {
                if (res && res.status === 200) {
                    caches.open(STATIC_CACHE).then((c) => c.put(request, res));
                }
            }).catch(() => {});
            return cached;
        }
        try {
            const res = await fetch(request);
            if (res && res.status === 200 && request.url.startsWith('http')) {
                const clone = res.clone();
                caches.open(STATIC_CACHE).then((c) => c.put(request, clone));
            }
            return res;
        } catch {
            if (request.destination === 'image') return caches.match('./assets/img/profile.png');
        }
    })());
});


// Cache bust handler: admin publish triggers this in all public tabs
self.addEventListener('message', (event) => {
    if (event.data === 'SKIP_WAITING') {
        self.skipWaiting();
    }
    if (event.data === 'BUST_API_CACHE') {
        // Delete all cached public API responses to force fresh fetch
        caches.open(API_CACHE).then((cache) => {
            cache.keys().then((keys) => {
                keys.forEach((req) => {
                    if (req.url.includes('/api/public/')) {
                        cache.delete(req);
                    }
                });
            });
        });
    }
});

