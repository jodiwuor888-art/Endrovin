package com.portfolio.dto;

import jakarta.validation.constraints.NotBlank;

public class PasskeyRecoveryVerifyRequest {

    private String pendingAuthToken;

    @NotBlank(message = "Recovery code is required")
    private String recoveryCode;

    private String username;

    public PasskeyRecoveryVerifyRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPendingAuthToken() {
        return pendingAuthToken;
    }

    public void setPendingAuthToken(String pendingAuthToken) {
        this.pendingAuthToken = pendingAuthToken;
    }

    public String getRecoveryCode() {
        return recoveryCode;
    }

    public void setRecoveryCode(String recoveryCode) {
        this.recoveryCode = recoveryCode;
    }
}
