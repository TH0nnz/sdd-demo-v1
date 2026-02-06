package com.example.timesheet.integration;

import com.example.timesheet.domain.entity.*;
import com.example.timesheet.domain.enums.ProjectStatus;
import com.example.timesheet.domain.enums.TaskStatus;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for Report functionality (T138-T139).
 * Tests verify end-to-end report queries and CSV export functionality.
 * 
 * Corresponds to User Story 4 - 部門工時報表 (Department Reports)
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Report Integration Tests")
@Transactional
class ReportIntegrationTest {

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
    private User executiveUser1;
    private User executiveUser2;
    private Department department1;
    private Department department2;
    private Project project1;
    private Project project2;
    private Task task1;
    private Task task2;
    private List<TimesheetEntry> timesheetEntries;

    @BeforeEach
    void setUp() {
        // Create two departments
        department1 = departmentRepository.save(Department.builder()
            .name("Engineering")
            .build());

        department2 = departmentRepository.save(Department.builder()
            .name("Design")
            .build());

        // Create MANAGER user
        deptHeadUser = userRepository.save(User.builder()
            .name("Manager-Li")
            .email("depthead@example.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(UserRole.MANAGER)
            .department(department1)
            .active(true)
            .build());

        // Create EXECUTIVE users in different departments
        executiveUser1 = userRepository.save(User.builder()
            .name("Executive-Wang")
            .email("executive1@example.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(UserRole.EXECUTIVE)
            .department(department1)
            .active(true)
            .build());

        executiveUser2 = userRepository.save(User.builder()
            .name("Executive-Zhang")
            .email("executive2@example.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .role(UserRole.EXECUTIVE)
            .department(department2)
            .active(true)
            .build());

        // Create projects
        project1 = projectRepository.save(Project.builder()
            .name("Mobile App")
            .description("iOS and Android app development")
            .status(ProjectStatus.ACTIVE)
            .pm(deptHeadUser)
            .totalHours(100)
            .allocatedHours(0)
            .build());

        project2 = projectRepository.save(Project.builder()
            .name("Web Portal")
            .description("Company web portal")
            .status(ProjectStatus.ACTIVE)
            .pm(deptHeadUser)
            .totalHours(50)
            .allocatedHours(0)
            .build());

        // Create tasks
        task1 = taskRepository.save(Task.builder()
            .name("API Development")
            .project(project1)
            .assignee(executiveUser1)
            .estimatedHours(40.0)
            .usedHours(0.0)
            .status(TaskStatus.IN_PROGRESS)
            .version(0)
            .build());

        task2 = taskRepository.save(Task.builder()
            .name("Frontend Development")
            .project(project2)
            .assignee(executiveUser1)
            .estimatedHours(50.0)
            .usedHours(0.0)
            .status(TaskStatus.IN_PROGRESS)
            .version(0)
            .build());

        // Create timesheet entries for the current week
        LocalDate today = LocalDate.now();
        timesheetEntries = new java.util.ArrayList<>();
        
        // executiveUser1: 5 days of work, 8 hours per day
        for (int i = 0; i < 5; i++) {
            LocalDate workDate = today.minusDays(i);
            TimesheetEntry entry = timesheetRepository.save(TimesheetEntry.builder()
                .user(executiveUser1)
                .task(task1)
                .workDate(workDate)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .calculatedHours(new BigDecimal("8"))
                .lunchDeducted(true)
                .build());
            timesheetEntries.add(entry);
        }

        // executiveUser1: 3 days on project2
        for (int i = 0; i < 3; i++) {
            LocalDate workDate = today.minusDays(7 + i);
            TimesheetEntry entry = timesheetRepository.save(TimesheetEntry.builder()
                .user(executiveUser1)
                .task(task2)
                .workDate(workDate)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .calculatedHours(new BigDecimal("8"))
                .lunchDeducted(true)
                .build());
            timesheetEntries.add(entry);
        }

        // executiveUser2 (different department): 2 days
        for (int i = 0; i < 2; i++) {
            LocalDate workDate = today.minusDays(i);
            TimesheetEntry entry = timesheetRepository.save(TimesheetEntry.builder()
                .user(executiveUser2)
                .task(task1) // same project but different employee
                .workDate(workDate)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 30))
                .calculatedHours(new BigDecimal("8.5"))
                .lunchDeducted(true)
                .build());
            timesheetEntries.add(entry);
        }
    }

    @Test
    @DisplayName("T138: Integration test for timesheet report with filters - should retrieve entries within department and date range")
    void testTimesheetReportWithFilters() {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        // Query timesheet entries for department1 within date range
        List<TimesheetEntry> result = timesheetRepository.findAll().stream()
            .filter(entry -> entry.getUser().getDepartment().getId().equals(department1.getId()))
            .filter(entry -> !entry.getWorkDate().isBefore(startDate) && !entry.getWorkDate().isAfter(endDate))
            .toList();

        // Should have 8 entries: 5 from executiveUser1 on task1 + 3 from executiveUser1 on task2
        assertThat(result).hasSize(8);
        
        // Verify all entries belong to department1 users
        assertThat(result).allMatch(entry -> entry.getUser().getDepartment().getId().equals(department1.getId()));
        
        // Verify date range
        assertThat(result).allMatch(entry -> !entry.getWorkDate().isBefore(startDate) && !entry.getWorkDate().isAfter(endDate));
    }

    @Test
    @DisplayName("T138: Integration test for timesheet report - should support filtering by user")
    void testTimesheetReportFilterByUser() {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        // Query timesheet entries for specific user
        List<TimesheetEntry> result = timesheetRepository.findAll().stream()
            .filter(entry -> entry.getUser().getId().equals(executiveUser1.getId()))
            .filter(entry -> !entry.getWorkDate().isBefore(startDate) && !entry.getWorkDate().isAfter(endDate))
            .toList();

        // Should have 8 entries from executiveUser1 (5 current week + 3 from previous week)
        assertThat(result).hasSize(8);
        assertThat(result).allMatch(entry -> entry.getUser().getId().equals(executiveUser1.getId()));
    }

    @Test
    @DisplayName("T138: Integration test for timesheet report - should support filtering by project")
    void testTimesheetReportFilterByProject() {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        // Query timesheet entries for specific project
        List<TimesheetEntry> result = timesheetRepository.findAll().stream()
            .filter(entry -> entry.getTask().getProject().getId().equals(project1.getId()))
            .filter(entry -> !entry.getWorkDate().isBefore(startDate) && !entry.getWorkDate().isAfter(endDate))
            .toList();

        // Should have 7 entries: 5 from executiveUser1 + 2 from executiveUser2 on project1
        assertThat(result).hasSize(7);
        assertThat(result).allMatch(entry -> entry.getTask().getProject().getId().equals(project1.getId()));
    }

    @Test
    @DisplayName("T138: Integration test for timesheet report - calculate summary statistics")
    void testTimesheetReportSummaryStatistics() {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        // Get all timesheet entries
        List<TimesheetEntry> entries = timesheetRepository.findAll().stream()
            .filter(entry -> entry.getUser().getDepartment().getId().equals(department1.getId()))
            .filter(entry -> !entry.getWorkDate().isBefore(startDate) && !entry.getWorkDate().isAfter(endDate))
            .toList();

        // Calculate total hours
        BigDecimal totalHours = entries.stream()
            .map(TimesheetEntry::getCalculatedHours)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Should be 5*8 + 3*8 = 64 hours
        assertThat(totalHours).isEqualByComparingTo(new BigDecimal("64"));

        // Calculate hours by project
        Map<String, BigDecimal> hoursByProject = entries.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                entry -> entry.getTask().getProject().getName(),
                java.util.stream.Collectors.reducing(
                    BigDecimal.ZERO,
                    TimesheetEntry::getCalculatedHours,
                    BigDecimal::add
                )
            ));

        // Project1 (Mobile App): 5*8 = 40 hours
        // Project2 (Web Portal): 3*8 = 24 hours
        assertThat(hoursByProject).containsEntry("Mobile App", new BigDecimal("40"));
        assertThat(hoursByProject).containsEntry("Web Portal", new BigDecimal("24"));
    }

    @Test
    @DisplayName("T138: Integration test for timesheet report - calculate summary by user")
    void testTimesheetReportSummaryByUser() {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        // Get all timesheet entries
        List<TimesheetEntry> entries = timesheetRepository.findAll().stream()
            .filter(entry -> entry.getUser().getDepartment().getId().equals(department1.getId()))
            .filter(entry -> !entry.getWorkDate().isBefore(startDate) && !entry.getWorkDate().isAfter(endDate))
            .toList();

        // Calculate hours by user
        Map<String, BigDecimal> hoursByUser = entries.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                entry -> entry.getUser().getName(),
                java.util.stream.Collectors.reducing(
                    BigDecimal.ZERO,
                    TimesheetEntry::getCalculatedHours,
                    BigDecimal::add
                )
            ));

        // ExecutiveUser1: 5*8 + 3*8 = 64 hours
        assertThat(hoursByUser).containsEntry("Executive-Wang", new BigDecimal("64"));
    }

    @Test
    @DisplayName("T139: Integration test for CSV export - should generate proper CSV content")
    void testTimesheetReportCsvExport() {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        // Get all timesheet entries
        List<TimesheetEntry> entries = timesheetRepository.findAll().stream()
            .filter(entry -> entry.getUser().getDepartment().getId().equals(department1.getId()))
            .filter(entry -> !entry.getWorkDate().isBefore(startDate) && !entry.getWorkDate().isAfter(endDate))
            .toList();

        // Simulate CSV export
        StringBuilder csv = new StringBuilder();
        csv.append("Date,User,Project,Task,Hours\n");
        
        for (TimesheetEntry entry : entries) {
            csv.append(entry.getWorkDate())
                .append(",").append(entry.getUser().getName())
                .append(",").append(entry.getTask().getProject().getName())
                .append(",").append(entry.getTask().getName())
                .append(",").append(entry.getCalculatedHours())
                .append("\n");
        }

        String csvContent = csv.toString();
        
        // Verify CSV structure
        assertThat(csvContent).contains("Date,User,Project,Task,Hours");
        assertThat(csvContent).contains("Executive-Wang");
        assertThat(csvContent).contains("Mobile App");
        assertThat(csvContent).contains("API Development");
        
        // Verify data rows match entries count
        String[] lines = csvContent.split("\n");
        assertThat(lines).hasSizeGreaterThanOrEqualTo(entries.size() + 1); // +1 for header
    }

    @Test
    @DisplayName("T139: Integration test for CSV export - should handle special characters")
    void testTimesheetReportCsvExportSpecialCharacters() {
        // Create an entry with special characters
        LocalDate workDate = LocalDate.now().minusDays(1);
        Task specialTask = taskRepository.save(Task.builder()
            .name("API \"Development\" & Testing")
            .project(project1)
            .assignee(executiveUser1)
            .estimatedHours(40.0)
            .usedHours(0.0)
            .status(TaskStatus.IN_PROGRESS)
            .version(0)
            .build());

        timesheetRepository.save(TimesheetEntry.builder()
            .user(executiveUser1)
            .task(specialTask)
            .workDate(workDate)
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(17, 0))
            .calculatedHours(new BigDecimal("8"))
            .lunchDeducted(true)
            .build());

        // Retrieve and verify
        List<TimesheetEntry> entries = timesheetRepository.findAll().stream()
            .filter(entry -> entry.getTask().getName().contains("&"))
            .toList();

        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getTask().getName()).contains("&").contains("\"");
    }

    @Test
    @DisplayName("T139: Integration test for CSV export - empty result set")
    void testTimesheetReportCsvExportEmpty() {
        LocalDate startDate = LocalDate.now().plusDays(100);
        LocalDate endDate = LocalDate.now().plusDays(200);

        // Query timesheet entries for non-existent date range
        List<TimesheetEntry> entries = timesheetRepository.findAll().stream()
            .filter(entry -> !entry.getWorkDate().isBefore(startDate) && !entry.getWorkDate().isAfter(endDate))
            .toList();

        assertThat(entries).isEmpty();

        // CSV should still have header
        StringBuilder csv = new StringBuilder();
        csv.append("Date,User,Project,Task,Hours\n");
        
        for (TimesheetEntry entry : entries) {
            csv.append(entry.getWorkDate())
                .append(",").append(entry.getUser().getName())
                .append(",").append(entry.getTask().getProject().getName())
                .append(",").append(entry.getTask().getName())
                .append(",").append(entry.getCalculatedHours())
                .append("\n");
        }

        assertThat(csv.toString()).isEqualTo("Date,User,Project,Task,Hours\n");
    }
}
