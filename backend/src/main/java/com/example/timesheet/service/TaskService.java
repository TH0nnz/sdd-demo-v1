package com.example.timesheet.service;

import com.example.timesheet.domain.entity.Project;
import com.example.timesheet.domain.entity.Task;
import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.ProjectRepository;
import com.example.timesheet.domain.repository.TaskRepository;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.request.CreateTaskRequest;
import com.example.timesheet.dto.request.UpdateTaskRequest;
import com.example.timesheet.dto.response.PageMetadata;
import com.example.timesheet.dto.response.TaskDetailResponse;
import com.example.timesheet.dto.response.TaskPageResponse;
import com.example.timesheet.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for Task management.
 * Handles task creation, updates, deletion, and status management.
 * PM users can create/update/delete tasks, EXECUTIVE can complete tasks.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final ProjectService projectService;
    
    /**
     * Create a new task.
     * Only PM can create tasks.
     *
     * @param request Task creation request
     * @param pmId ID of the PM creating the task
     * @return Created task details
     * @throws IllegalArgumentException if PM not found or project not found
     */
    public TaskDetailResponse createTask(CreateTaskRequest request, Long pmId) {
        log.info("Creating new task: {} in project: {} by PM: {}", 
            request.getName(), request.getProjectId(), pmId);
        
        // Verify user is PM
        User pm = userRepository.findById(pmId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (pm.getRole() != UserRole.PM) {
            throw new IllegalArgumentException("Only PM role can create tasks");
        }
        
        // Verify project exists
        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + request.getProjectId()));
        
        // Verify assignee exists
        User assignee = userRepository.findById(request.getAssigneeId())
            .orElseThrow(() -> new IllegalArgumentException("Assignee not found with id: " + request.getAssigneeId()));
        
        // Create task
        Task task = Task.builder()
            .name(request.getName())
            .description(request.getDescription())
            .estimatedHours(request.getEstimatedHours())
            .usedHours(0.0)
            .status("TODO")
            .project(project)
            .assignee(assignee)
            .build();
        
        task = taskRepository.save(task);
        log.info("Task created with id: {}", task.getId());
        
        // Allocate hours to project
        projectService.allocateHours(project.getId(), request.getEstimatedHours().intValue());
        
        return taskMapper.entityToDetailResponse(task);
    }
    
    /**
     * Get task details by ID.
     *
     * @param taskId Task ID
     * @return Task details
     * @throws IllegalArgumentException if task not found
     */
    public TaskDetailResponse getTaskById(Long taskId) {
        log.debug("Fetching task with id: {}", taskId);
        
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));
        
        return taskMapper.entityToDetailResponse(task);
    }
    
    /**
     * Get tasks with optional filtering and pagination.
     *
     * @param projectId optional project ID filter
     * @param status optional status filter
     * @param assigneeId optional assignee ID filter
     * @param pageable Pagination parameters
     * @return Paginated task list
     */
    public TaskPageResponse getTasks(Long projectId, String status, Long assigneeId, Pageable pageable) {
        log.debug("Fetching tasks with filters - projectId: {}, status: {}, assigneeId: {}", 
            projectId, status, assigneeId);
        
        Page<Task> page;
        
        if (projectId != null && status != null) {
            page = taskRepository.findByProjectIdAndStatus(projectId, status, pageable);
        } else if (projectId != null) {
            page = taskRepository.findByProjectId(projectId, pageable);
        } else if (assigneeId != null && status != null) {
            page = taskRepository.findByAssigneeIdAndStatus(assigneeId, status, pageable);
        } else if (assigneeId != null) {
            page = taskRepository.findByAssigneeId(assigneeId, pageable);
        } else {
            // Fallback to all tasks
            page = taskRepository.findAll(pageable);
        }
        
        return TaskPageResponse.builder()
            .content(taskMapper.entitiesToDetailResponses(page.getContent()))
            .pageInfo(buildPageInfo(page))
            .build();
    }
    
    /**
     * Update task details.
     * Only PM can update tasks.
     *
     * @param taskId Task ID
     * @param request Update request with optimistic locking version
     * @param pmId ID of the PM updating the task
     * @return Updated task details
     * @throws IllegalArgumentException if PM not authorized or version conflict
     */
    public TaskDetailResponse updateTask(Long taskId, UpdateTaskRequest request, Long pmId) {
        log.info("Updating task: {} by PM: {}", taskId, pmId);
        
        // Verify user is PM
        User pm = userRepository.findById(pmId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (pm.getRole() != UserRole.PM) {
            throw new IllegalArgumentException("Only PM role can update tasks");
        }
        
        // Get task with version check
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));
        
        // Verify version matches (optimistic locking)
        if (!task.getVersion().equals(request.getVersion())) {
            throw new IllegalArgumentException(
                String.format("Task version mismatch. Current version: %d, provided version: %d",
                    task.getVersion(), request.getVersion())
            );
        }
        
        // Handle estimated hours change
        if (request.getEstimatedHours() != null && 
            !request.getEstimatedHours().equals(task.getEstimatedHours())) {
            double difference = request.getEstimatedHours() - task.getEstimatedHours();
            if (difference > 0) {
                projectService.allocateHours(task.getProject().getId(), (int) difference);
            } else {
                projectService.deallocateHours(task.getProject().getId(), (int) Math.abs(difference));
            }
            task.setEstimatedHours(request.getEstimatedHours());
        }
        
        // Update only provided fields
        if (request.getName() != null) {
            task.setName(request.getName());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getUsedHours() != null) {
            task.setUsedHours(request.getUsedHours());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new IllegalArgumentException("Assignee not found with id: " + request.getAssigneeId()));
            task.setAssignee(assignee);
        }
        
        task = taskRepository.save(task);
        log.info("Task updated: {}", taskId);
        
        return taskMapper.entityToDetailResponse(task);
    }
    
    /**
     * Delete a task.
     * Only PM can delete tasks.
     *
     * @param taskId Task ID
     * @param pmId ID of the PM deleting the task
     * @throws IllegalArgumentException if PM not authorized or task not found
     */
    public void deleteTask(Long taskId, Long pmId) {
        log.info("Deleting task: {} by PM: {}", taskId, pmId);
        
        // Verify user is PM
        User pm = userRepository.findById(pmId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (pm.getRole() != UserRole.PM) {
            throw new IllegalArgumentException("Only PM role can delete tasks");
        }
        
        // Get task
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));
        
        // Deallocate hours from project
        projectService.deallocateHours(task.getProject().getId(), task.getEstimatedHours().intValue());
        
        // Delete task
        taskRepository.deleteById(taskId);
        log.info("Task deleted: {}", taskId);
    }
    
    /**
     * Mark a task as completed.
     * Only EXECUTIVE can complete tasks.
     *
     * @param taskId Task ID
     * @param executiveId ID of the EXECUTIVE completing the task
     * @return Completed task details
     * @throws IllegalArgumentException if EXECUTIVE not authorized or task not found
     */
    public TaskDetailResponse completeTask(Long taskId, Long executiveId) {
        log.info("Marking task as completed: {} by EXECUTIVE: {}", taskId, executiveId);
        
        // Verify user is EXECUTIVE
        User executive = userRepository.findById(executiveId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (executive.getRole() != UserRole.EXECUTIVE) {
            throw new IllegalArgumentException("Only EXECUTIVE role can complete tasks");
        }
        
        // Get task
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));
        
        // Update task status
        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        
        task = taskRepository.save(task);
        log.info("Task marked as completed: {}", taskId);
        
        return taskMapper.entityToDetailResponse(task);
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
