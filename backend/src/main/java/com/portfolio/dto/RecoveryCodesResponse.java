package com.portfolio.dto;

import java.util.List;

public class RecoveryCodesResponse {
    private List<String> recoveryCodes;
    private String message = "Please store these single-use recovery codes in a secure location. They will not be shown again.";

    public RecoveryCodesResponse() {
    }

    public RecoveryCodesResponse(List<String> recoveryCodes) {
        this.recoveryCodes = recoveryCodes;
    }

    public List<String> getRecoveryCodes() {
        return recoveryCodes;
    }

    public void setRecoveryCodes(List<String> recoveryCodes) {
        this.recoveryCodes = recoveryCodes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
