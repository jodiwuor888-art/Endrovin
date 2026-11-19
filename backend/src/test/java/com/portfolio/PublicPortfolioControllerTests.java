package com.portfolio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.dto.ContactMessageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PublicPortfolioControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Empty Production State: Public Portfolio endpoint succeeds with empty database and zero fake records")
    void testPublicPortfolioEmptyState() throws Exception {
        mockMvc.perform(get("/api/public/portfolio")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projects").isArray())
                .andExpect(jsonPath("$.data.projects").isEmpty())
                .andExpect(jsonPath("$.data.skills").isArray())
                .andExpect(jsonPath("$.data.services").isArray())
                .andExpect(jsonPath("$.data.education").isArray())
                .andExpect(jsonPath("$.data.experience").isArray())
                .andExpect(jsonPath("$.data.certificates").isArray())
                .andExpect(jsonPath("$.data.testimonials").isArray());
    }

    @Test
    @DisplayName("Contact Form: Valid message is submitted and persisted")
    void testSubmitValidContactMessage() throws Exception {
        ContactMessageRequest request = new ContactMessageRequest(
                "Jane Recruiter",
                "recruiter@company.com",
                "Job Opportunity: Software Developer",
                "Hello, we were impressed by your portfolio and would love to speak with you."
        );

        mockMvc.perform(post("/api/public/contact")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(Boolean.TRUE))
                .andExpect(jsonPath("$.data.fullName").value("Jane Recruiter"))
                .andExpect(jsonPath("$.data.email").value("recruiter@company.com"))
                .andExpect(jsonPath("$.data.read").value(false));
    }

    @Test
    @DisplayName("Contact Form Validation: Rejects invalid email and short messages")
    void testSubmitInvalidContactMessage() throws Exception {
        ContactMessageRequest request = new ContactMessageRequest(
                "", // empty name
                "invalid-email",
                "Hi", // too short subject
                "Short" // too short message
        );

        mockMvc.perform(post("/api/public/contact")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(Boolean.FALSE));
    }

    @Test
    @DisplayName("Spam Protection: Rejects bots populating honeypot website field")
    void testHoneypotSpamRejection() throws Exception {
        ContactMessageRequest request = new ContactMessageRequest(
                "Spam Bot",
                "bot@spam.com",
                "Buy cheap traffic",
                "Click this malicious link right now to buy traffic."
        );
        request.setWebsite("http://spam-link.com"); // honeypot populated

        mockMvc.perform(post("/api/public/contact")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(Boolean.FALSE))
                .andExpect(result -> assertTrue(
                        result.getResponse().getContentAsString().contains("Spam detected")));
    }
}
