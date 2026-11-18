/* ==========================================================================
   JOASH PORTFOLIO - PROGRESSIVE WEB APP SERVICE WORKER (v1.0.0)
   Secure caching policy: Public static shell cached; Admin & Auth strictly bypassed
   ========================================================================== */

const CACHE_NAME = 'joash-portfolio-static-v1';
const OFFLINE_URL = './offline.html';

// Public static assets safe to pre-cache
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
    './assets/img/Joash.jpeg',
    './assets/img/cyber-car-bg.jpg'
];

// Install: Cache public static shell
self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => {
            return cache.addAll(PRECACHE_ASSETS).catch((err) => {
                console.warn('[PWA SW] Pre-caching partial failure, proceeding:', err);
            });
        }).then(() => self.skipWaiting())
    );
});

// Activate: Clean up older cache versions
self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((keys) => {
            return Promise.all(
                keys.map((key) => {
                    if (key !== CACHE_NAME) {
                        return caches.delete(key);
                    }
                })
            );
        }).then(() => self.clients.claim())
    );
});

// Fetch: Smart routing & security boundaries
self.addEventListener('fetch', (event) => {
    const request = event.request;
    const url = new URL(request.url);

    // 1. SECURITY: Completely bypass Service Worker for all admin & auth routes
    if (
        url.pathname.includes('/admin') ||
        url.pathname.includes('/api/admin') ||
        url.pathname.includes('/api/auth') ||
        request.headers.has('Authorization') ||
        request.method !== 'GET'
    ) {
        // Direct network fetch — no caching of credentials or admin views
        return;
    }

    // 2. Public API requests: Network-First with Cache Fallback
    if (url.pathname.includes('/api/public/')) {
        event.respondWith(
            fetch(request)
                .then((networkResponse) => {
                    if (networkResponse && networkResponse.status === 200) {
                        const copy = networkResponse.clone();
                        caches.open(CACHE_NAME).then((cache) => cache.put(request, copy));
                    }
                    return networkResponse;
                })
                .catch(() => caches.match(request))
        );
        return;
    }

    // 3. Navigation requests (HTML pages): Network first, then Cache, then Offline Fallback
    if (request.mode === 'navigate') {
        event.respondWith(
            fetch(request)
                .then((networkResponse) => {
                    if (networkResponse && networkResponse.status === 200) {
                        const copy = networkResponse.clone();
                        caches.open(CACHE_NAME).then((cache) => cache.put(request, copy));
                    }
                    return networkResponse;
                })
                .catch(async () => {
                    const cachedResponse = await caches.match(request);
                    if (cachedResponse) return cachedResponse;
                    return caches.match(OFFLINE_URL);
                })
        );
        return;
    }

    // 4. Static assets (CSS, JS, Fonts, Images): Cache-First, then Network
    event.respondWith(
        caches.match(request).then((cachedResponse) => {
            if (cachedResponse) {
                // Return cached version immediately and update in background
                fetch(request).then((networkResponse) => {
                    if (networkResponse && networkResponse.status === 200) {
                        caches.open(CACHE_NAME).then((cache) => cache.put(request, networkResponse));
                    }
                }).catch(() => {});
                return cachedResponse;
            }

            return fetch(request).then((networkResponse) => {
                if (networkResponse && networkResponse.status === 200 && request.url.startsWith('http')) {
                    const copy = networkResponse.clone();
                    caches.open(CACHE_NAME).then((cache) => cache.put(request, copy));
                }
                return networkResponse;
            }).catch(() => {
                if (request.destination === 'image') {
                    return caches.match('./assets/img/profile.png');
                }
            });
        })
    );
});
