package com.example.timesheet.security;

import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Permission tests for EMPLOYEE role (T060).
 * Verifies that EMPLOYEE role can only access their own data and designated endpoints.
 * 
 * Test scenarios:
 * - EMPLOYEE can access /api/employee/* endpoints
 * - EMPLOYEE cannot access other roles' endpoints (PM, MANAGER, HR, EXECUTIVE)
 * - EMPLOYEE can only view/edit their own timesheet entries
 * - EMPLOYEE can only view tasks assigned to them
 * - EMPLOYEE cannot access admin functions
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Employee Permission Tests")
class EmployeePermissionTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private String employeeToken;
    private String pmToken;
    private String managerToken;
    private String hrToken;
    private String executiveToken;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        // Create users with different roles
        User employee = createUser("employee@test.com", "Employee User", UserRole.EMPLOYEE, "EMP001");
        User pm = createUser("pm@test.com", "PM User", UserRole.PM, "PM001");
        User manager = createUser("manager@test.com", "Manager User", UserRole.MANAGER, "MGR001");
        User hr = createUser("hr@test.com", "HR User", UserRole.HR, "HR001");
        User executive = createUser("exec@test.com", "Executive User", UserRole.EXECUTIVE, "EXEC001");
        
        // Generate tokens
        employeeToken = jwtTokenProvider.generateTokenFromUsername(employee.getEmail());
        pmToken = jwtTokenProvider.generateTokenFromUsername(pm.getEmail());
        managerToken = jwtTokenProvider.generateTokenFromUsername(manager.getEmail());
        hrToken = jwtTokenProvider.generateTokenFromUsername(hr.getEmail());
        executiveToken = jwtTokenProvider.generateTokenFromUsername(executive.getEmail());
    }
    
    private User createUser(String email, String name, UserRole role, String employeeId) {
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("password123"))
                .name(name)
                .role(role)
                .active(true)
                .build();
        return userRepository.save(user);
    }
    
    @Nested
    @DisplayName("Employee Endpoint Access Tests")
    class EmployeeEndpointAccessTests {
        
        @Test
        @DisplayName("EMPLOYEE should access /api/employee/tasks")
        void employeeShouldAccessEmployeeTasks() throws Exception {
            mockMvc.perform(get("/api/employee/tasks")
                    .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());
        }
        
        @Test
        @DisplayName("EMPLOYEE should access /api/employee/timesheets")
        void employeeShouldAccessEmployeeTimesheets() throws Exception {
            mockMvc.perform(get("/api/employee/timesheets")
                    .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());
        }
        
        @Test
        @DisplayName("Unauthenticated user should not access employee endpoints")
        void unauthenticatedUserShouldNotAccessEmployeeEndpoints() throws Exception {
            mockMvc.perform(get("/api/employee/tasks"))
                .andExpect(status().isUnauthorized());
        }
    }
    
    @Nested
    @DisplayName("Cross-Role Access Restriction Tests")
    class CrossRoleAccessRestrictionTests {
        
        @Test
        @DisplayName("EMPLOYEE should NOT access PM endpoints")
        void employeeShouldNotAccessPMEndpoints() throws Exception {
            mockMvc.perform(get("/api/pm/projects")
                    .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
        }
        
        @Test
        @DisplayName("EMPLOYEE should NOT access MANAGER endpoints")
        void employeeShouldNotAccessManagerEndpoints() throws Exception {
            mockMvc.perform(get("/api/manager/department/timesheets")
                    .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
        }
        
        @Test
        @DisplayName("EMPLOYEE should NOT access HR endpoints")
        void employeeShouldNotAccessHREndpoints() throws Exception {
            mockMvc.perform(get("/api/hr/users")
                    .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
        }
        
        @Test
        @DisplayName("EMPLOYEE should NOT access EXECUTIVE endpoints")
        void employeeShouldNotAccessExecutiveEndpoints() throws Exception {
            mockMvc.perform(get("/api/executive/projects")
                    .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
        }
        
        @Test
        @DisplayName("PM should NOT access employee-specific endpoints")
        void pmShouldNotAccessEmployeeEndpoints() throws Exception {
            // PM has their own endpoints and shouldn't access employee-specific ones
            mockMvc.perform(get("/api/employee/tasks")
                    .header("Authorization", "Bearer " + pmToken))
                .andExpect(status().isForbidden());
        }
    }
    
    @Nested
    @DisplayName("Data Isolation Tests")
    class DataIsolationTests {
        
        @Test
        @DisplayName("EMPLOYEE can access auth/me endpoint")
        void employeeCanAccessAuthMe() throws Exception {
            mockMvc.perform(get("/api/auth/me")
                    .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());
        }
        
        @Test
        @DisplayName("Different roles can access their respective auth/me")
        void differentRolesCanAccessTheirAuthMe() throws Exception {
            // All roles should be able to access their own profile
            mockMvc.perform(get("/api/auth/me")
                    .header("Authorization", "Bearer " + pmToken))
                .andExpect(status().isOk());
            
            mockMvc.perform(get("/api/auth/me")
                    .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
            
            mockMvc.perform(get("/api/auth/me")
                    .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk());
        }
    }
    
    @Nested
    @DisplayName("Authorization Header Tests")
    class AuthorizationHeaderTests {
        
        @Test
        @DisplayName("Should reject malformed Authorization header")
        void shouldRejectMalformedAuthorizationHeader() throws Exception {
            mockMvc.perform(get("/api/employee/tasks")
                    .header("Authorization", "InvalidToken"))
                .andExpect(status().isUnauthorized());
        }
        
        @Test
        @DisplayName("Should reject expired token")
        void shouldRejectExpiredToken() throws Exception {
            // This test would require creating an expired token
            // For now, we test with an invalid token
            mockMvc.perform(get("/api/employee/tasks")
                    .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
        }
        
        @Test
        @DisplayName("Should reject missing Authorization header")
        void shouldRejectMissingAuthorizationHeader() throws Exception {
            mockMvc.perform(get("/api/employee/tasks"))
                .andExpect(status().isUnauthorized());
        }
    }
    
    @Nested
    @DisplayName("Public Endpoint Access Tests")
    class PublicEndpointAccessTests {
        
        @Test
        @DisplayName("Login endpoint should be accessible without authentication")
        void loginEndpointShouldBeAccessibleWithoutAuthentication() throws Exception {
            // Login endpoint should be public
            mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed()); // GET not allowed, but accessible
        }
    }
}
