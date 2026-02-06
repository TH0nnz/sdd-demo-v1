package com.example.timesheet.integration;

import com.example.timesheet.domain.entity.Department;
import com.example.timesheet.domain.entity.Project;
import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.DepartmentRepository;
import com.example.timesheet.domain.repository.ProjectRepository;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.request.CreateProjectRequest;
import com.example.timesheet.dto.request.UpdateProjectRequest;
import com.example.timesheet.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for Project workflows (T055).
 * Tests end-to-end project management functionality with real database.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Project Integration Tests")
@Transactional
class ProjectIntegrationTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

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
    }

    /**
     * T055: Integration test for create project workflow
     * Manager creates a project and assigns it to PM
     */
    @Test
    @DisplayName("T055: Manager can create project and PM can view it")
    void testCreateProjectWorkflow() {
        // Arrange
        CreateProjectRequest request = CreateProjectRequest.builder()
            .name("E-Commerce Platform Revamp")
            .description("Complete redesign of e-commerce platform")
            .totalHours(500)
            .pmId(pm.getId())
            .build();

        // Act
        var createdProject = projectService.createProject(request, manager.getId());

        // Assert
        assertThat(createdProject)
            .isNotNull()
            .extracting("name", "description", "totalHours", "allocatedHours", "remainingHours")
            .containsExactly(
                "E-Commerce Platform Revamp",
                "Complete redesign of e-commerce platform",
                500,
                0,
                500
            );

        assertThat(createdProject.getStatus()).isEqualTo("ACTIVE");
        assertThat(createdProject.getPmId()).isEqualTo(pm.getId());

        // Verify project is persisted
        Project saved = projectRepository.findById(createdProject.getId()).orElseThrow();
        assertThat(saved.getName()).isEqualTo("E-Commerce Platform Revamp");
        assertThat(saved.getTotalHours()).isEqualTo(500);
    }

    /**
     * T055: Manager can update project hours
     */
    @Test
    @DisplayName("T055: Manager can update project hours")
    void testUpdateProjectHours() {
        // Create a project
        CreateProjectRequest createRequest = CreateProjectRequest.builder()
            .name("Original Project")
            .totalHours(500)
            .pmId(pm.getId())
            .build();

        var project = projectService.createProject(createRequest, manager.getId());

        // Update the project
        UpdateProjectRequest updateRequest = UpdateProjectRequest.builder()
            .name("Updated Project")
            .totalHours(600)
            .version(0)
            .build();

        var updated = projectService.updateProject(project.getId(), updateRequest, manager.getId());

        // Assert
        assertThat(updated)
            .extracting("name", "totalHours", "remainingHours")
            .containsExactly(
                "Updated Project",
                600,
                600  // remainingHours should increase by the difference
            );
    }

    /**
     * T055: Manager can close a project
     */
    @Test
    @DisplayName("T055: Manager can close a project")
    void testCloseProjectWorkflow() {
        // Create a project
        CreateProjectRequest createRequest = CreateProjectRequest.builder()
            .name("Project to Close")
            .totalHours(500)
            .pmId(pm.getId())
            .build();

        var project = projectService.createProject(createRequest, manager.getId());
        assertThat(project.getStatus().toString()).isEqualTo("ACTIVE");

        // Close it
        var closed = projectService.closeProject(project.getId(), manager.getId());

        // Assert
        assertThat(closed.getStatus().toString()).isEqualTo("CLOSED");

        // Verify it's persisted
        Project saved = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(saved.getStatus().toString()).isEqualTo("CLOSED");
    }

    /**
     * T055: Cannot close already closed project
     */
    @Test
    @DisplayName("T055: Cannot close already closed project should throw exception")
    void testCannotCloseClosedProjectTwice() {
        // Create and close a project
        CreateProjectRequest createRequest = CreateProjectRequest.builder()
            .name("Project to Close Twice")
            .totalHours(500)
            .pmId(pm.getId())
            .build();

        var project = projectService.createProject(createRequest, manager.getId());
        projectService.closeProject(project.getId(), manager.getId());

        // Try to close again
        assertThatThrownBy(() -> projectService.closeProject(project.getId(), manager.getId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already closed");
    }

    /**
     * T055: Verify optimistic locking prevents concurrent updates
     */
    @Test
    @DisplayName("T055: Optimistic locking prevents concurrent modifications")
    void testOptimisticLocking() {
        // Create a project
        CreateProjectRequest createRequest = CreateProjectRequest.builder()
            .name("Concurrent Test")
            .totalHours(500)
            .pmId(pm.getId())
            .build();

        var project = projectService.createProject(createRequest, manager.getId());

        // Try to update with wrong version
        UpdateProjectRequest updateRequest = UpdateProjectRequest.builder()
            .name("Updated Name")
            .totalHours(600)
            .version(999) // Wrong version
            .build();

        assertThatThrownBy(() -> 
            projectService.updateProject(project.getId(), updateRequest, manager.getId())
        ).isInstanceOf(Exception.class);
    }

    /**
     * T055: Cannot create project without PM
     */
    @Test
    @DisplayName("T055: Cannot create project without valid PM should throw exception")
    void testCreateProjectWithoutValidPm() {
        CreateProjectRequest request = CreateProjectRequest.builder()
            .name("Invalid Project")
            .totalHours(500)
            .pmId(999999L) // Non-existent PM
            .build();

        assertThatThrownBy(() -> projectService.createProject(request, manager.getId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("PM not found");
    }

    /**
     * T055: Only MANAGER can create projects
     */
    @Test
    @DisplayName("T055: Non-manager cannot create project should throw exception")
    void testNonManagerCannotCreateProject() {
        CreateProjectRequest request = CreateProjectRequest.builder()
            .name("Unauthorized Project")
            .totalHours(500)
            .pmId(pm.getId())
            .build();

        assertThatThrownBy(() -> projectService.createProject(request, pm.getId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("MANAGER");
    }
}
