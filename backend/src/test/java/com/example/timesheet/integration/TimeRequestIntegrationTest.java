package com.example.timesheet.integration;

import com.example.timesheet.domain.entity.Department;
import com.example.timesheet.domain.entity.Project;
import com.example.timesheet.domain.entity.TimeRequest;
import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.DepartmentRepository;
import com.example.timesheet.domain.repository.ProjectRepository;
import com.example.timesheet.domain.repository.TimeRequestRepository;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.request.ApproveTimeRequestRequest;
import com.example.timesheet.dto.request.CreateProjectRequest;
import com.example.timesheet.service.ProjectService;
import com.example.timesheet.service.TimeRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for TimeRequest workflows (T056).
 * Tests end-to-end time request approval functionality with real database.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("TimeRequest Integration Tests")
@Transactional
class TimeRequestIntegrationTest {

    @Autowired
    private TimeRequestService timeRequestService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TimeRequestRepository timeRequestRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private User manager;
    private User pm;
    private Department department;
    private Project project;

    @BeforeEach
    void setup() {
        department = departmentRepository.save(Department.builder()
            .name("Research & Development")
            .build());

        manager = userRepository.save(User.builder()
            .name("Manager Zhang")
            .email("manager@example.com")
            .passwordHash("hashed_password")
            .role(UserRole.MANAGER)
            .department(department)
            .active(true)
            .build());

        pm = userRepository.save(User.builder()
            .name("PM Li")
            .email("pm@example.com")
            .passwordHash("hashed_password")
            .role(UserRole.PM)
            .department(department)
            .active(true)
            .build());

        // Create a project
        CreateProjectRequest projectRequest = CreateProjectRequest.builder()
            .name("Test Project")
            .totalHours(100)
            .pmId(pm.getId())
            .build();

        var projectDto = projectService.createProject(projectRequest, manager.getId());
        project = projectRepository.findById(projectDto.getId()).orElseThrow();
    }

    /**
     * T056: Integration test for approve time request workflow
     * PM requests additional hours, Manager approves it, Project hours increase
     */
    @Test
    @DisplayName("T056: Manager can approve time request and project hours increase")
    void testApproveTimeRequestWorkflow() {
        // Arrange: Create a time request
        TimeRequest timeRequest = TimeRequest.builder()
            .project(project)
            .requestedHours(50)
            .reason("Additional development time needed")
            .requester(pm)
            .build();

        timeRequest = timeRequestRepository.save(timeRequest);

        int originalTotalHours = project.getTotalHours();

        // Act: Manager approves the request
        ApproveTimeRequestRequest approvalRequest = ApproveTimeRequestRequest.builder()
            .approved(true)
            .reason("Approved for development")
            .build();

        var approvedRequest = timeRequestService.approveTimeRequest(
            timeRequest.getId(),
            approvalRequest,
            manager.getId()
        );

        // Assert
        assertThat(approvedRequest.getStatus().toString()).isEqualTo("APPROVED");
        assertThat(approvedRequest.getApprovedAt()).isNotNull();

        // Verify project hours increased
        Project updatedProject = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(updatedProject.getTotalHours())
            .isEqualTo(originalTotalHours + 50);
    }

    /**
     * T056: Manager can reject time request
     */
    @Test
    @DisplayName("T056: Manager can reject time request")
    void testRejectTimeRequest() {
        // Create a time request
        TimeRequest timeRequest = TimeRequest.builder()
            .project(project)
            .requestedHours(50)
            .reason("Additional hours needed")
            .requester(pm)
            .build();

        timeRequest = timeRequestRepository.save(timeRequest);
        int originalTotalHours = project.getTotalHours();

        // Manager rejects the request
        ApproveTimeRequestRequest rejectionRequest = ApproveTimeRequestRequest.builder()
            .approved(false)
            .reason("Budget constraints")
            .build();

        var rejectedRequest = timeRequestService.approveTimeRequest(
            timeRequest.getId(),
            rejectionRequest,
            manager.getId()
        );

        // Assert
        assertThat(rejectedRequest.getStatus().toString()).isEqualTo("REJECTED");

        // Verify project hours did NOT change
        Project unchanged = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(unchanged.getTotalHours()).isEqualTo(originalTotalHours);
    }

    /**
     * T056: Cannot approve non-existent time request
     */
    @Test
    @DisplayName("T056: Approving non-existent time request should throw exception")
    void testApproveNonExistentTimeRequest() {
        ApproveTimeRequestRequest approvalRequest = ApproveTimeRequestRequest.builder()
            .approved(true)
            .reason("Approval")
            .build();

        assertThatThrownBy(() -> 
            timeRequestService.approveTimeRequest(999999L, approvalRequest, manager.getId())
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not found");
    }

    /**
     * T056: Cannot approve already approved request
     */
    @Test
    @DisplayName("T056: Cannot approve already approved request should throw exception")
    void testCannotApproveAlreadyApprovedRequest() {
        // Create and approve a time request
        TimeRequest timeRequest = TimeRequest.builder()
            .project(project)
            .requestedHours(50)
            .reason("Additional hours")
            .requester(pm)
            .build();

        timeRequest = timeRequestRepository.save(timeRequest);
        Long requestId = timeRequest.getId();

        ApproveTimeRequestRequest firstApproval = ApproveTimeRequestRequest.builder()
            .approved(true)
            .reason("Approval")
            .build();

        // First approval should succeed
        timeRequestService.approveTimeRequest(requestId, firstApproval, manager.getId());

        // Second approval should fail
        ApproveTimeRequestRequest secondApproval = ApproveTimeRequestRequest.builder()
            .approved(true)
            .reason("Second approval")
            .build();

        assertThatThrownBy(() ->
            timeRequestService.approveTimeRequest(requestId, secondApproval, manager.getId())
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already");
    }
}
