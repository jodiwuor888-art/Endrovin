package com.portfolio.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.portfolio.dto.*;
import com.portfolio.entity.*;
import com.portfolio.exception.BadRequestException;
import com.portfolio.exception.UnauthorizedException;
import com.portfolio.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class WebAuthnService {

    private static final Logger log = LoggerFactory.getLogger(WebAuthnService.class);

    private final AdminPasskeyCredentialRepository credentialRepository;
    private final AdminDeviceRepository deviceRepository;
    private final WebAuthnChallengeRepository challengeRepository;
    private final AdminRecoveryCodeRepository recoveryCodeRepository;
    private final AdminAuthenticationLogRepository authLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${portfolio.security.webauthn.rp-id:localhost}")
    private String rpId;

    @Value("${portfolio.security.webauthn.rp-name:Portfolio Management System}")
    private String rpName;

    @Value("${portfolio.security.webauthn.allowed-origins:http://localhost:8080,https://localhost:8080,http://127.0.0.1:8080}")
    private String allowedOrigins;

    public WebAuthnService(AdminPasskeyCredentialRepository credentialRepository,
                           AdminDeviceRepository deviceRepository,
                           WebAuthnChallengeRepository challengeRepository,
                           AdminRecoveryCodeRepository recoveryCodeRepository,
                           AdminAuthenticationLogRepository authLogRepository,
                           PasswordEncoder passwordEncoder) {
        this.credentialRepository = credentialRepository;
        this.deviceRepository = deviceRepository;
        this.challengeRepository = challengeRepository;
        this.recoveryCodeRepository = recoveryCodeRepository;
        this.authLogRepository = authLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String getEffectiveRpId(String origin) {
        if (origin != null && !origin.isBlank()) {
            try {
                java.net.URI uri = java.net.URI.create(origin);
                String host = uri.getHost();
                if (host != null && !host.isBlank()) {
                    if ("127.0.0.1".equals(host) || "::1".equals(host) || "localhost".equalsIgnoreCase(host)) {
                        return "localhost";
                    }
                    return host;
                }
            } catch (Exception ignored) {}
        }
        return (rpId == null || "127.0.0.1".equals(rpId) || "::1".equals(rpId)) ? "localhost" : rpId;
    }

    /* ==========================================================================
       1. REGISTRATION WORKFLOW
       ========================================================================== */

    @Transactional
    public PasskeyRegistrationOptionsResponse getRegistrationOptions(Admin admin, String origin) {
        String effectiveRp = getEffectiveRpId(origin);
        String challenge = generateChallenge(admin.getAdminId(), "REGISTRATION");

        Map<String, String> rp = new HashMap<>();
        rp.put("id", effectiveRp);
        rp.put("name", rpName);

        Map<String, Object> user = new HashMap<>();
        user.put("id", Base64.getUrlEncoder().withoutPadding().encodeToString(admin.getUsername().getBytes(StandardCharsets.UTF_8)));
        user.put("name", admin.getUsername());
        user.put("displayName", admin.getFullName() != null ? admin.getFullName() : admin.getUsername());

        List<Map<String, Object>> pubKeyCredParams = new ArrayList<>();
        // ES256 (ECDSA P-256) - standard for Windows Hello, Android Biometrics, iOS TouchID/FaceID
        Map<String, Object> es256 = new HashMap<>();
        es256.put("type", "public-key");
        es256.put("alg", -7);
        pubKeyCredParams.add(es256);

        // RS256 (RSA with SHA-256)
        Map<String, Object> rs256 = new HashMap<>();
        rs256.put("type", "public-key");
        rs256.put("alg", -257);
        pubKeyCredParams.add(rs256);

        Map<String, Object> authenticatorSelection = new HashMap<>();
        authenticatorSelection.put("authenticatorAttachment", "platform"); // Enforces device biometric platform authenticator
        authenticatorSelection.put("userVerification", "required"); // Enforces fingerprint / biometric prompt
        authenticatorSelection.put("residentKey", "preferred");

        return new PasskeyRegistrationOptionsResponse(rp, user, challenge, pubKeyCredParams, authenticatorSelection);
    }

    @Transactional
    public RecoveryCodesResponse verifyRegistration(Admin admin, PasskeyRegistrationVerifyRequest request, String origin, String ip) {
        try {
            PasskeyRegistrationVerifyRequest.ResponseData resp = request.getResponse();
            if (resp == null || resp.getClientDataJSON() == null || resp.getAttestationObject() == null) {
                throw new BadRequestException("Missing clientDataJSON or attestationObject in registration response");
            }

            // 1. Verify clientDataJSON
            byte[] clientDataBytes = Base64.getUrlDecoder().decode(resp.getClientDataJSON());
            JsonNode clientDataJson = jsonMapper.readTree(clientDataBytes);

            String type = clientDataJson.path("type").asText();
            if (!"webauthn.create".equals(type)) {
                throw new BadRequestException("Invalid WebAuthn clientData type: " + type);
            }

            String clientOrigin = clientDataJson.path("origin").asText();
            verifyClientDataOrigin(clientOrigin, origin);

            String challenge = clientDataJson.path("challenge").asText();
            verifyChallenge(challenge, "REGISTRATION", admin.getAdminId());

            // 2. Parse attestationObject (CBOR)
            byte[] attestationBytes = Base64.getUrlDecoder().decode(resp.getAttestationObject());
            JsonNode attestationNode = cborMapper.readTree(attestationBytes);

            byte[] authData = attestationNode.path("authData").binaryValue();
            if (authData == null || authData.length < 37) {
                throw new BadRequestException("Invalid authData in attestationObject");
            }

            // Parse AuthData flags
            int flags = authData[32] & 0xFF;
            boolean userPresent = (flags & 0x01) != 0;
            boolean userVerified = (flags & 0x04) != 0;
            boolean attestedCredentialDataPresent = (flags & 0x40) != 0;

            if (!userPresent) {
                throw new BadRequestException("User was not present during biometric enrollment");
            }
            if (!userVerified) {
                throw new BadRequestException("User verification (biometric/PIN) was not confirmed");
            }
            if (!attestedCredentialDataPresent) {
                throw new BadRequestException("Attested credential data missing from authenticator");
            }

            // Extract Credential ID and COSE Public Key from authData
            ByteBuffer buffer = ByteBuffer.wrap(authData);
            buffer.position(37); // skip 32 bytes rpIdHash, 1 byte flags, 4 bytes signCount

            byte[] aaguid = new byte[16];
            buffer.get(aaguid);

            int credIdLength = buffer.getShort() & 0xFFFF;
            byte[] credIdBytes = new byte[credIdLength];
            buffer.get(credIdBytes);
            String credentialId = Base64.getUrlEncoder().withoutPadding().encodeToString(credIdBytes);

            // Remaining bytes in buffer are COSE Key (CBOR)
            byte[] coseKeyBytes = new byte[buffer.remaining()];
            buffer.get(coseKeyBytes);

            PublicKey publicKey = parseCosePublicKey(coseKeyBytes);
            String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            // 3. Save Credential
            String transports = resp.getTransports() != null ? String.join(",", resp.getTransports()) : "internal";
            AdminPasskeyCredential credential = new AdminPasskeyCredential(credentialId, admin, encodedPublicKey, 0, transports);
            credentialRepository.save(credential);

            // 4. Save Device
            AdminDevice device = new AdminDevice(admin, credential, request.getDeviceName(), request.getDeviceType());
            deviceRepository.save(device);

            // 5. Generate Recovery Codes if first device
            List<String> rawRecoveryCodes = null;
            if (recoveryCodeRepository.countByAdminAndUsedAtIsNull(admin) == 0) {
                rawRecoveryCodes = generateRecoveryCodes(admin);
            }

            // 6. Record authentication log
            authLogRepository.save(new AdminAuthenticationLog(
                    admin.getAdminId(), "WEBAUTHN_REGISTRATION",
                    request.getDeviceName() + " (" + request.getDeviceType() + ")",
                    ip, "SUCCESS"
            ));

            log.info("Successfully enrolled biometric passkey [{}] for admin: {}", request.getDeviceName(), admin.getUsername());
            return new RecoveryCodesResponse(rawRecoveryCodes);

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to verify WebAuthn registration: {}", e.getMessage(), e);
            throw new BadRequestException("Passkey verification failed: " + e.getMessage());
        }
    }

    /* ==========================================================================
       2. AUTHENTICATION WORKFLOW
       ========================================================================== */

    @Transactional
    public PasskeyAuthenticationOptionsResponse getAuthenticationOptions(Admin admin, String origin) {
        String effectiveRp = getEffectiveRpId(origin);
        String challenge = generateChallenge(admin.getAdminId(), "AUTHENTICATION");

        List<AdminPasskeyCredential> creds = credentialRepository.findByAdminAndRevokedAtIsNull(admin);
        if (creds.isEmpty()) {
            throw new BadRequestException("No registered passkey credentials found for administrator");
        }

        List<Map<String, Object>> allowCredentials = new ArrayList<>();
        for (AdminPasskeyCredential cred : creds) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "public-key");
            item.put("id", cred.getCredentialId());
            if (cred.getCredentialTransports() != null) {
                item.put("transports", Arrays.asList(cred.getCredentialTransports().split(",")));
            }
            allowCredentials.add(item);
        }

        return new PasskeyAuthenticationOptionsResponse(challenge, effectiveRp, allowCredentials);
    }

    @Transactional
    public void verifyAuthentication(Admin admin, PasskeyAuthenticationVerifyRequest request, String origin, String ip) {
        try {
            PasskeyAuthenticationVerifyRequest.AuthResponseData resp = request.getResponse();
            if (resp == null || resp.getClientDataJSON() == null || resp.getAuthenticatorData() == null || resp.getSignature() == null) {
                throw new BadRequestException("Incomplete authentication assertion response from device");
            }

            // 1. Verify clientDataJSON
            byte[] clientDataBytes = Base64.getUrlDecoder().decode(resp.getClientDataJSON());
            JsonNode clientDataJson = jsonMapper.readTree(clientDataBytes);

            String type = clientDataJson.path("type").asText();
            if (!"webauthn.get".equals(type)) {
                throw new BadRequestException("Invalid clientData type for authentication: " + type);
            }

            String clientOrigin = clientDataJson.path("origin").asText();
            verifyClientDataOrigin(clientOrigin, origin);

            String challenge = clientDataJson.path("challenge").asText();
            verifyChallenge(challenge, "AUTHENTICATION", admin.getAdminId());

            // 2. Fetch registered credential
            AdminPasskeyCredential credential = credentialRepository.findByCredentialIdAndRevokedAtIsNull(request.getId())
                    .orElseThrow(() -> new UnauthorizedException("Unrecognized or revoked passkey credential"));

            if (!credential.getAdmin().getAdminId().equals(admin.getAdminId())) {
                throw new UnauthorizedException("Credential does not belong to the authenticating administrator");
            }

            // 3. Verify authenticatorData
            byte[] authData = Base64.getUrlDecoder().decode(resp.getAuthenticatorData());
            int flags = authData[32] & 0xFF;
            boolean userPresent = (flags & 0x01) != 0;
            boolean userVerified = (flags & 0x04) != 0;

            if (!userPresent) {
                throw new UnauthorizedException("User presence flag not set by biometric authenticator");
            }
            if (!userVerified) {
                throw new UnauthorizedException("Biometric verification (fingerprint / Windows Hello) was not completed");
            }

            // 4. Verify cryptographic signature
            byte[] clientDataHash = MessageDigest.getInstance("SHA-256").digest(clientDataBytes);
            byte[] signedData = new byte[authData.length + clientDataHash.length];
            System.arraycopy(authData, 0, signedData, 0, authData.length);
            System.arraycopy(clientDataHash, 0, signedData, authData.length, clientDataHash.length);

            byte[] signatureBytes = Base64.getUrlDecoder().decode(resp.getSignature());
            byte[] publicKeyBytes = Base64.getDecoder().decode(credential.getPublicKey());

            boolean valid = verifySignature(publicKeyBytes, signedData, signatureBytes);
            if (!valid) {
                authLogRepository.save(new AdminAuthenticationLog(admin.getAdminId(), "WEBAUTHN_BIOMETRIC", "Unknown Device", ip, "FAILED"));
                throw new UnauthorizedException("Cryptographic signature verification failed");
            }

            // 5. Update signCount and lastUsedAt
            ByteBuffer countBuffer = ByteBuffer.wrap(authData, 33, 4);
            long signCount = countBuffer.getInt() & 0xFFFFFFFFL;
            if (signCount > credential.getSignCount()) {
                credential.setSignCount(signCount);
            }
            credential.setLastUsedAt(LocalDateTime.now());
            credentialRepository.save(credential);

            deviceRepository.findByCredential_CredentialId(credential.getCredentialId()).ifPresent(dev -> {
                dev.setLastUsedAt(LocalDateTime.now());
                deviceRepository.save(dev);
            });

            // 6. Record successful log
            authLogRepository.save(new AdminAuthenticationLog(admin.getAdminId(), "WEBAUTHN_BIOMETRIC", "Device: " + credential.getCredentialId().substring(0, Math.min(8, credential.getCredentialId().length())), ip, "SUCCESS"));
            log.info("Biometric passkey authentication succeeded for admin: {}", admin.getUsername());

        } catch (UnauthorizedException | BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Authentication assertion failed: {}", e.getMessage(), e);
            throw new UnauthorizedException("Passkey verification error: " + e.getMessage());
        }
    }

    /* ==========================================================================
       3. RECOVERY CODES
       ========================================================================== */

    @Transactional
    public List<String> generateRecoveryCodes(Admin admin) {
        recoveryCodeRepository.deleteByAdmin(admin);

        List<String> rawCodes = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            String code = generateReadableCode();
            rawCodes.add(code);
            String hashed = passwordEncoder.encode(code);
            recoveryCodeRepository.save(new AdminRecoveryCode(admin, hashed));
        }
        return rawCodes;
    }

    @Transactional
    public void verifyRecoveryCode(Admin admin, String code, String ip) {
        List<AdminRecoveryCode> activeCodes = recoveryCodeRepository.findByAdminAndUsedAtIsNull(admin);
        for (AdminRecoveryCode rc : activeCodes) {
            if (passwordEncoder.matches(code.trim().toUpperCase(), rc.getHashedRecoveryCode())) {
                rc.setUsedAt(LocalDateTime.now());
                recoveryCodeRepository.save(rc);

                authLogRepository.save(new AdminAuthenticationLog(
                        admin.getAdminId(), "RECOVERY_CODE", "Emergency Recovery Used", ip, "SUCCESS"
                ));
                log.warn("Emergency recovery code used for admin [{}] from IP: {}", admin.getUsername(), ip);
                return;
            }
        }
        authLogRepository.save(new AdminAuthenticationLog(admin.getAdminId(), "RECOVERY_CODE", "Invalid Code Attempt", ip, "FAILED"));
        throw new UnauthorizedException("Invalid or already used recovery code");
    }

    private String generateReadableCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i == 4) sb.append("-");
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /* ==========================================================================
       4. CHALLENGE MANAGEMENT
       ========================================================================== */

    private String generateChallenge(Long adminId, String type) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        challengeRepository.save(new WebAuthnChallenge(adminId, challenge, type, 300)); // 5 min expiry
        return challenge;
    }

    private void verifyChallenge(String challengeValue, String expectedType, Long expectedAdminId) {
        WebAuthnChallenge challenge = challengeRepository.findByChallengeValueAndUsedAtIsNullAndExpiresAtAfter(challengeValue, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Challenge has expired, been reused, or is invalid"));

        if (!expectedType.equals(challenge.getChallengeType())) {
            throw new BadRequestException("Challenge type mismatch");
        }
        if (expectedAdminId != null && challenge.getAdminId() != null && !expectedAdminId.equals(challenge.getAdminId())) {
            throw new BadRequestException("Challenge does not belong to current user session");
        }

        challenge.setUsedAt(LocalDateTime.now());
        challengeRepository.save(challenge);
    }

    private void verifyClientDataOrigin(String clientDataOrigin, String requestOrigin) {
        if (clientDataOrigin == null || clientDataOrigin.isBlank()) {
            throw new BadRequestException("Missing origin in WebAuthn clientData");
        }
        String normalizedClient = clientDataOrigin.trim().toLowerCase();

        // Local development environments always permitted
        if (normalizedClient.startsWith("http://localhost") || normalizedClient.startsWith("https://localhost")
                || normalizedClient.startsWith("http://127.0.0.1") || normalizedClient.startsWith("https://127.0.0.1")) {
            return;
        }

        // Check against configured allowed-origins list (supports wildcard suffix matching via *)
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            for (String allowed : allowedOrigins.split(",")) {
                String pattern = allowed.trim().toLowerCase();
                if (pattern.isEmpty()) continue;
                if (pattern.endsWith("*")) {
                    // Wildcard prefix match: e.g. "https://*.vercel.app" or "https://*.onrender.com"
                    String prefix = pattern.substring(0, pattern.length() - 1);
                    if (normalizedClient.startsWith(prefix)) return;
                } else if (normalizedClient.equals(pattern)) {
                    return;
                }
            }
        }

        // Exact match with the HTTP request Origin header as a final fallback
        if (requestOrigin != null && !requestOrigin.isBlank()
                && normalizedClient.equalsIgnoreCase(requestOrigin.trim())) {
            return;
        }

        throw new UnauthorizedException("WebAuthn origin unauthorized: " + clientDataOrigin);
    }

    /* ==========================================================================
       5. CRYPTOGRAPHIC HELPERS
       ========================================================================== */

    private PublicKey parseCosePublicKey(byte[] coseKeyBytes) throws Exception {
        JsonNode coseMap = cborMapper.readTree(coseKeyBytes);

        int kty = coseMap.path("1").asInt(); // 2 = EC2, 3 = RSA
        int alg = coseMap.path("3").asInt(); // -7 = ES256, -257 = RS256

        if (kty == 2) { // EC2 P-256
            byte[] x = coseMap.path("-2").binaryValue();
            byte[] y = coseMap.path("-3").binaryValue();

            ECPoint point = new ECPoint(new BigInteger(1, x), new BigInteger(1, y));
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(new ECGenParameterSpec("secp256r1"));
            ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);

            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePublic(new ECPublicKeySpec(point, ecParameters));
        } else if (kty == 3) { // RSA
            byte[] n = coseMap.path("-1").binaryValue();
            byte[] e = coseMap.path("-2").binaryValue();

            RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(1, n), new BigInteger(1, e));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        }

        throw new BadRequestException("Unsupported COSE key type: " + kty + ", algorithm: " + alg);
    }

    private boolean verifySignature(byte[] x509PublicKeyBytes, byte[] data, byte[] signatureBytes) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(x509PublicKeyBytes);
            PublicKey publicKey;
            try {
                publicKey = KeyFactory.getInstance("EC").generatePublic(keySpec);
                Signature sig = Signature.getInstance("SHA256withECDSA");
                sig.initVerify(publicKey);
                sig.update(data);
                return sig.verify(signatureBytes);
            } catch (Exception ecEx) {
                publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);
                Signature sig = Signature.getInstance("SHA256withRSA");
                sig.initVerify(publicKey);
                sig.update(data);
                return sig.verify(signatureBytes);
            }
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    /* ==========================================================================
       6. DEVICE MANAGEMENT & REVOCATION
       ========================================================================== */

    @Transactional(readOnly = true)
    public List<AdminDeviceDto> getAdminDevices(Admin admin) {
        List<AdminDevice> devices = deviceRepository.findByAdminAndRevokedAtIsNullOrderByRegisteredAtDesc(admin);
        List<AdminDeviceDto> dtos = new ArrayList<>();
        for (AdminDevice d : devices) {
            String credId = d.getCredential() != null ? d.getCredential().getCredentialId() : "";
            dtos.add(new AdminDeviceDto(
                    d.getDeviceId(),
                    credId,
                    d.getDeviceName(),
                    d.getDeviceType(),
                    d.getRegisteredAt(),
                    d.getLastUsedAt(),
                    false
            ));
        }
        return dtos;
    }

    @Transactional
    public void revokePasskey(Admin admin, String credentialId, String ip) {
        AdminPasskeyCredential credential = credentialRepository.findByCredentialIdAndRevokedAtIsNull(credentialId)
                .orElseThrow(() -> new BadRequestException("Passkey credential not found or already revoked"));

        if (!credential.getAdmin().getAdminId().equals(admin.getAdminId())) {
            throw new UnauthorizedException("Cannot revoke passkey belonging to another administrator");
        }

        credential.setRevokedAt(LocalDateTime.now());
        credentialRepository.save(credential);

        deviceRepository.findByCredential_CredentialId(credentialId).ifPresent(dev -> {
            dev.setRevokedAt(LocalDateTime.now());
            deviceRepository.save(dev);
        });

        authLogRepository.save(new AdminAuthenticationLog(
                admin.getAdminId(), "WEBAUTHN_REVOCATION", "Revoked: " + credentialId.substring(0, Math.min(8, credentialId.length())),
                ip, "SUCCESS"
        ));
        log.info("Administrator [{}] revoked passkey credential [{}]", admin.getUsername(), credentialId);
    }

    @Transactional(readOnly = true)
    public long getRemainingRecoveryCodesCount(Admin admin) {
        return recoveryCodeRepository.countByAdminAndUsedAtIsNull(admin);
    }
}
