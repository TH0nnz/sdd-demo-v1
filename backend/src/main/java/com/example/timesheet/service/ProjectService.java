package com.example.timesheet.service;

import com.example.timesheet.domain.entity.Project;
import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.enums.ProjectStatus;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.ProjectRepository;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.request.CreateProjectRequest;
import com.example.timesheet.dto.request.UpdateProjectRequest;
import com.example.timesheet.dto.response.PageMetadata;
import com.example.timesheet.dto.response.ProjectDashboardResponse;
import com.example.timesheet.dto.response.ProjectDetailResponse;
import com.example.timesheet.dto.response.ProjectPageResponse;
import com.example.timesheet.dto.response.ProjectSimpleResponse;
import com.example.timesheet.domain.repository.TaskRepository;
import com.example.timesheet.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for Project management.
 * Handles project creation, updates, closure, and access control.
 * Only MANAGER role can perform project operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final TaskRepository taskRepository;
    
    /**
     * Create a new project.
     * Only MANAGER can create projects.
     *
     * @param request Project creation request
     * @param managerId ID of the manager creating the project
     * @return Created project details
     * @throws IllegalArgumentException if manager is not MANAGER role or PM not found
     */
    public ProjectDetailResponse createProject(CreateProjectRequest request, Long managerId) {
        log.info("Creating new project: {} by user: {}", request.getName(), managerId);
        
        // Verify user is MANAGER
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (manager.getRole() != UserRole.MANAGER) {
            throw new IllegalArgumentException("Only MANAGER role can create projects");
        }
        
        // Verify PM exists and is indeed a PM
        User pm = userRepository.findById(request.getPmId())
            .orElseThrow(() -> new IllegalArgumentException("PM not found with id: " + request.getPmId()));
        
        if (pm.getRole() != UserRole.PM) {
            throw new IllegalArgumentException("Assigned user must have PM role");
        }
        
        // Create project
        Project project = Project.builder()
            .name(request.getName())
            .description(request.getDescription())
            .totalHours(request.getTotalHours())
            .allocatedHours(0)
            .status(ProjectStatus.ACTIVE)
            .pm(pm)
            .build();
        
        project = projectRepository.save(project);
        log.info("Project created with id: {}", project.getId());
        
        return projectMapper.entityToDetailResponse(project);
    }
    
    /**
     * Get project details by ID.
     *
     * @param projectId Project ID
     * @return Project details
     * @throws IllegalArgumentException if project not found
     */
    public ProjectDetailResponse getProjectById(Long projectId) {
        log.debug("Fetching project with id: {}", projectId);
        
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));
        
        return projectMapper.entityToDetailResponse(project);
    }
    
    /**
     * Get project dashboard with real-time statistics.
     * Includes task counts, completion rates, and allocation metrics.
     *
     * @param projectId Project ID
     * @return project dashboard response with calculated metrics
     * @throws IllegalArgumentException if project not found
     */
    public ProjectDashboardResponse getProjectDashboard(Long projectId) {
        log.debug("Fetching project dashboard: {}", projectId);
        
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));
        
        // Get completed task count
        long completedTasks = taskRepository.countByProjectIdAndStatus(projectId, "COMPLETED");
        
        // For total task count, we'll need to count all tasks with any status
        // Using findByProjectId to get all and then count
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE);
        org.springframework.data.domain.Page<com.example.timesheet.domain.entity.Task> allTasks = 
            taskRepository.findByProjectId(projectId, pageable);
        long totalTasks = allTasks.getTotalElements();
        
        // Calculate metrics
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        double allocationRate = project.getTotalHours() > 0 ? 
            (double) project.getAllocatedHours() / project.getTotalHours() * 100 : 0;
        
        // Build response
        ProjectDashboardResponse response = projectMapper.entityToDashboardResponse(project);
        response.setTaskCount((int) totalTasks);
        response.setCompletedTaskCount((int) completedTasks);
        response.setCompletionRate(completionRate);
        response.setAllocationRate(allocationRate);
        response.setRemainingHours(project.getRemainingHours());
        
        return response;
    }
    /**
     * Get projects for a PM with pagination.
     *
     * @param pmId PM user ID
     * @param pageable Pagination parameters
     * @return Paginated project list
     */
    public ProjectPageResponse getProjectsByPm(Long pmId, Pageable pageable) {
        log.debug("Fetching projects for PM: {}", pmId);
        
        Page<Project> page = projectRepository.findByPmId(pmId, pageable);
        
        return ProjectPageResponse.builder()
            .content(projectMapper.entitiesToDetailResponses(page.getContent()))
            .pageInfo(buildPageInfo(page))
            .build();
    }
    
    /**
     * Get all active projects with pagination.
     *
     * @param pageable Pagination parameters
     * @return Paginated active project list
     */
    public ProjectPageResponse getActiveProjects(Pageable pageable) {
        log.debug("Fetching active projects");
        
        Page<Project> page = projectRepository.findByStatus(ProjectStatus.ACTIVE, pageable);
        
        return ProjectPageResponse.builder()
            .content(projectMapper.entitiesToDetailResponses(page.getContent()))
            .pageInfo(buildPageInfo(page))
            .build();
    }
    
    /**
     * Update project details.
     * Only MANAGER can update projects.
     *
     * @param projectId Project ID
     * @param request Update request with optimistic locking version
     * @param managerId ID of the manager updating the project
     * @return Updated project details
     * @throws IllegalArgumentException if manager is not MANAGER or version conflict
     */
    public ProjectDetailResponse updateProject(Long projectId, UpdateProjectRequest request, Long managerId) {
        log.info("Updating project: {} by user: {}", projectId, managerId);
        
        // Verify user is MANAGER
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (manager.getRole() != UserRole.MANAGER) {
            throw new IllegalArgumentException("Only MANAGER role can update projects");
        }
        
        // Get project with version check
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));
        
        // Verify version matches (optimistic locking)
        if (!project.getVersion().equals(request.getVersion())) {
            throw new IllegalArgumentException(
                String.format("Project version mismatch. Current version: %d, provided version: %d",
                    project.getVersion(), request.getVersion())
            );
        }
        
        // Update only provided fields
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getTotalHours() != null) {
            project.setTotalHours(request.getTotalHours());
        }
        
        project = projectRepository.save(project);
        log.info("Project updated: {}", projectId);
        
        return projectMapper.entityToDetailResponse(project);
    }
    
    /**
     * Close a project.
     * Locked projects cannot have new allocations.
     * Only MANAGER can close projects.
     *
     * @param projectId Project ID
     * @param managerId ID of the manager closing the project
     * @return Closed project details
     * @throws IllegalArgumentException if manager is not MANAGER or project already closed
     */
    public ProjectDetailResponse closeProject(Long projectId, Long managerId) {
        log.info("Closing project: {} by user: {}", projectId, managerId);
        
        // Verify user is MANAGER
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (manager.getRole() != UserRole.MANAGER) {
            throw new IllegalArgumentException("Only MANAGER role can close projects");
        }
        
        // Get project
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));
        
        // Verify not already closed
        if (project.getStatus() == ProjectStatus.CLOSED) {
            throw new IllegalArgumentException("Project is already closed");
        }
        
        // Close project
        project.setStatus(ProjectStatus.CLOSED);
        project = projectRepository.save(project);
        log.info("Project closed: {}", projectId);
        
        return projectMapper.entityToDetailResponse(project);
    }
    
    /**
     * Allocate hours to a project (used by services when tasks are created).
     * Internal method - increments allocatedHours.
     *
     * @param projectId Project ID
     * @param hours Hours to allocate
     * @throws IllegalArgumentException if project not found or insufficient remaining hours
     */
    @Transactional
    public void allocateHours(Long projectId, Integer hours) {
        log.debug("Allocating {} hours to project: {}", hours, projectId);
        
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));
        
        if (project.getStatus() == ProjectStatus.CLOSED) {
            throw new IllegalArgumentException("Cannot allocate hours to a closed project");
        }
        
        int newAllocated = project.getAllocatedHours() + hours;
        if (newAllocated > project.getTotalHours()) {
            throw new IllegalArgumentException(
                String.format("Insufficient project hours. Remaining: %d, Requested: %d",
                    project.getRemainingHours(), hours)
            );
        }
        
        project.setAllocatedHours(newAllocated);
        projectRepository.save(project);
    }
    
    /**
     * Deallocate hours from a project (used by services when tasks are deleted).
     * Internal method - decrements allocatedHours.
     *
     * @param projectId Project ID
     * @param hours Hours to deallocate
     * @throws IllegalArgumentException if project not found
     */
    @Transactional
    public void deallocateHours(Long projectId, Integer hours) {
        log.debug("Deallocating {} hours from project: {}", hours, projectId);
        
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));
        
        int newAllocated = Math.max(0, project.getAllocatedHours() - hours);
        project.setAllocatedHours(newAllocated);
        projectRepository.save(project);
    }
    
    /**
     * Internal method to add hours to project when time request is approved.
     * Called by TimeRequestService.
     *
     * @param projectId Project ID
     * @param hours Hours to add
     */
    @Transactional
    public void addHours(Long projectId, Integer hours) {
        log.info("Adding {} hours to project: {}", hours, projectId);
        
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));
        
        project.setTotalHours(project.getTotalHours() + hours);
        projectRepository.save(project);
    }
    
    // ========== Helper Methods ==========
    
    private PageMetadata buildPageInfo(Page<?> page) {
        return PageMetadata.builder()
            .currentPage(page.getNumber() + 1)  // Convert 0-indexed to 1-indexed
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}
