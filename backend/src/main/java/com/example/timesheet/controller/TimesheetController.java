package com.example.timesheet.controller;

import com.example.timesheet.dto.request.CreateTimesheetRequest;
import com.example.timesheet.dto.request.UpdateTimesheetRequest;
import com.example.timesheet.dto.response.MessageResponse;
import com.example.timesheet.dto.response.TimesheetPageResponse;
import com.example.timesheet.dto.response.TimesheetResponse;
import com.example.timesheet.dto.response.WorkHoursCalculationResponse;
import com.example.timesheet.service.TimesheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.time.LocalDate;

/**
 * REST Controller for timesheet management (T125).
 * Handles timesheet CRUD operations and work hours calculation.
 * Only EXECUTIVE role can access these endpoints.
 * 
 * Corresponds to User Story 1 - 工時記錄 (Timesheet Entry MVP)
 */
@RestController
@RequestMapping("/api/timesheets")
@Tag(name = "Timesheets", description = "工時記錄 API (Executive)")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "BearerAuth")
public class TimesheetController {
    
    private final TimesheetService timesheetService;
    
    /**
     * Get all timesheets for current user within optional date range.
     * 
     * @param startDate optional start date
     * @param endDate optional end date
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated timesheet list
     */
    @GetMapping
    @PreAuthorize("hasRole('EXECUTIVE')")
    @Operation(summary = "查詢我的工時記錄")
    public ResponseEntity<TimesheetPageResponse> listMyTimesheets(
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Authentication authentication) {
        
        log.debug("Listing timesheets for user from {} to {}", startDate, endDate);
        
        Long userId = extractUserId(authentication);
        
        // Default to this month if dates not provided
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        Pageable pageable = PageRequest.of(page, size);
        TimesheetPageResponse response = timesheetService.getUserTimesheets(
            userId, startDate, endDate, pageable
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get timesheet details by ID.
     * 
     * @param id the timesheet ID
     * @return timesheet details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EXECUTIVE')")
    @Operation(summary = "查詢工時記錄詳情")
    public ResponseEntity<TimesheetResponse> getTimesheet(@PathVariable Long id) {
        log.debug("Getting timesheet: {}", id);
        
        TimesheetResponse response = timesheetService.getTimesheetById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Calculate work hours preview without saving.
     * Used for real-time calculation in frontend.
     * 
     * @param request the timesheet request with times
     * @return calculation preview
     */
    @PostMapping("/calculate-preview")
    @PreAuthorize("hasRole('EXECUTIVE')")
    @Operation(summary = "工時計算預覽 (含午休自動扣除)")
    public ResponseEntity<WorkHoursCalculationResponse> calculatePreview(
        @Valid @RequestBody CreateTimesheetRequest request) {
        
        log.debug("Calculating preview for times: {} to {}", 
                  request.getStartTime(), request.getEndTime());
        
        WorkHoursCalculationResponse response = timesheetService.calculatePreview(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create a new timesheet entry.
     * Automatically deducts lunch break (12:00-13:00).
     * Updates associated task's used_hours.
     * 
     * @param request the create request
     * @param authentication the authenticated user
     * @return created timesheet with 201 status
     */
    @PostMapping
    @PreAuthorize("hasRole('EXECUTIVE')")
    @Operation(summary = "建立工時記錄 (支援午休自動扣除)")
    public ResponseEntity<TimesheetResponse> createTimesheet(
        @Valid @RequestBody CreateTimesheetRequest request,
        Authentication authentication) {
        
        log.info("Creating timesheet for work date: {}", request.getWorkDate());
        
        Long userId = extractUserId(authentication);
        
        try {
            TimesheetResponse response = timesheetService.createTimesheet(request, userId);
            return ResponseEntity
                .created(URI.create("/api/timesheets/" + response.getId()))
                .body(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Update an existing timesheet entry.
     * Only allowed within 3 working days of work date.
     * Updates associated task's used_hours.
     * 
     * @param id the timesheet ID
     * @param request the update request
     * @param authentication the authenticated user
     * @return updated timesheet
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EXECUTIVE')")
    @Operation(summary = "更新工時記錄 (限三工作天內)")
    public ResponseEntity<TimesheetResponse> updateTimesheet(
        @PathVariable Long id,
        @Valid @RequestBody UpdateTimesheetRequest request,
        Authentication authentication) {
        
        log.info("Updating timesheet: {}", id);
        
        Long userId = extractUserId(authentication);
        
        try {
            TimesheetResponse response = timesheetService.updateTimesheet(id, request, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Delete a timesheet entry.
     * Reverses the hours adjustment to associated task.
     * 
     * @param id the timesheet ID
     * @param authentication the authenticated user
     * @return success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EXECUTIVE')")
    @Operation(summary = "刪除工時記錄")
    public ResponseEntity<MessageResponse> deleteTimesheet(
        @PathVariable Long id,
        Authentication authentication) {
        
        log.info("Deleting timesheet: {}", id);
        
        Long userId = extractUserId(authentication);
        
        try {
            timesheetService.deleteTimesheet(id, userId);
            return ResponseEntity.ok(MessageResponse.builder()
                .message("工時記錄已刪除")
                .build());
        } catch (IllegalArgumentException e) {
            log.error("Deletion error: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Extract user ID from authentication.
     * 
     * @param authentication the authentication object
     * @return the user ID
     */
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            // In a real JWT scenario, this would be extracted from claims
            // For now, we'll use the username as a placeholder
            String username = ((UserDetails) principal).getUsername();
            log.debug("Extracted username from token: {}", username);
            // TODO: Map username to user ID from database
        }
        
        // For testing purposes, if this is a test environment
        String userId = authentication.getName();
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            log.warn("Could not parse user ID from authentication: {}", userId);
            throw new IllegalArgumentException("Invalid user ID in token");
        }
    }
}
