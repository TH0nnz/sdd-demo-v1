package com.example.timesheet.controller;

import com.example.timesheet.dto.request.CreateProjectRequest;
import com.example.timesheet.dto.request.UpdateProjectRequest;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.response.MessageResponse;
import com.example.timesheet.dto.response.ProjectDashboardResponse;
import com.example.timesheet.dto.response.ProjectDetailResponse;
import com.example.timesheet.dto.response.ProjectPageResponse;
import com.example.timesheet.service.ProjectService;
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
 * REST Controller for project management.
 * Handles project CRUD operations and lifecycle management.
 * Only MANAGER users have access to these endpoints.
 * 
 * Corresponds to User Story 3 - 管理層管理專案與分派時數
 */
@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "專案管理 API (Manager)")
@Slf4j
@SecurityRequirement(name = "BearerAuth")
public class ProjectController {
    
    private final ProjectService projectService;
    private final UserRepository userRepository;
    
    public ProjectController(ProjectService projectService, UserRepository userRepository) {
        this.projectService = projectService;
        this.userRepository = userRepository;
    }
    
    /**
     * Get all active projects with optional filtering and pagination.
     * 
     * @param pmId optional PM filter
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated project list
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'PM')")
    @Operation(summary = "查詢專案列表")
    public ResponseEntity<ProjectPageResponse> listProjects(
            @RequestParam(required = false) Long pmId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Listing projects with filters - pmId: {}, page: {}, size: {}", pmId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        ProjectPageResponse response;
        
        if (pmId != null) {
            response = projectService.getProjectsByPm(pmId, pageable);
        } else {
            response = projectService.getActiveProjects(pageable);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get project details by ID.
     * 
     * @param projectId Project ID
     * @return project details
     */
    @GetMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'PM')")
    @Operation(summary = "取得專案詳細資訊")
    public ResponseEntity<ProjectDetailResponse> getProject(@PathVariable Long projectId) {
        log.debug("Fetching project: {}", projectId);
        
        ProjectDetailResponse response = projectService.getProjectById(projectId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get project dashboard with real-time statistics.
     * Includes task counts, completion rates, and allocation metrics.
     * 
     * @param projectId Project ID
     * @return project dashboard response with calculated metrics
     */
    @GetMapping("/{projectId}/dashboard")
    @PreAuthorize("hasAnyRole('MANAGER', 'PM')")
    @Operation(summary = "取得專案儀表板統計")
    public ResponseEntity<ProjectDashboardResponse> getProjectDashboard(@PathVariable Long projectId) {
        log.debug("Fetching project dashboard: {}", projectId);
        
        ProjectDashboardResponse response = projectService.getProjectDashboard(projectId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create a new project.
     * Only MANAGER can create projects.
     * 
     * @param request project creation details
     * @param authentication current authenticated user
     * @return created project with 201 CREATED status
     */
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "建立新專案")
    public ResponseEntity<ProjectDetailResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"))
            .getId();
        
        log.info("Creating project: {} by user: {}", request.getName(), userId);
        
        ProjectDetailResponse response = projectService.createProject(request, userId);
        
        return ResponseEntity
            .created(URI.create("/api/projects/" + response.getId()))
            .body(response);
    }
    
    /**
     * Update project details.
     * Only MANAGER can update projects.
     * Supports optimistic locking via version field.
     * 
     * @param projectId Project ID
     * @param request update request with version for optimistic locking
     * @param authentication current authenticated user
     * @return updated project details
     */
    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "更新專案資訊")
    public ResponseEntity<ProjectDetailResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication) {
        
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"))
            .getId();
        
        log.info("Updating project: {} by user: {}", projectId, userId);
        
        ProjectDetailResponse response = projectService.updateProject(projectId, request, userId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Close a project.
     * Closed projects cannot have new allocations.
     * Only MANAGER can close projects.
     * 
     * @param projectId Project ID
     * @param authentication current authenticated user
     * @return closed project details
     */
    @PostMapping("/{projectId}/close")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "關閉專案")
    public ResponseEntity<ProjectDetailResponse> closeProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"))
            .getId();
        
        log.info("Closing project: {} by user: {}", projectId, userId);
        
        ProjectDetailResponse response = projectService.closeProject(projectId, userId);
        
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
