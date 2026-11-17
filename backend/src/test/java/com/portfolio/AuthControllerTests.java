package com.portfolio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.dto.AuthRequest;
import com.portfolio.dto.FirstAdminRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import org.mockito.ArgumentCaptor;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    @DisplayName("Admin Provisioning: Allows one-time setup of first admin, then permanently locks")
    void testFirstAdminProvisioningAndLockout() throws Exception {
        // Step 1: Check status - setup is needed
        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.setupNeeded").value(true));

        // Step 2: Create first admin
        FirstAdminRequest firstAdmin = new FirstAdminRequest(
                "masteradmin",
                "admin@portfoliodev.local",
                "SuperSecretPassword123!",
                "Master Administrator"
        );

        mockMvc.perform(post("/api/auth/setup-first-admin")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(firstAdmin))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("masteradmin"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());

        // Step 3: Check status again - setup is NOT needed anymore
        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.setupNeeded").value(false));

        // Step 4: Second attempt to setup first admin MUST be blocked (400 Bad Request)
        FirstAdminRequest duplicateAttempt = new FirstAdminRequest(
                "attacker",
                "attacker@evil.com",
                "AttackerPass123!",
                "Malicious User"
        );

        mockMvc.perform(post("/api/auth/setup-first-admin")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(duplicateAttempt))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(result -> assertTrue(objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .path("message").asText().contains("permanently locked")));
    }

    @Test
    @DisplayName("Authentication: Login with valid credentials succeeds and invalid fails")
    void testLoginSuccessAndFailure() throws Exception {
        // Provision first admin
        FirstAdminRequest firstAdmin = new FirstAdminRequest(
                "testadmin",
                "testadmin@portfolio.local",
                "StrongPassword123!",
                "Test Admin"
        );

        mockMvc.perform(post("/api/auth/setup-first-admin")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(firstAdmin))))
                .andExpect(status().isOk());

        // Valid login
        AuthRequest validAuth = new AuthRequest("testadmin", "StrongPassword123!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(validAuth))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("testadmin"));

        // Invalid password
        AuthRequest invalidAuth = new AuthRequest("testadmin", "WrongPassword!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(invalidAuth))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Password recovery: emails username and a single-use reset link without revealing account existence")
    @SuppressWarnings("null")
    void testEmailPasswordRecoveryAndSingleUseToken() throws Exception {
        FirstAdminRequest firstAdmin = new FirstAdminRequest(
                "recoveryadmin",
                "recovery@example.com",
                "OldPassword123!",
                "Recovery Admin"
        );
        mockMvc.perform(post("/api/auth/setup-first-admin")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(firstAdmin))))
                .andExpect(status().isOk());

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        String recoveryMessage = mockMvc.perform(post("/api/auth/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(toJson(Map.of("email", "missing@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(
                        "If an administrator account matches that email, recovery instructions will be sent."))
                .andReturn().getResponse().getContentAsString();
        String genericMessage = objectMapper.readTree(recoveryMessage).path("message").asText();

        ArgumentCaptor<SimpleMailMessage> emailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        mockMvc.perform(post("/api/auth/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(toJson(Map.of("email", "RECOVERY@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(genericMessage));
        verify(mailSender, atLeastOnce()).send(emailCaptor.capture());

        SimpleMailMessage recoveryEmail = emailCaptor.getValue();
        String emailText = Objects.requireNonNull(recoveryEmail.getText());
        assertTrue(emailText.contains("Username: recoveryadmin"));
        Matcher tokenMatcher = Pattern.compile("#resetToken=([A-Za-z0-9_-]+)")
                .matcher(emailText);
        assertTrue(tokenMatcher.find(), "Reset email should contain a URL-fragment token");
        String token = tokenMatcher.group(1);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(toJson(Map.of(
                                "token", token,
                                "newPassword", "NewPassword456!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(toJson(Map.of(
                                "token", token,
                                "newPassword", "AnotherPassword789!"
                        ))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(toJson(new AuthRequest(
                                "recoveryadmin", "NewPassword456!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("recoveryadmin"));
    }

    private String toJson(Object value) throws Exception {
        return Objects.requireNonNull(objectMapper.writeValueAsString(value));
    }
}
