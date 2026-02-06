package com.example.timesheet.controller;

import com.example.timesheet.domain.entity.Task;
import com.example.timesheet.domain.entity.TimesheetEntry;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.request.CreateTimesheetRequest;
import com.example.timesheet.dto.request.UpdateTimesheetRequest;
import com.example.timesheet.dto.response.*;
import com.example.timesheet.mapper.TaskMapper;
import com.example.timesheet.mapper.TimesheetMapper;
import com.example.timesheet.security.JwtTokenProvider;
import com.example.timesheet.service.TaskService;
import com.example.timesheet.service.TimesheetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Employee (執行人員) operations.
 * Handles timesheet submission, task viewing, and work hour management.
 * 
 * Endpoints:
 * - GET /api/employee/tasks - View assigned tasks
 * - GET /api/employee/timesheets - View timesheet history
 * - POST /api/employee/timesheets - Submit new timesheet
 * - PUT /api/employee/timesheets/{id} - Edit existing timesheet (within 3 working days)
 */
@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {
    
    private final TaskService taskService;
    private final TimesheetService timesheetService;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final TimesheetMapper timesheetMapper;
    
    /**
     * Get user ID from authentication.
     */
    private Long getUserId(Authentication authentication) {
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"))
            .getId();
    }
    
    /**
     * Get tasks assigned to the current employee.
     * Supports filtering by status and pagination.
     * 
     * @param status Optional task status filter
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param sort Sort field and direction
     * @param authentication Current user authentication
     * @return Page of assigned tasks
     */
    @GetMapping("/tasks")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TaskPageResponse> getMyTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            Authentication authentication) {
        
        Long userId = getUserId(authentication);
        log.info("Employee {} fetching assigned tasks, status: {}", userId, status);
        
        // Parse sort parameter
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        
        TaskPageResponse response = taskService.getTasksByAssignee(userId, status, pageable);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get timesheet entries for the current employee.
     * Supports date range filtering and pagination.
     * 
     * @param startDate Optional start date filter (YYYY-MM-DD)
     * @param endDate Optional end date filter (YYYY-MM-DD)
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param authentication Current user authentication
     * @return Page of timesheet entries
     */
    @GetMapping("/timesheets")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TimesheetPageResponse> getMyTimesheets(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = getUserId(authentication);
        log.info("Employee {} fetching timesheets, dates: {} to {}", userId, startDate, endDate);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "workDate"));
        
        TimesheetPageResponse response = timesheetService.getTimesheetsByUser(
            userId, startDate, endDate, pageable);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Submit a new timesheet entry.
     * Business rules:
     * - Can only submit for past 3 working days
     * - Task must have sufficient remaining hours
     * - Time must be in 0.1 hour increments
     * - Automatic lunch deduction (12:00-13:00)
     * 
     * @param request Timesheet creation request
     * @param authentication Current user authentication
     * @return Created timesheet response
     */
    @PostMapping("/timesheets")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TimesheetResponse> createTimesheet(
            @Valid @RequestBody CreateTimesheetRequest request,
            Authentication authentication) {
        
        Long userId = getUserId(authentication);
        log.info("Employee {} creating timesheet for task {}, date: {}", 
            userId, request.getTaskId(), request.getWorkDate());
        
        TimesheetResponse response = timesheetService.createTimesheet(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Update an existing timesheet entry.
     * Business rules:
     * - Can only edit entries within past 3 working days
     * - Task must have sufficient remaining hours (including hours from this entry)
     * - Time must be in 0.1 hour increments
     * 
     * @param id Timesheet entry ID
     * @param request Timesheet update request
     * @param authentication Current user authentication
     * @return Updated timesheet response
     */
    @PutMapping("/timesheets/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TimesheetResponse> updateTimesheet(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTimesheetRequest request,
            Authentication authentication) {
        
        Long userId = getUserId(authentication);
        log.info("Employee {} updating timesheet {}", userId, id);
        
        TimesheetResponse response = timesheetService.updateTimesheet(id, request, userId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get a specific task detail.
     * Employee can only view tasks assigned to them.
     * 
     * @param taskId Task ID
     * @param authentication Current user authentication
     * @return Task details
     */
    @GetMapping("/tasks/{taskId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TaskDetailResponse> getTaskDetail(
            @PathVariable Long taskId,
            Authentication authentication) {
        
        Long userId = getUserId(authentication);
        log.info("Employee {} fetching task detail {}", userId, taskId);
        
        TaskDetailResponse response = taskService.getTaskById(taskId);
        
        // Verify the task is assigned to this employee
        if (!response.getAssigneeId().equals(userId)) {
            log.warn("Employee {} attempted to access task {} not assigned to them", userId, taskId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Mark a task as completed.
     * Employee can mark their own tasks as completed when work is finished.
     * 
     * @param taskId Task ID
     * @param authentication Current user authentication
     * @return Success message
     */
    @PostMapping("/tasks/{taskId}/complete")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<MessageResponse> completeTask(
            @PathVariable Long taskId,
            Authentication authentication) {
        
        Long userId = getUserId(authentication);
        log.info("Employee {} marking task {} as completed", userId, taskId);
        
        taskService.completeTask(taskId, userId);
        
        return ResponseEntity.ok(MessageResponse.builder()
            .message("任務已標記為完成")
            .build());
    }
}
