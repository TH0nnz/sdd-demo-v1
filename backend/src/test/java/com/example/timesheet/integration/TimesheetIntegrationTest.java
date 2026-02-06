package com.example.timesheet.integration;

import com.example.timesheet.domain.entity.*;
import com.example.timesheet.domain.enums.ProjectStatus;
import com.example.timesheet.domain.enums.TaskStatus;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.*;
import com.example.timesheet.dto.request.CreateTimesheetRequest;
import com.example.timesheet.dto.request.UpdateTimesheetRequest;
import com.example.timesheet.dto.response.TimesheetResponse;
import com.example.timesheet.service.TimesheetService;
import com.example.timesheet.util.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for Timesheet workflows (T114-T117).
 * Tests end-to-end timesheet creation, update, and 3-working-day edit window.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Timesheet Integration Tests")
@Transactional
class TimesheetIntegrationTest {

    @Autowired
    private TimesheetService timesheetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private User executive;
    private User pm;
    private Department department;
    private Project project;
    private Task task;

    @BeforeEach
    void setup() {
        DateUtils.clearPublicHolidays();
        
        department = departmentRepository.save(Department.builder()
            .name("Research & Development")
            .build());

        pm = userRepository.save(User.builder()
            .name("PM Li")
            .email("pm@example.com")
            .passwordHash("hashed_password")
            .role(UserRole.PM)
            .department(department)
            .active(true)
            .build());

        executive = userRepository.save(User.builder()
            .name("Executive Zhang")
            .email("executive@example.com")
            .passwordHash("hashed_password")
            .role(UserRole.EXECUTIVE)
            .department(department)
            .active(true)
            .build());

        project = projectRepository.save(Project.builder()
            .name("Q1 Project")
            .description("First quarter project")
            .status(ProjectStatus.ACTIVE)
            .pm(pm)
            .totalHours(100)
            .build());

        task = taskRepository.save(Task.builder()
            .name("Implementation Task")
            .description("Core implementation")
            .estimatedHours(40.0)
            .usedHours(0.0)
            .status(TaskStatus.IN_PROGRESS)
            .project(project)
            .assignee(executive)
            .version(0)
            .build());
    }

    @Test
    @DisplayName("T114: Should create timesheet with normal hours (no lunch deduction)")
    void testCreateTimesheetNormalHours() {
        // Arrange
        CreateTimesheetRequest request = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(11, 0))
            .build();

        // Act
        TimesheetResponse response = timesheetService.createTimesheet(request, executive.getId());

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCalculatedHours()).isEqualByComparingTo(new BigDecimal("2.0"));
        assertThat(response.getLunchDeducted()).isFalse();
        assertThat(response.getTaskId()).isEqualTo(task.getId());
        
        // Verify task used_hours was updated
        Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(updatedTask.getUsedHours()).isEqualByComparingTo(2.0);
    }

    @Test
    @DisplayName("T115: Should create timesheet with automatic lunch deduction (12:00-13:00)")
    void testCreateTimesheetWithLunchDeduction() {
        // Arrange: work from 10:00 to 14:00 (overlaps lunch 12:00-13:00)
        CreateTimesheetRequest request = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(14, 0))
            .build();

        // Act
        TimesheetResponse response = timesheetService.createTimesheet(request, executive.getId());

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCalculatedHours()).isEqualByComparingTo(new BigDecimal("3.0")); // 4 hours - 1 lunch
        assertThat(response.getLunchDeducted()).isTrue();
        
        // Verify task used_hours was updated (4 - 1 = 3)
        Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(updatedTask.getUsedHours()).isEqualByComparingTo(3.0);
    }

    @Test
    @DisplayName("T116: Should allow edit timesheet within 3 working days")
    void testEditTimesheetWithin3WorkingDays() {
        // Arrange: Create a timesheet 1 working day ago
        LocalDate workDate = DateUtils.subtractWorkingDays(LocalDate.now(), 1);
        
        CreateTimesheetRequest createRequest = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(workDate)
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(11, 0))
            .build();

        TimesheetResponse created = timesheetService.createTimesheet(createRequest, executive.getId());
        Long timesheetId = created.getId();

        // Act: Update the timesheet
        UpdateTimesheetRequest updateRequest = UpdateTimesheetRequest.builder()
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(13, 0)) // Change to 4 hours (1 hour lunch deduction = 3 hours)
            .build();

        TimesheetResponse updated = timesheetService.updateTimesheet(timesheetId, updateRequest, executive.getId());

        // Assert
        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(timesheetId);
        assertThat(updated.getCalculatedHours()).isEqualByComparingTo(new BigDecimal("3.0")); // 4 hours - 1 lunch
        assertThat(updated.getLunchDeducted()).isTrue();
    }

    @Test
    @DisplayName("T117: Should reject edit when beyond 3 working days")
    void testRejectEditBeyond3WorkingDays() {
        // Arrange: Create a timesheet 5 working days ago (beyond 3-day window)
        LocalDate workDate = DateUtils.subtractWorkingDays(LocalDate.now(), 5);
        
        CreateTimesheetRequest createRequest = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(workDate)
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(11, 0))
            .build();

        TimesheetResponse created = timesheetService.createTimesheet(createRequest, executive.getId());
        Long timesheetId = created.getId();

        // Act & Assert: Attempt to update should fail
        UpdateTimesheetRequest updateRequest = UpdateTimesheetRequest.builder()
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(12, 0))
            .build();

        assertThatThrownBy(() -> 
            timesheetService.updateTimesheet(timesheetId, updateRequest, executive.getId())
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be edited");
    }

    @Test
    @DisplayName("Should verify timesheet belongs to current user before allowing edit")
    void testOnlyOwnerCanEditTimesheet() {
        // Arrange: Create timesheet for executive
        CreateTimesheetRequest request = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(11, 0))
            .build();

        TimesheetResponse created = timesheetService.createTimesheet(request, executive.getId());

        // Act & Assert: Try to update as different user (PM)
        UpdateTimesheetRequest updateRequest = UpdateTimesheetRequest.builder()
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(12, 0))
            .build();

        assertThatThrownBy(() -> 
            timesheetService.updateTimesheet(created.getId(), updateRequest, pm.getId())
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should delete timesheet and adjust task used_hours")
    void testDeleteTimesheet() {
        // Arrange: Create timesheet
        CreateTimesheetRequest request = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(14, 0)) // 3 hours after lunch deduction
            .build();

        TimesheetResponse created = timesheetService.createTimesheet(request, executive.getId());
        Task taskAfterCreate = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(taskAfterCreate.getUsedHours()).isEqualByComparingTo(3.0);

        // Act
        timesheetService.deleteTimesheet(created.getId(), executive.getId());

        // Assert: Task used_hours should be reduced
        Task taskAfterDelete = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(taskAfterDelete.getUsedHours()).isEqualByComparingTo(0.0);
    }

    @Test
    @DisplayName("Should handle multiple timesheets for same task")
    void testMultipleTimesheetsForSameTask() {
        // Arrange & Act: Create multiple timesheets
        CreateTimesheetRequest request1 = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(LocalDate.now().minusDays(2))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(11, 0)) // 2 hours
            .build();

        CreateTimesheetRequest request2 = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(14, 0)) // 3 hours (after lunch deduction)
            .build();

        timesheetService.createTimesheet(request1, executive.getId());
        timesheetService.createTimesheet(request2, executive.getId());

        // Assert: Task used_hours should be cumulative
        Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(updatedTask.getUsedHours()).isEqualByComparingTo(5.0); // 2 + 3
    }

    @Test
    @DisplayName("Should allow creation when hour calculation results in valid 0.5 increments after rounding")
    void testValidateHourIncrements() {
        // Arrange: Work from 9:00 to 9:45 (0.75 hours, rounds to 0.5 or 1.0)
        CreateTimesheetRequest request = CreateTimesheetRequest.builder()
            .taskId(task.getId())
            .workDate(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(9, 45)) // 0.75 hours
            .build();

        // Act: Should succeed because WorkHoursCalculator rounds 0.75 to 1.0
        TimesheetResponse response = timesheetService.createTimesheet(request, executive.getId());

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCalculatedHours()).isEqualByComparingTo(new BigDecimal("1.0"));
    }
}
