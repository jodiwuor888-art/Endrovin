package com.portfolio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdminManagementTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminJwtToken;

    @BeforeEach
    void setUp() throws Exception {
        // Provision admin
        FirstAdminRequest firstAdmin = new FirstAdminRequest(
                "superadmin",
                "superadmin@portfolio.local",
                "SuperStrongPass123!",
                "Super Admin"
        );

        MvcResult result = mockMvc.perform(post("/api/auth/setup-first-admin")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(java.util.Objects.requireNonNull(objectMapper.writeValueAsString(firstAdmin))))
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<?> response = objectMapper.readValue(result.getResponse().getContentAsString(), ApiResponse.class);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> data = (java.util.Map<String, Object>) response.getData();
        this.adminJwtToken = java.util.Objects.requireNonNull((String) data.get("token"), "Admin token missing");
    }

    @Test
    @DisplayName("Authorization Protection: Unauthenticated requests to /api/admin are rejected with 401")
    void testUnauthenticatedAccessForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Dashboard Statistics: Reports 0 for empty database and increments genuinely when content added")
    void testDashboardStatsZeroAndIncrement() throws Exception {
        // Step 1: Initial stats must be 0
        mockMvc.perform(get("/api/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + adminJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalProjects").value(0))
                .andExpect(jsonPath("$.data.totalSkills").value(0))
                .andExpect(jsonPath("$.data.totalServices").value(0));

        // Step 2: Add one Project
        ProjectDto newProject = new ProjectDto();
        newProject.setTitle("Student Management System");
        newProject.setCategory("Web Development");
        newProject.setShortDescription("A comprehensive web system for student management");
        newProject.setTechnologies("Java, Spring Boot, SQL Server");

        mockMvc.perform(post("/api/admin/projects")
                        .header("Authorization", "Bearer " + adminJwtToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(java.util.Objects.requireNonNull(objectMapper.writeValueAsString(newProject))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Student Management System"));

        // Step 3: Check stats again - totalProjects must be 1
        mockMvc.perform(get("/api/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + adminJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalProjects").value(1))
                .andExpect(jsonPath("$.data.publishedProjects").value(1));
    }

    @Test
    @DisplayName("Projects CRUD: Create, read, update, and delete project")
    void testProjectLifecycle() throws Exception {
        // Create
        ProjectDto project = new ProjectDto();
        project.setTitle("Library Automation");
        project.setCategory("Java Desktop");
        project.setShortDescription("Desktop app for libraries");
        project.setTechnologies("Java, Swing, JDBC");

        MvcResult createResult = mockMvc.perform(post("/api/admin/projects")
                        .header("Authorization", "Bearer " + adminJwtToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(java.util.Objects.requireNonNull(objectMapper.writeValueAsString(project))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();

        ApiResponse<?> createResp = objectMapper.readValue(createResult.getResponse().getContentAsString(), ApiResponse.class);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> data = (java.util.Map<String, Object>) createResp.getData();
        Long projectId = ((Number) data.get("id")).longValue();

        // Update
        project.setTitle("Library Automation Suite v2");
        mockMvc.perform(put("/api/admin/projects/" + projectId)
                        .header("Authorization", "Bearer " + adminJwtToken)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(java.util.Objects.requireNonNull(objectMapper.writeValueAsString(project))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Library Automation Suite v2"));

        // Delete
        mockMvc.perform(delete("/api/admin/projects/" + projectId)
                        .header("Authorization", "Bearer " + adminJwtToken))
                .andExpect(status().isOk());

        // Verify count is 0
        mockMvc.perform(get("/api/admin/projects")
                        .header("Authorization", "Bearer " + adminJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
