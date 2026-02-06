package com.example.timesheet.controller;

import com.example.timesheet.dto.request.CreateTaskRequest;
import com.example.timesheet.dto.request.UpdateTaskRequest;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.response.MessageResponse;
import com.example.timesheet.dto.response.TaskDetailResponse;
import com.example.timesheet.dto.response.TaskPageResponse;
import com.example.timesheet.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * REST Controller for task management.
 * Handles task CRUD operations and task lifecycle management.
 * PM users have access to create/update/delete endpoints.
 * EXECUTIVE users can complete tasks.
 * 
 * Corresponds to User Story 2 - 任务管理 (Task Management)
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "任務管理 API (PM/Executive)")
@Slf4j
@SecurityRequirement(name = "BearerAuth")
public class TaskController {
    
    private final TaskService taskService;
    private final UserRepository userRepository;
    
    public TaskController(TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }
    
    /**
     * Get all tasks with optional filtering and pagination.
     * 
     * @param projectId optional project ID filter
     * @param status optional status filter
     * @param assigneeId optional assignee ID filter
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated task list
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PM', 'EXECUTIVE')")
    @Operation(summary = "查詢任務列表")
    public ResponseEntity<TaskPageResponse> listTasks(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Listing tasks with filters - projectId: {}, status: {}, assigneeId: {}, page: {}, size: {}", 
            projectId, status, assigneeId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        TaskPageResponse response = taskService.getTasks(projectId, status, assigneeId, pageable);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get task details by ID.
     * 
     * @param taskId Task ID
     * @return task details
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('PM', 'EXECUTIVE')")
    @Operation(summary = "取得任務詳細資訊")
    public ResponseEntity<TaskDetailResponse> getTask(@PathVariable Long taskId) {
        log.debug("Fetching task: {}", taskId);
        
        TaskDetailResponse response = taskService.getTaskById(taskId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create a new task.
     * Only PM can create tasks.
     * 
     * @param request task creation details
     * @param authentication current authenticated user
     * @return created task with 201 CREATED status
     */
    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @Operation(summary = "建立新任務")
    public ResponseEntity<TaskDetailResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {
        
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"))
            .getId();
        
        log.info("Creating task: {} in project: {} by user: {}", 
            request.getName(), request.getProjectId(), userId);
        
        TaskDetailResponse response = taskService.createTask(request, userId);
        
        return ResponseEntity
            .created(URI.create("/api/tasks/" + response.getId()))
            .body(response);
    }
    
    /**
     * Update task details.
     * Only PM can update tasks.
     * Supports optimistic locking via version field.
     * 
     * @param taskId Task ID
     * @param request update request with version for optimistic locking
     * @param authentication current authenticated user
     * @return updated task details
     */
    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('PM')")
    @Operation(summary = "更新任務資訊")
    public ResponseEntity<TaskDetailResponse> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication) {
        
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"))
            .getId();
        
        log.info("Updating task: {} by user: {}", taskId, userId);
        
        TaskDetailResponse response = taskService.updateTask(taskId, request, userId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a task.
     * Only PM can delete tasks.
     * Deallocates hours from the project.
     * 
     * @param taskId Task ID
     * @param authentication current authenticated user
     * @return no content response
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('PM')")
    @Operation(summary = "刪除任務")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long taskId,
            Authentication authentication) {
        
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"))
            .getId();
        
        log.info("Deleting task: {} by user: {}", taskId, userId);
        
        taskService.deleteTask(taskId, userId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Mark a task as completed.
     * Only EXECUTIVE can complete tasks.
     * Sets completedAt timestamp.
     * 
     * @param taskId Task ID
     * @param authentication current authenticated user
     * @return completed task details
     */
    @PostMapping("/{taskId}/complete")
    @PreAuthorize("hasRole('EXECUTIVE')")
    @Operation(summary = "完成任務")
    public ResponseEntity<TaskDetailResponse> completeTask(
            @PathVariable Long taskId,
            Authentication authentication) {
        
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"))
            .getId();
        
        log.info("Completing task: {} by user: {}", taskId, userId);
        
        TaskDetailResponse response = taskService.completeTask(taskId, userId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Exception handler for IllegalArgumentException.
     * Maps business logic errors to appropriate HTTP status codes.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Business logic error: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(MessageResponse.error(ex.getMessage()));
    }
}
