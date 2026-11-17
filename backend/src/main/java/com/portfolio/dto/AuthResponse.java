package com.portfolio.dto;

import java.util.Set;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;

    // Two-factor authentication properties
    private boolean mfaRequired = false;
    private String pendingAuthToken;
    private PasskeyAuthenticationOptionsResponse passkeyOptions;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long id, String username, String email, String fullName, Set<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
        this.mfaRequired = false;
    }

    public static AuthResponse mfaRequired(String pendingAuthToken, Long id, String username, String email, PasskeyAuthenticationOptionsResponse passkeyOptions) {
        AuthResponse resp = new AuthResponse();
        resp.setMfaRequired(true);
        resp.setPendingAuthToken(pendingAuthToken);
        resp.setId(id);
        resp.setUsername(username);
        resp.setEmail(email);
        resp.setPasskeyOptions(passkeyOptions);
        return resp;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public boolean isMfaRequired() {
        return mfaRequired;
    }

    public void setMfaRequired(boolean mfaRequired) {
        this.mfaRequired = mfaRequired;
    }

    public String getPendingAuthToken() {
        return pendingAuthToken;
    }

    public void setPendingAuthToken(String pendingAuthToken) {
        this.pendingAuthToken = pendingAuthToken;
    }

    public PasskeyAuthenticationOptionsResponse getPasskeyOptions() {
        return passkeyOptions;
    }

    public void setPasskeyOptions(PasskeyAuthenticationOptionsResponse passkeyOptions) {
        this.passkeyOptions = passkeyOptions;
    }
}
