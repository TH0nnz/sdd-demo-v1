package com.example.timesheet.contract;

import com.example.timesheet.domain.entity.*;
import com.example.timesheet.domain.enums.ProjectStatus;
import com.example.timesheet.domain.enums.TaskStatus;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.*;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contract tests for Report API endpoints (T136-T137).
 * Tests verify that API contracts match the OpenAPI specification for department reports.
 * Tests are written FIRST and should FAIL before implementation.
 * 
 * Corresponds to User Story 4 - 部門工時報表 (Department Reports)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Report API Contract Tests")
@Transactional
class ReportApiContractTest {

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
    private TimesheetRepository timesheetRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User deptHeadUser;
    private User executiveUser;
    private Department department;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        // Create test department
        department = departmentRepository.save(Department.builder()
            .name("Engineering")
            .build());

        // Create MANAGER user
        deptHeadUser = userRepository.save(User.builder()
            .name("Manager-Li")
            .email("depthead@example.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(UserRole.MANAGER)
            .department(department)
            .active(true)
            .build());

        // Create EXECUTIVE user in the same department
        executiveUser = userRepository.save(User.builder()
            .name("Executive-Wang")
            .email("executive@example.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(UserRole.EXECUTIVE)
            .department(department)
            .active(true)
            .build());

        // Create project
        project = projectRepository.save(Project.builder()
            .name("Mobile App")
            .description("iOS and Android app development")
            .status(ProjectStatus.ACTIVE)
            .pm(deptHeadUser)
            .totalHours(100)
            .allocatedHours(0)
            .build());

        // Create task
        task = taskRepository.save(Task.builder()
            .name("API Development")
            .project(project)
            .assignee(executiveUser)
            .estimatedHours(40.0)
            .usedHours(0.0)
            .status(TaskStatus.IN_PROGRESS)
            .version(0)
            .build());

        // Create sample timesheet entries for the current week
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 5; i++) {
            LocalDate workDate = today.minusDays(i);
            timesheetRepository.save(TimesheetEntry.builder()
                .user(executiveUser)
                .task(task)
                .workDate(workDate)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .calculatedHours(new BigDecimal("8"))
                .lunchDeducted(true)
                .build());
        }
    }

    @Test
    @DisplayName("T136: Contract test for GET /api/reports/timesheets - should return paginated timesheet report")
    void testGetTimesheetReportContract() throws Exception {
        mockMvc.perform(get("/api/reports/timesheets")
            .with(user(deptHeadUser.getEmail()).roles("DEPT_HEAD"))
            .param("departmentId", department.getId().toString())
            .param("startDate", LocalDate.now().minusDays(7).toString())
            .param("endDate", LocalDate.now().toString())
            .param("page", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].date").exists())
            .andExpect(jsonPath("$.content[0].userName").exists())
            .andExpect(jsonPath("$.content[0].projectName").exists())
            .andExpect(jsonPath("$.content[0].taskName").exists())
            .andExpect(jsonPath("$.content[0].hours").exists())
            .andExpect(jsonPath("$.content[0].hours", instanceOf(Number.class)))
            .andExpect(jsonPath("$.page").exists())
            .andExpect(jsonPath("$.page.currentPage", greaterThanOrEqualTo(0)))
            .andExpect(jsonPath("$.page.totalPages", greaterThanOrEqualTo(0)))
            .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(5)))
            .andExpect(jsonPath("$.page.hasNext").isBoolean());
    }

    @Test
    @DisplayName("T137: Contract test for GET /api/reports/timesheets/export - should return CSV file")
    void testExportTimesheetReportCsvContract() throws Exception {
        mockMvc.perform(get("/api/reports/timesheets/export")
            .with(user(deptHeadUser.getEmail()).roles("DEPT_HEAD"))
            .param("departmentId", department.getId().toString())
            .param("startDate", LocalDate.now().minusDays(7).toString())
            .param("endDate", LocalDate.now().toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/csv;charset=UTF-8"))
            .andExpect(content().string(containsString("Date")))
            .andExpect(content().string(containsString("User")))
            .andExpect(content().string(containsString("Project")))
            .andExpect(content().string(containsString("Task")))
            .andExpect(content().string(containsString("Hours")));
    }

    @Test
    @DisplayName("T136: GET /api/reports/timesheets - unauthorized users should get 403")
    void testGetTimesheetReportUnauthorized() throws Exception {
        mockMvc.perform(get("/api/reports/timesheets")
            .with(user(executiveUser.getEmail()).roles("EXECUTIVE"))
            .param("departmentId", department.getId().toString())
            .param("page", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("T137: GET /api/reports/timesheets/export - unauthorized users should get 403")
    void testExportTimesheetReportUnauthorized() throws Exception {
        mockMvc.perform(get("/api/reports/timesheets/export")
            .with(user(executiveUser.getEmail()).roles("EXECUTIVE"))
            .param("departmentId", department.getId().toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("T136: GET /api/reports/timesheets - should support filtering by user")
    void testGetTimesheetReportFilterByUser() throws Exception {
        mockMvc.perform(get("/api/reports/timesheets")
            .with(user(deptHeadUser.getEmail()).roles("DEPT_HEAD"))
            .param("departmentId", department.getId().toString())
            .param("userId", executiveUser.getId().toString())
            .param("page", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

}
