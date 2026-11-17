package com.portfolio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "A valid email address is required")
    @Size(max = 120, message = "Email must not exceed 120 characters")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
