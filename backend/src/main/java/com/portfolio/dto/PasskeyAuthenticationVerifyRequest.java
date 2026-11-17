package com.portfolio.dto;

import jakarta.validation.constraints.NotBlank;

public class PasskeyAuthenticationVerifyRequest {

    private String pendingAuthToken;

    private String username;

    @NotBlank(message = "Credential ID is required")
    private String id;

    private String rawId;
    private String type = "public-key";

    private AuthResponseData response;

    public static class AuthResponseData {
        private String clientDataJSON;
        private String authenticatorData;
        private String signature;
        private String userHandle;

        public String getClientDataJSON() {
            return clientDataJSON;
        }

        public void setClientDataJSON(String clientDataJSON) {
            this.clientDataJSON = clientDataJSON;
        }

        public String getAuthenticatorData() {
            return authenticatorData;
        }

        public void setAuthenticatorData(String authenticatorData) {
            this.authenticatorData = authenticatorData;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getUserHandle() {
            return userHandle;
        }

        public void setUserHandle(String userHandle) {
            this.userHandle = userHandle;
        }
    }

    public PasskeyAuthenticationVerifyRequest() {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRawId() {
        return rawId;
    }

    public void setRawId(String rawId) {
        this.rawId = rawId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AuthResponseData getResponse() {
        return response;
    }

    public void setResponse(AuthResponseData response) {
        this.response = response;
    }
}
