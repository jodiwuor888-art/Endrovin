package com.portfolio.dto;

import java.util.List;
import java.util.Map;

public class PasskeyRegistrationOptionsResponse {
    private Map<String, String> rp;
    private Map<String, Object> user;
    private String challenge;
    private List<Map<String, Object>> pubKeyCredParams;
    private Map<String, Object> authenticatorSelection;
    private long timeout = 60000;
    private String attestation = "none";

    public PasskeyRegistrationOptionsResponse() {
    }

    public PasskeyRegistrationOptionsResponse(Map<String, String> rp, Map<String, Object> user,
                                              String challenge, List<Map<String, Object>> pubKeyCredParams,
                                              Map<String, Object> authenticatorSelection) {
        this.rp = rp;
        this.user = user;
        this.challenge = challenge;
        this.pubKeyCredParams = pubKeyCredParams;
        this.authenticatorSelection = authenticatorSelection;
        this.timeout = 60000;
        this.attestation = "none";
    }

    public Map<String, String> getRp() {
        return rp;
    }

    public void setRp(Map<String, String> rp) {
        this.rp = rp;
    }

    public Map<String, Object> getUser() {
        return user;
    }

    public void setUser(Map<String, Object> user) {
        this.user = user;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public List<Map<String, Object>> getPubKeyCredParams() {
        return pubKeyCredParams;
    }

    public void setPubKeyCredParams(List<Map<String, Object>> pubKeyCredParams) {
        this.pubKeyCredParams = pubKeyCredParams;
    }

    public Map<String, Object> getAuthenticatorSelection() {
        return authenticatorSelection;
    }

    public void setAuthenticatorSelection(Map<String, Object> authenticatorSelection) {
        this.authenticatorSelection = authenticatorSelection;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getAttestation() {
        return attestation;
    }

    public void setAttestation(String attestation) {
        this.attestation = attestation;
    }
}
