package com.example.timesheet.contract;

import com.example.timesheet.domain.entity.Department;
import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.DepartmentRepository;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.request.CreateUserRequest;
import com.example.timesheet.dto.request.UpdateUserRequest;
import com.example.timesheet.dto.response.UserResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contract tests for User API endpoints.
 * Tests verify that API contracts match the OpenAPI specification.
 * These tests are written FIRST and should FAIL before implementation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User API Contract Tests")
@Transactional
class UserApiContractTest {

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

    private String hrToken;
    private Department department;
    private User hrUser;

    @BeforeEach
    void setUp() {
        // Create test department
        department = departmentRepository.save(Department.builder()
                .name("研發部")
                .build());

        // Create HR user for authentication
        hrUser = userRepository.save(User.builder()
                .name("HR-張三")
                .email("hr@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.HR)
                .department(department)
                .active(true)
                .build());

        // In a real test, we'd generate JWT token for hrUser
        // For now, we'll test without authentication or mock the security context
        // This will be properly integrated with actual JWT in later tests
    }

    // ========== T029: Contract test for POST /api/users ==========

    @Test
    @DisplayName("T029.1: POST /api/users should create user with valid request")
    void createUserShouldSucceedWithValidRequest() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("李四")
                .email("lisi@example.com")
                .role(UserRole.EXECUTIVE)
                .departmentId(department.getId())
                .build();

        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("李四"))
                .andExpect(jsonPath("$.email").value("lisi@example.com"))
                .andExpect(jsonPath("$.role").value(UserRole.EXECUTIVE.toString()))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.version").value(0))
                .andReturn();

        // Verify response structure matches UserResponse schema
        String responseBody = result.getResponse().getContentAsString();
        UserResponse response = objectMapper.readValue(responseBody, UserResponse.class);
        
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("李四");
        assertThat(response.getEmail()).isEqualTo("lisi@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.EXECUTIVE);
        assertThat(response.getActive()).isTrue();
        assertThat(response.getVersion()).isEqualTo(0);
    }

    @Test
    @DisplayName("T029.2: POST /api/users should fail with missing required fields")
    void createUserShouldFailWithMissingName() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .role(UserRole.EXECUTIVE)
                .departmentId(department.getId())
                .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("T029.3: POST /api/users should fail with invalid email")
    void createUserShouldFailWithInvalidEmail() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("李四")
                .email("invalid-email")
                .role(UserRole.EXECUTIVE)
                .departmentId(department.getId())
                .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("T029.4: POST /api/users should fail with duplicate email")
    void createUserShouldFailWithDuplicateEmail() throws Exception {
        // First user already exists
        userRepository.save(User.builder()
                .name("王五")
                .email("wangwu@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.EXECUTIVE)
                .department(department)
                .active(true)
                .build());

        CreateUserRequest request = CreateUserRequest.builder()
                .name("李四")
                .email("wangwu@example.com")  // Duplicate email
                .role(UserRole.EXECUTIVE)
                .departmentId(department.getId())
                .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("T029.5: POST /api/users should auto-generate password if not provided")
    void createUserShouldAutoGeneratePassword() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("王五")
                .email("wangwu@example.com")
                .role(UserRole.PM)
                .departmentId(department.getId())
                // No initialPassword provided
                .build();

        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        // Verify user was created with some password
        String responseBody = result.getResponse().getContentAsString();
        UserResponse response = objectMapper.readValue(responseBody, UserResponse.class);
        
        // User should be created (we can't verify password without access, but response should have ID)
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("王五");
    }

    @Test
    @DisplayName("T029.6: POST /api/users should accept various roles")
    void createUserShouldAcceptAllRoles() throws Exception {
        for (UserRole role : UserRole.values()) {
            CreateUserRequest request = CreateUserRequest.builder()
                    .name("Test User " + role)
                    .email("user_" + role.name().toLowerCase() + "@example.com")
                    .role(role)
                    .departmentId(department.getId())
                    .build();

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.role").value(role.toString()));
        }
    }

    // ========== T030: Contract test for PUT /api/users/{userId} ==========

    @Test
    @DisplayName("T030.1: PUT /api/users/{userId} should update user with valid request")
    void updateUserShouldSucceedWithValidRequest() throws Exception {
        // Create a user to update
        User user = userRepository.save(User.builder()
                .name("舊名字")
                .email("oldname@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.EXECUTIVE)
                .department(department)
                .active(true)
                .build());

        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("新名字")
                .role(UserRole.PM)
                .departmentId(department.getId())
                .version(user.getVersion())
                .build();

        mockMvc.perform(put("/api/users/{userId}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value("新名字"))
                .andExpect(jsonPath("$.role").value(UserRole.PM.toString()))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    @DisplayName("T030.2: PUT /api/users/{userId} should fail with non-existent user")
    void updateUserShouldFailWithNonExistentUser() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("新名字")
                .role(UserRole.PM)
                .version(0)
                .build();

        mockMvc.perform(put("/api/users/{userId}", 99999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("T030.3: PUT /api/users/{userId} should handle optimistic locking conflict")
    void updateUserShouldFailWithVersionMismatch() throws Exception {
        User user = userRepository.save(User.builder()
                .name("舊名字")
                .email("conflict@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.EXECUTIVE)
                .department(department)
                .active(true)
                .build());

        // Update the user first time to increment version
        User updated = userRepository.save(User.builder()
                .id(user.getId())
                .name("更新名字")
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .role(UserRole.PM)
                .department(user.getDepartment())
                .active(user.getActive())
                .version(1)
                .build());

        // Try to update with old version
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("另一個名字")
                .version(0)  // Old version
                .build();

        mockMvc.perform(put("/api/users/{userId}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("T030.4: PUT /api/users/{userId} should allow partial updates")
    void updateUserShouldAllowPartialUpdate() throws Exception {
        User user = userRepository.save(User.builder()
                .name("原始名字")
                .email("original@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.EXECUTIVE)
                .department(department)
                .active(true)
                .build());

        // Update only the role, keep name same
        UpdateUserRequest request = UpdateUserRequest.builder()
                .role(UserRole.PM)
                .version(user.getVersion())
                .build();

        mockMvc.perform(put("/api/users/{userId}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("原始名字"))  // Name unchanged
                .andExpect(jsonPath("$.role").value(UserRole.PM.toString()));  // Role changed
    }

    @Test
    @DisplayName("T030.5: PUT /api/users/{userId} should fail with invalid name")
    void updateUserShouldFailWithInvalidName() throws Exception {
        User user = userRepository.save(User.builder()
                .name("原始名字")
                .email("original@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.EXECUTIVE)
                .department(department)
                .active(true)
                .build());

        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("A")  // Too short (min 2)
                .version(user.getVersion())
                .build();

        mockMvc.perform(put("/api/users/{userId}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
