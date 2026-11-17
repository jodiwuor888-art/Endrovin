package com.portfolio.dto;

import java.util.List;
import java.util.Map;

public class PasskeyAuthenticationOptionsResponse {
    private String challenge;
    private String rpId;
    private long timeout = 60000;
    private String userVerification = "required";
    private List<Map<String, Object>> allowCredentials;

    public PasskeyAuthenticationOptionsResponse() {
    }

    public PasskeyAuthenticationOptionsResponse(String challenge, String rpId, List<Map<String, Object>> allowCredentials) {
        this.challenge = challenge;
        this.rpId = rpId;
        this.timeout = 60000;
        this.userVerification = "required";
        this.allowCredentials = allowCredentials;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getRpId() {
        return rpId;
    }

    public void setRpId(String rpId) {
        this.rpId = rpId;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getUserVerification() {
        return userVerification;
    }

    public void setUserVerification(String userVerification) {
        this.userVerification = userVerification;
    }

    public List<Map<String, Object>> getAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(List<Map<String, Object>> allowCredentials) {
        this.allowCredentials = allowCredentials;
    }
}
