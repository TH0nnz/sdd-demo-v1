package com.example.timesheet.contract;

import com.example.timesheet.domain.entity.Department;
import com.example.timesheet.domain.entity.Project;
import com.example.timesheet.domain.entity.Task;
import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.enums.ProjectStatus;
import com.example.timesheet.domain.enums.TaskStatus;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.*;
import com.example.timesheet.dto.request.CreateTimesheetRequest;
import com.example.timesheet.dto.request.UpdateTimesheetRequest;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contract tests for Timesheet API endpoints (T111-T113).
 * Tests verify that API contracts match the OpenAPI specification.
 * Tests are written FIRST and should FAIL before implementation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Timesheet API Contract Tests")
@Transactional
class TimesheetApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User executiveUser;
    private Department department;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        // Create test department
        department = departmentRepository.save(Department.builder()
            .name("Research & Development")
            .build());

        // Create executive user
        executiveUser = userRepository.save(User.builder()
            .name("Executive-Zhang")
            .email("executive@example.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(UserRole.EXECUTIVE)
            .department(department)
            .active(true)
            .build());

        // Create PM for project
        User pm = userRepository.save(User.builder()
            .name("PM-Li")
            .email("pm@example.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(UserRole.PM)
            .department(department)
            .active(true)
            .build());

        // Create project
        project = projectRepository.save(Project.builder()
            .name("Q1 Project")
            .description("First quarter")
            .status(ProjectStatus.ACTIVE)
            .pm(pm)
            .totalHours(100)
            .build());

        // Create task
        task = taskRepository.save(Task.builder()
            .name("Implementation Task")
            .description("Core work")
            .estimatedHours(40.0)
            .usedHours(0.0)
            .status(TaskStatus.IN_PROGRESS)
            .project(project)
            .assignee(executiveUser)
            .version(0)
            .build());
    }

    // ========== T111: Contract test for POST /api/timesheets ==========

    @Test
    @DisplayName("T111.1: POST /api/timesheets should create timesheet with valid request")
    void testCreateTimesheetWithValidRequest() throws Exception {
        CreateTimesheetRequest request = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(11, 0))
            .build();

        mockMvc.perform(post("/api/timesheets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header("Authorization", "Bearer " + generateMockToken(executiveUser.getId())))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.taskId").value(task.getId().intValue()))
            .andExpect(jsonPath("$.calculatedHours").value(closeTo(2.0, 0.1)))
            .andExpect(jsonPath("$.lunchDeducted").value(false))
            .andExpect(jsonPath("$.userId").value(executiveUser.getId().intValue()));
    }

    @Test
    @DisplayName("T111.2: POST /api/timesheets should return 400 for missing required fields")
    void testCreateTimesheetWithMissingFields() throws Exception {
        CreateTimesheetRequest request = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            // missing workDate, startTime, endTime
            .build();

        mockMvc.perform(post("/api/timesheets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header("Authorization", "Bearer " + generateMockToken(executiveUser.getId())))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("T111.3: POST /api/timesheets should return 401 for unauthorized user")
    void testCreateTimesheetUnauthorized() throws Exception {
        CreateTimesheetRequest request = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(11, 0))
            .build();

        mockMvc.perform(post("/api/timesheets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    // ========== T112: Contract test for POST /api/timesheets/calculate-preview ==========

    @Test
    @DisplayName("T112.1: POST /api/timesheets/calculate-preview should calculate hours with lunch deduction")
    void testCalculatePreviewWithLunchDeduction() throws Exception {
        // Work from 10:00 to 14:00 (includes lunch 12:00-13:00)
        CreateTimesheetRequest request = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(14, 0))
            .build();

        mockMvc.perform(post("/api/timesheets/calculate-preview")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header("Authorization", "Bearer " + generateMockToken(executiveUser.getId())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.calculatedHours").value(closeTo(3.0, 0.1))) // 4 - 1 lunch
            .andExpect(jsonPath("$.lunchDeducted").value(true))
            .andExpect(jsonPath("$.lunchHours").value(closeTo(1.0, 0.1)))
            .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("T112.2: POST /api/timesheets/calculate-preview should calculate hours without lunch deduction")
    void testCalculatePreviewWithoutLunchDeduction() throws Exception {
        // Work from 09:00 to 11:00 (no lunch overlap)
        CreateTimesheetRequest request = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(11, 0))
            .build();

        mockMvc.perform(post("/api/timesheets/calculate-preview")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header("Authorization", "Bearer " + generateMockToken(executiveUser.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.calculatedHours").value(closeTo(2.0, 0.1)))
            .andExpect(jsonPath("$.lunchDeducted").value(false));
    }

    @Test
    @DisplayName("T112.3: POST /api/timesheets/calculate-preview should return 400 for invalid times")
    void testCalculatePreviewInvalidTimes() throws Exception {
        CreateTimesheetRequest request = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(14, 0))
            .endTime(LocalTime.of(10, 0)) // end before start
            .build();

        mockMvc.perform(post("/api/timesheets/calculate-preview")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header("Authorization", "Bearer " + generateMockToken(executiveUser.getId())))
            .andExpect(status().isBadRequest());
    }

    // ========== T113: Contract test for PUT /api/timesheets/{timesheetId} ==========

    @Test
    @DisplayName("T113.1: PUT /api/timesheets/{id} should update timesheet with valid request")
    void testUpdateTimesheetWithValidRequest() throws Exception {
        // First create a timesheet
        CreateTimesheetRequest createRequest = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(11, 0))
            .build();

        var createResult = mockMvc.perform(post("/api/timesheets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest))
            .header("Authorization", "Bearer " + generateMockToken(executiveUser.getId())))
            .andExpect(status().isCreated())
            .andReturn();

        Long timesheetId = extractIdFromResponse(createResult);

        // Now update it
        UpdateTimesheetRequest updateRequest = UpdateTimesheetRequest.builder()
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(12, 0)) // 3 hours - 1 lunch = 2 hours
            .build();

        mockMvc.perform(put("/api/timesheets/" + timesheetId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest))
            .header("Authorization", "Bearer " + generateMockToken(executiveUser.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(timesheetId.intValue()))
            .andExpect(jsonPath("$.calculatedHours").value(closeTo(2.0, 0.1)))
            .andExpect(jsonPath("$.lunchDeducted").value(true));
    }

    @Test
    @DisplayName("T113.2: PUT /api/timesheets/{id} should return 404 for non-existent timesheet")
    void testUpdateNonExistentTimesheet() throws Exception {
        UpdateTimesheetRequest updateRequest = UpdateTimesheetRequest.builder()
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(12, 0))
            .build();

        mockMvc.perform(put("/api/timesheets/99999")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest))
            .header("Authorization", "Bearer " + generateMockToken(executiveUser.getId())))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("T113.3: PUT /api/timesheets/{id} should return 400 for invalid times")
    void testUpdateTimesheetInvalidTimes() throws Exception {
        // Create a timesheet first
        CreateTimesheetRequest createRequest = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(11, 0))
            .build();

        var createResult = mockMvc.perform(post("/api/timesheets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest))
            .header("Authorization", "Bearer " + generateMockToken(executiveUser.getId())))
            .andExpect(status().isCreated())
            .andReturn();

        Long timesheetId = extractIdFromResponse(createResult);

        // Try to update with invalid times
        UpdateTimesheetRequest updateRequest = UpdateTimesheetRequest.builder()
            .startTime(LocalTime.of(14, 0))
            .endTime(LocalTime.of(10, 0)) // end before start
            .build();

        mockMvc.perform(put("/api/timesheets/" + timesheetId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest))
            .header("Authorization", "Bearer " + generateMockToken(executiveUser.getId())))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("T113.4: PUT /api/timesheets/{id} should return 403 if not owner")
    void testUpdateTimesheetForbiddenNonOwner() throws Exception {
        // Create a timesheet
        CreateTimesheetRequest createRequest = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(11, 0))
            .build();

        var createResult = mockMvc.perform(post("/api/timesheets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest))
            .header("Authorization", "Bearer " + generateMockToken(executiveUser.getId())))
            .andExpect(status().isCreated())
            .andReturn();

        Long timesheetId = extractIdFromResponse(createResult);

        // Try to update as different user
        User otherUser = userRepository.save(User.builder()
            .name("Other-User")
            .email("other@example.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(UserRole.EXECUTIVE)
            .department(department)
            .active(true)
            .build());

        UpdateTimesheetRequest updateRequest = UpdateTimesheetRequest.builder()
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(12, 0))
            .build();

        mockMvc.perform(put("/api/timesheets/" + timesheetId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest))
            .header("Authorization", "Bearer " + generateMockToken(otherUser.getId())))
            .andExpect(status().isForbidden());
    }

    // ========== Helper Methods ==========

    private String generateMockToken(Long userId) {
        // In a real scenario, this would generate a valid JWT token
        // For testing purposes, we'll return a mock token format
        // The actual JWT validation would be mocked or disabled in test profile
        return "mock-jwt-token-user-" + userId;
    }

    private Long extractIdFromResponse(org.springframework.test.web.servlet.MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        return objectMapper.readTree(content).get("id").asLong();
    }
}
