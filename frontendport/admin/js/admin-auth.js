/* ==========================================================================
   ADMIN AUTHENTICATION & SESSION MANAGEMENT
   ========================================================================== */

// WebAuthn W3C specification strictly requires a valid domain (e.g. localhost) rather than an IP address (127.0.0.1)
if (window.location.hostname === '127.0.0.1' || window.location.hostname === '::1') {
    const targetUrl = window.location.href.replace('//127.0.0.1', '//localhost').replace('//[::1]', '//localhost');
    window.location.replace(targetUrl);
}

function checkAuth() {
    const token = localStorage.getItem('adminToken');
    if (!token) {
        window.location.href = 'login.html';
        return null;
    }
    return token;
}

function getAuthHeaders() {
    const token = localStorage.getItem('adminToken');
    return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };
}

function getUploadHeaders() {
    const token = localStorage.getItem('adminToken');
    return {
        'Authorization': `Bearer ${token}`
    };
}

/**
 * Cross-platform device detection for WebAuthn passkey labeling
 */
function getPlatformDeviceInfo() {
    const ua = navigator.userAgent || '';
    if (/iPad|iPhone|iPod/.test(ua) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1)) {
        return {
            name: 'Apple Face ID / Touch ID',
            type: 'Apple iOS/iPadOS Platform Authenticator'
        };
    }
    if (/Macintosh|Mac OS X/.test(ua)) {
        return {
            name: 'Mac Touch ID',
            type: 'macOS Platform Authenticator'
        };
    }
    if (/Android/.test(ua)) {
        return {
            name: 'Android Biometric Passkey',
            type: 'Android Platform Authenticator'
        };
    }
    if (/Windows/.test(ua)) {
        return {
            name: 'Windows Hello / Fingerprint Reader',
            type: 'Windows Platform Authenticator'
        };
    }
    return {
        name: 'Built-in Platform Biometrics',
        type: 'Device Platform Authenticator'
    };
}

function logout() {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminUser');
    sessionStorage.clear();
    window.location.href = 'login.html';
}

/**
 * Terminate admin session when navigating to public portfolio so that returning requires fresh login
 */
function exitAdminToPublic(e) {
    if (e && e.preventDefault) e.preventDefault();
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminUser');
    sessionStorage.clear();
    window.location.href = '../index.html';
}

// Enforce re-authentication when returning via browser back button (bfcache)
window.addEventListener('pageshow', function() {
    const isLoginPage = window.location.pathname.endsWith('login.html');
    if (!isLoginPage) {
        const token = localStorage.getItem('adminToken');
        if (!token) {
            window.location.replace('login.html');
        }
    }
});

document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.public-exit-link, a[href*="../index.html"]').forEach(link => {
        link.addEventListener('click', function(e) {
            exitAdminToPublic(e);
        });
    });
});

// Global fetch wrapper with auto-401 logout
async function adminFetch(endpoint, options = {}) {
    const token = checkAuth();
    if (!token) return null;

    options.headers = options.headers || {};
    options.headers['Authorization'] = `Bearer ${token}`;
    if (!(options.body instanceof FormData)) {
        options.headers['Content-Type'] = options.headers['Content-Type'] || 'application/json';
    }

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, options);
        if (response.status === 401) {
            alert('Your session has expired. Please log in again.');
            logout();
            return null;
        }
        const contentType = response.headers.get('content-type') || '';
        if (!contentType.includes('application/json')) {
            return {
                success: false,
                message: response.ok
                    ? 'The server returned an unexpected response. Check that the backend is running.'
                    : `The server returned HTTP ${response.status}.`
            };
        }

        return await response.json();
    } catch (err) {
        console.error(`API Error on ${endpoint}:`, err);
        const backendUrl = API_BASE || window.location.origin;
        return {
            success: false,
            message: `Cannot reach the backend at ${backendUrl}. Make sure the Spring Boot server is running and try again.`
        };
    }
}


/* ==========================================================================
   WEBAUTHN / FIDO2 BIOMETRIC PASSKEY HELPERS
   ========================================================================== */

function base64urlToUint8Array(base64url) {
    if (!base64url) return new Uint8Array();
    const padding = '='.repeat((4 - (base64url.length % 4)) % 4);
    const base64 = (base64url + padding).replace(/-/g, '+').replace(/_/g, '/');
    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);
    for (let i = 0; i < rawData.length; ++i) {
        outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
}

function uint8ArrayToBase64url(uint8Array) {
    let binary = '';
    const bytes = new Uint8Array(uint8Array);
    const len = bytes.byteLength;
    for (let i = 0; i < len; i++) {
        binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

/**
 * Helper to ensure relying party ID is a valid domain per W3C specification
 */
function normalizeRpId(rpId) {
    const host = window.location.hostname;
    if (!host || host === '127.0.0.1' || host === '::1') {
        return 'localhost';
    }
    // In production (e.g. *.vercel.app), RP ID must match current hostname or be a valid suffix
    if (!rpId || rpId === 'localhost' || rpId === '127.0.0.1' || !host.endsWith(rpId)) {
        return host;
    }
    return rpId;
}

/**
 * Check if the internal laptop platform authenticator (Lenovo Fingerprint / Windows Hello) is available
 */
async function isPlatformFingerprintAvailable() {
    if (!window.PublicKeyCredential) return false;
    if (typeof PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable !== 'function') return false;
    try {
        return await PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable();
    } catch (e) {
        return false;
    }
}

/**
 * Perform Step 2 WebAuthn Assertion (Biometric Fingerprint / Windows Hello)
 * Specifically configured for laptop internal fingerprint sensors (Lenovo, ThinkPad, Windows Hello)
 */
async function triggerPasskeyAuthentication(options, pendingAuthToken, username) {
    if (!window.PublicKeyCredential) {
        throw new Error('WebAuthn biometrics and passkeys are not supported by this browser.');
    }

    if (window.location.hostname === '127.0.0.1' || window.location.hostname === '::1') {
        const targetUrl = window.location.href.replace('//127.0.0.1', '//localhost').replace('//[::1]', '//localhost');
        window.location.replace(targetUrl);
        return;
    }

    // If options not provided or challenge missing, fetch fresh options from server
    if (!options || !options.challenge) {
        const un = username || (options && options.username) || localStorage.getItem('lastAdminUsername') || '';
        if (!un) throw new Error('Administrator username required for biometric authentication.');
        const optRes = await fetch(`${API_BASE}/api/admin/passkey/authentication/options`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: un })
        });
        const optJson = await optRes.json();
        if (!optJson.success || !optJson.data) {
            throw new Error(optJson.message || 'No registered biometric passkey found. Sign in with password first to enroll.');
        }
        options = Object.assign({}, optJson.data, { username: un });
    }

    // Prepare credential request options targeted strictly for local device platform sensor
    const publicKey = {
        challenge: base64urlToUint8Array(options.challenge),
        rpId: normalizeRpId(options.rpId),
        timeout: options.timeout || 60000,
        // 'required' forces physical biometric verification on this local device
        userVerification: 'required',
        // 'client-device' hint instructs browser to prioritize this device's own sensor
        // and NOT prompt cross-device hybrid (QR code / phone connect)
        hints: ['client-device']
    };

    if (options.allowCredentials && options.allowCredentials.length > 0) {
        publicKey.allowCredentials = options.allowCredentials.map(c => ({
            id: base64urlToUint8Array(c.id),
            type: c.type || 'public-key',
            // Enforce ['internal'] only — prevents Windows/Chrome from asking to connect an Android phone or tablet
            transports: ['internal']
        }));
    }

    let assertion;
    try {
        assertion = await navigator.credentials.get({ publicKey });
    } catch (authErr) {
        if (authErr.name === 'SecurityError' || (authErr.message && authErr.message.toLowerCase().includes('invalid domain'))) {
            throw new Error('WebAuthn biometrics requires domain "localhost" instead of IP address. Please open via http://localhost:8080/admin/');
        }
        if (authErr.name === 'NotAllowedError') {
            const devInfo = getPlatformDeviceInfo();
            throw new Error(`Biometric verification was cancelled or timed out. Please verify on your ${devInfo.name} sensor to try again.`);
        }
        throw authErr;
    }

    if (!assertion) {
        throw new Error('Biometric authentication cancelled by user.');
    }

    const verifyPayload = {
        id: assertion.id,
        rawId: uint8ArrayToBase64url(assertion.rawId),
        type: assertion.type,
        pendingAuthToken: pendingAuthToken || null,
        username: username || (options && options.username) || null,
        response: {
            clientDataJSON: uint8ArrayToBase64url(assertion.response.clientDataJSON),
            authenticatorData: uint8ArrayToBase64url(assertion.response.authenticatorData),
            signature: uint8ArrayToBase64url(assertion.response.signature),
            userHandle: assertion.response.userHandle ? uint8ArrayToBase64url(assertion.response.userHandle) : null
        }
    };

    return await API.verifyPasskeyAuth(verifyPayload, pendingAuthToken);
}

// Global alias so either name works seamlessly
const performPasskeyAuthentication = triggerPasskeyAuthentication;

/**
 * Perform WebAuthn Registration to enroll new device / fingerprint
 * Specifically targeting Lenovo laptop built-in fingerprint reader via platform authenticator
 */
async function triggerPasskeyRegistration(deviceName, deviceType, authTokenOverride) {
    if (!window.PublicKeyCredential) {
        throw new Error('WebAuthn biometrics and passkeys are not supported by this browser.');
    }

    if (window.location.hostname === '127.0.0.1' || window.location.hostname === '::1') {
        const targetUrl = window.location.href.replace('//127.0.0.1', '//localhost').replace('//[::1]', '//localhost');
        window.location.replace(targetUrl);
        return;
    }

    const token = authTokenOverride || checkAuth();
    if (!token) throw new Error('You must be signed in as administrator to enroll devices');

    const authHeaders = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };

    // 1. Fetch registration options from server
    let optRes;
    try {
        optRes = await fetch(`${API_BASE}/api/admin/passkey/registration/options`, {
            method: 'POST',
            headers: authHeaders
        });
    } catch (networkErr) {
        throw new Error(
            'Cannot reach the backend server at ' + API_BASE + '. ' +
            'Make sure the Spring Boot backend is running and try again.'
        );
    }

    // Guard: response must be JSON, not an HTML error page
    const contentType = optRes.headers.get('content-type') || '';
    if (!contentType.includes('application/json')) {
        throw new Error(
            'Backend returned an unexpected response (HTTP ' + optRes.status + '). ' +
            'Confirm the Spring Boot server is running and accessible at ' + API_BASE
        );
    }

    const optJson = await optRes.json();
    if (!optJson.success || !optJson.data) {
        throw new Error(optJson.message || 'Failed to retrieve passkey registration options from server.');
    }

    const options = optJson.data;

    // Sanitize RP ID
    const rpObj = Object.assign({}, options.rp || {});
    rpObj.id = normalizeRpId(rpObj.id);

    // 2. Prepare publicKey options explicitly for Lenovo laptop built-in platform authenticator
    const publicKey = {
        challenge: base64urlToUint8Array(options.challenge),
        rp: rpObj,
        user: {
            id: base64urlToUint8Array(options.user.id),
            name: options.user.name,
            displayName: options.user.displayName
        },
        pubKeyCredParams: options.pubKeyCredParams || [
            { type: 'public-key', alg: -7 },   // ES256 (Windows Hello default)
            { type: 'public-key', alg: -257 }  // RS256
        ],
        timeout: options.timeout || 60000,
        attestation: options.attestation || 'none',
        authenticatorSelection: {
            authenticatorAttachment: 'platform', // Strictly uses this device's own internal sensor (laptop or phone)
            userVerification: 'required',        // Requires physical biometric touch on this device
            residentKey: 'preferred'
        },
        hints: ['client-device']
    };

    // 3. Call browser WebAuthn creation (triggers local platform biometric prompt)
    let credential;
    try {
        credential = await navigator.credentials.create({ publicKey });
    } catch (credErr) {
        if (credErr.name === 'SecurityError' || (credErr.message && credErr.message.toLowerCase().includes('invalid domain'))) {
            throw new Error('Biometric security error: Domain must match ' + window.location.hostname);
        }
        if (credErr.name === 'NotAllowedError') {
            throw new Error('Biometric setup was cancelled or timed out. Please try again.');
        }
        throw credErr;
    }

    if (!credential) {
        throw new Error('Passkey enrollment cancelled by user');
    }

    // 4. Send attestation response to server
    const registrationVerifyPayload = {
        id: credential.id,
        rawId: uint8ArrayToBase64url(credential.rawId),
        type: credential.type,
        deviceName: deviceName || 'Lenovo Laptop Fingerprint Reader',
        deviceType: deviceType || 'Laptop / Internal Biometric Sensor',
        response: {
            clientDataJSON: uint8ArrayToBase64url(credential.response.clientDataJSON),
            attestationObject: uint8ArrayToBase64url(credential.response.attestationObject),
            transports: credential.response.getTransports ? credential.response.getTransports() : ['internal']
        }
    };

    const verifyRes = await fetch(`${API_BASE}/api/admin/passkey/registration/verify`, {
        method: 'POST',
        headers: authHeaders,
        body: JSON.stringify(registrationVerifyPayload)
    });

    return await verifyRes.json();
}
