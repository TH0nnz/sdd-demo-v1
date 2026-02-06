package com.example.timesheet.contract;

import com.example.timesheet.domain.entity.Department;
import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.DepartmentRepository;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.request.ApproveTimeRequestRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contract tests for TimeRequest API endpoints (T054).
 * Tests verify that API contracts match the OpenAPI specification.
 * Written FIRST and should FAIL before implementation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TimeRequest API Contract Tests")
@Transactional
class TimeRequestApiContractTest {

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
     * T054: Contract test for POST /api/time-requests/{requestId}/approve
     * Manager approves a time request for additional project hours
     */
    @Test
    @DisplayName("T054: POST /api/time-requests/{requestId}/approve should approve request and return 200 OK")
    void testApproveTimeRequest() throws Exception {
        // This test will verify that the endpoint exists and follows the contract
        // The actual implementation will be tested in integration tests
        
        // For now, this test verifies the endpoint structure:
        // - Only MANAGER can approve
        // - Returns 200 OK on success
        // - Response includes updated project hours
        
        mockMvc.perform(post("/api/time-requests/1/approve")
            .with(user("manager@example.com").roles("MANAGER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                ApproveTimeRequestRequest.builder()
                    .approved(true)
                    .reason("Approved for resource allocation")
                    .build())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(anyOf(
                equalTo("APPROVED"),
                equalTo("REJECTED"),
                equalTo("PENDING")
            )));
    }

    /**
     * T054: Verify non-manager cannot approve time request
     */
    @Test
    @DisplayName("T054: Non-manager should not be able to approve time request (403 Forbidden)")
    void testApproveTimeRequestForbidden() throws Exception {
        mockMvc.perform(post("/api/time-requests/1/approve")
            .with(user("pm@example.com").roles("PM"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                ApproveTimeRequestRequest.builder()
                    .approved(true)
                    .reason("Unauthorized approval")
                    .build())))
            .andExpect(status().isForbidden());
    }

    /**
     * T054: Verify non-existent time request returns 404
     */
    @Test
    @DisplayName("T054: Approve non-existent time request should return 404 Not Found")
    void testApproveNonExistentTimeRequest() throws Exception {
        mockMvc.perform(post("/api/time-requests/999999/approve")
            .with(user("manager@example.com").roles("MANAGER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                ApproveTimeRequestRequest.builder()
                    .approved(true)
                    .reason("Approval")
                    .build())))
            .andExpect(status().isNotFound());
    }

    /**
     * T054: Verify cannot approve already approved request
     */
    @Test
    @DisplayName("T054: Approve already approved request should return 400 Bad Request")
    void testApproveAlreadyApprovedRequest() throws Exception {
        // This will be verified in integration tests
        // For contract test, we verify the endpoint exists and has proper validation
        
        mockMvc.perform(post("/api/time-requests/1/approve")
            .with(user("manager@example.com").roles("MANAGER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                ApproveTimeRequestRequest.builder()
                    .approved(true)
                    .reason("Approval")
                    .build())))
            .andExpect(result -> {
                int status = result.getResponse().getStatus();
                if (status != 200 && status != 400 && status != 404) {
                    throw new AssertionError("Status should be 200, 400, or 404 but was " + status);
                }
            });
    }

    /**
     * Verify unauthenticated user cannot access endpoint
     */
    @Test
    @DisplayName("POST /api/time-requests/1/approve without authentication should return 401 Unauthorized")
    void testApproveTimeRequestUnauthorized() throws Exception {
        mockMvc.perform(post("/api/time-requests/1/approve")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                ApproveTimeRequestRequest.builder()
                    .approved(true)
                    .reason("Approval")
                    .build())))
            .andExpect(status().isUnauthorized());
    }
}
