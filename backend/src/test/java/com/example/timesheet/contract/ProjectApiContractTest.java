package com.example.timesheet.contract;

import com.example.timesheet.domain.entity.Department;
import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.DepartmentRepository;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.request.CreateProjectRequest;
import com.example.timesheet.dto.request.UpdateProjectRequest;
import com.example.timesheet.dto.response.ProjectDetailResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contract tests for Project API endpoints (T051-T053).
 * Tests verify that API contracts match the OpenAPI specification.
 * Written FIRST and should FAIL before implementation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Project API Contract Tests")
@Transactional
class ProjectApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User manager;
    private User pm;
    private Department department;

    @BeforeEach
    void setup() {
        department = departmentRepository.save(Department.builder()
            .name("Research & Development")
            .build());

        manager = userRepository.save(User.builder()
            .name("Manager Zhang")
            .email("manager@example.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(UserRole.MANAGER)
            .department(department)
            .active(true)
            .build());

        pm = userRepository.save(User.builder()
            .name("PM Li")
            .email("pm@example.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(UserRole.PM)
            .department(department)
            .active(true)
            .build());
    }

    /**
     * T051: Contract test for POST /api/projects
     * Manager creates a new project
     */
    @Test
    @DisplayName("T051: POST /api/projects should create project and return 201 CREATED")
    void testCreateProject() throws Exception {
        // Arrange
        CreateProjectRequest request = CreateProjectRequest.builder()
            .name("E-Commerce Platform Revamp")
            .description("Complete redesign of e-commerce platform")
            .totalHours(500)
            .pmId(pm.getId())
            .build();

        // Act
        MvcResult result = mockMvc.perform(post("/api/projects")
            .with(user("manager@example.com").roles("MANAGER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value("E-Commerce Platform Revamp"))
            .andExpect(jsonPath("$.description").value("Complete redesign of e-commerce platform"))
            .andExpect(jsonPath("$.totalHours").value(500))
            .andExpect(jsonPath("$.allocatedHours").value(0))
            .andExpect(jsonPath("$.remainingHours").value(500))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.pmId").value(pm.getId().intValue()))
            .andReturn();

        // Assert
        String responseBody = result.getResponse().getContentAsString();
        ProjectDetailResponse response = objectMapper.readValue(responseBody, ProjectDetailResponse.class);
        assertThat(response)
            .isNotNull()
            .extracting("name", "status", "totalHours")
            .containsExactly("E-Commerce Platform Revamp", "ACTIVE", 500);
    }

    /**
     * T051: Verify non-manager cannot create project
     */
    @Test
    @DisplayName("T051: Non-manager should not be able to create project (403 Forbidden)")
    void testCreateProjectForbidden() throws Exception {
        CreateProjectRequest request = CreateProjectRequest.builder()
            .name("Unauthorized Project")
            .totalHours(100)
            .pmId(pm.getId())
            .build();

        mockMvc.perform(post("/api/projects")
            .with(user("pm@example.com").roles("PM"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    /**
     * T051: Verify missing required fields return 400 Bad Request
     */
    @Test
    @DisplayName("T051: Create project with missing fields should return 400 Bad Request")
    void testCreateProjectBadRequest() throws Exception {
        CreateProjectRequest request = CreateProjectRequest.builder()
            .name("") // Invalid: empty name
            .totalHours(100)
            .build();

        mockMvc.perform(post("/api/projects")
            .with(user("manager@example.com").roles("MANAGER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    /**
     * T052: Contract test for PUT /api/projects/{projectId}
     * Manager updates an existing project
     */
    @Test
    @DisplayName("T052: PUT /api/projects/{projectId} should update project and return 200 OK")
    void testUpdateProject() throws Exception {
        // First, create a project
        CreateProjectRequest createRequest = CreateProjectRequest.builder()
            .name("Original Project Name")
            .description("Original description")
            .totalHours(500)
            .pmId(pm.getId())
            .build();

        MvcResult createResult = mockMvc.perform(post("/api/projects")
            .with(user("manager@example.com").roles("MANAGER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ProjectDetailResponse createdProject = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            ProjectDetailResponse.class);

        // Now update it
        UpdateProjectRequest updateRequest = UpdateProjectRequest.builder()
            .name("Updated Project Name")
            .description("Updated description")
            .totalHours(600)
            .version(0)
            .build();

        mockMvc.perform(put("/api/projects/" + createdProject.getId())
            .with(user("manager@example.com").roles("MANAGER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Project Name"))
            .andExpect(jsonPath("$.description").value("Updated description"))
            .andExpect(jsonPath("$.totalHours").value(600));
    }

    /**
     * T052: Verify optimistic locking prevents concurrent updates
     */
    @Test
    @DisplayName("T052: Update with wrong version should return 409 Conflict")
    void testUpdateProjectConflict() throws Exception {
        // Create a project
        CreateProjectRequest createRequest = CreateProjectRequest.builder()
            .name("Test Project")
            .totalHours(500)
            .pmId(pm.getId())
            .build();

        MvcResult createResult = mockMvc.perform(post("/api/projects")
            .with(user("manager@example.com").roles("MANAGER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ProjectDetailResponse createdProject = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            ProjectDetailResponse.class);

        // Try to update with wrong version
        UpdateProjectRequest updateRequest = UpdateProjectRequest.builder()
            .name("Updated Name")
            .totalHours(600)
            .version(999) // Wrong version
            .build();

        mockMvc.perform(put("/api/projects/" + createdProject.getId())
            .with(user("manager@example.com").roles("MANAGER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isConflict());
    }

    /**
     * T053: Contract test for POST /api/projects/{projectId}/close
     * Manager closes a project
     */
    @Test
    @DisplayName("T053: POST /api/projects/{projectId}/close should close project and return 200 OK")
    void testCloseProject() throws Exception {
        // Create a project
        CreateProjectRequest createRequest = CreateProjectRequest.builder()
            .name("Project to Close")
            .totalHours(500)
            .pmId(pm.getId())
            .build();

        MvcResult createResult = mockMvc.perform(post("/api/projects")
            .with(user("manager@example.com").roles("MANAGER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ProjectDetailResponse createdProject = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            ProjectDetailResponse.class);

        // Close it
        mockMvc.perform(post("/api/projects/" + createdProject.getId() + "/close")
            .with(user("manager@example.com").roles("MANAGER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    /**
     * T053: Verify cannot close already closed project
     */
    @Test
    @DisplayName("T053: Close already closed project should return 400 Bad Request")
    void testCloseProjectTwice() throws Exception {
        // Create and close a project
        CreateProjectRequest createRequest = CreateProjectRequest.builder()
            .name("Project to Close Twice")
            .totalHours(500)
            .pmId(pm.getId())
            .build();

        MvcResult createResult = mockMvc.perform(post("/api/projects")
            .with(user("manager@example.com").roles("MANAGER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ProjectDetailResponse createdProject = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            ProjectDetailResponse.class);

        // Close first time
        mockMvc.perform(post("/api/projects/" + createdProject.getId() + "/close")
            .with(user("manager@example.com").roles("MANAGER")))
            .andExpect(status().isOk());

        // Try to close second time - should fail
        mockMvc.perform(post("/api/projects/" + createdProject.getId() + "/close")
            .with(user("manager@example.com").roles("MANAGER")))
            .andExpect(status().isBadRequest());
    }

    /**
     * Verify non-existent project returns 404
     */
    @Test
    @DisplayName("GET /api/projects/999999 should return 404 Not Found")
    void testGetNonExistentProject() throws Exception {
        mockMvc.perform(get("/api/projects/999999")
            .with(user("manager@example.com").roles("MANAGER")))
            .andExpect(status().isNotFound());
    }
}
