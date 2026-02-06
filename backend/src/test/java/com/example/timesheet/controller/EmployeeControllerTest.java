package com.example.timesheet.controller;

import com.example.timesheet.domain.entity.Project;
import com.example.timesheet.domain.entity.Task;
import com.example.timesheet.domain.entity.TimesheetEntry;
import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.enums.ProjectStatus;
import com.example.timesheet.domain.enums.TaskStatus;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.ProjectRepository;
import com.example.timesheet.domain.repository.TaskRepository;
import com.example.timesheet.domain.repository.TimesheetRepository;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.request.CreateTimesheetRequest;
import com.example.timesheet.dto.request.UpdateTimesheetRequest;
import com.example.timesheet.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for EmployeeController (T059).
 * Tests employee endpoints with MockMvc and actual Spring context.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("EmployeeController Integration Tests")
class EmployeeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private TimesheetRepository timesheetRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private User employeeUser;
    private Project testProject;
    private Task assignedTask;
    private String employeeToken;
    
    @BeforeEach
    void setUp() {
        timesheetRepository.deleteAll();
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.deleteAll();
        
        employeeUser = User.builder()
                .email("employee@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .name("Test Employee")
                .role(UserRole.EMPLOYEE)
                .active(true)
                .build();
        employeeUser = userRepository.save(employeeUser);
        
        // Create a project for tasks
        testProject = Project.builder()
                .name("Test Project")
                .description("Test project for employee tasks")
                .status(ProjectStatus.ACTIVE)
                .pm(employeeUser)
                .totalHours(100)
                .allocatedHours(0)
                .build();
        testProject = projectRepository.save(testProject);
        
        assignedTask = new Task();
        assignedTask.setName("Test Task");
        assignedTask.setDescription("Test Description");
        assignedTask.setProject(testProject);
        assignedTask.setEstimatedHours(10.0);
        assignedTask.setUsedHours(0.0);
        assignedTask.setStatus(TaskStatus.IN_PROGRESS);
        assignedTask.setAssignee(employeeUser);
        assignedTask = taskRepository.save(assignedTask);
        
        employeeToken = jwtTokenProvider.generateTokenFromUsername(employeeUser.getEmail());
    }
    
    @Nested
    @DisplayName("GET /api/employee/tasks Tests")
    class GetTasksTests {
        
        @Test
        @DisplayName("Should get assigned tasks successfully")
        void shouldGetAssignedTasksSuccessfully() throws Exception {
            mockMvc.perform(get("/api/employee/tasks")
                    .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        }
        
        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/employee/tasks"))
                .andExpect(status().isUnauthorized());
        }
    }
    
    @Nested
    @DisplayName("POST /api/employee/timesheets Tests")
    class CreateTimesheetTests {
        
        @Test
        @DisplayName("Should create timesheet successfully")
        void shouldCreateTimesheetSuccessfully() throws Exception {
            CreateTimesheetRequest request = new CreateTimesheetRequest();
            request.setTaskId(assignedTask.getId());
            request.setWorkDate(LocalDate.now());
            request.setStartTime(LocalTime.of(9, 0));
            request.setEndTime(LocalTime.of(14, 0));
            
            mockMvc.perform(post("/api/employee/timesheets")
                    .header("Authorization", "Bearer " + employeeToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calculatedHours").exists());
        }
    }
}
