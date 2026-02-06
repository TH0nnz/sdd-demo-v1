package com.example.timesheet.service;

import com.example.timesheet.domain.entity.Task;
import com.example.timesheet.domain.entity.TimesheetEntry;
import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.repository.TaskRepository;
import com.example.timesheet.domain.repository.TimesheetRepository;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.request.CreateTimesheetRequest;
import com.example.timesheet.dto.request.UpdateTimesheetRequest;
import com.example.timesheet.dto.response.PageMetadata;
import com.example.timesheet.dto.response.TimesheetPageResponse;
import com.example.timesheet.dto.response.TimesheetResponse;
import com.example.timesheet.dto.response.WorkHoursCalculationResponse;
import com.example.timesheet.mapper.TimesheetMapper;
import com.example.timesheet.util.DateUtils;
import com.example.timesheet.util.WorkHoursCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service for Timesheet management (T121).
 * Handles timesheet creation, updates, deletion, and work hours calculations.
 * 
 * Business Rules:
 * - Auto lunch deduction: 12:00-13:00 (1 hour)
 * - Edit window: 3 working days
 * - Time increments: 0.5 hours only
 * - Auto-update task.used_hours when created/updated/deleted
 * - PM notification when hours exceed task estimated hours
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TimesheetService {
    
    private static final int EDIT_WINDOW_WORKING_DAYS = 3;
    
    private final TimesheetRepository timesheetRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TimesheetMapper timesheetMapper;
    
    /**
     * Calculate work hours preview without saving.
     * Used for real-time calculation in frontend.
     * 
     * @param request the timesheet request
     * @return calculation preview response
     */
    public WorkHoursCalculationResponse calculatePreview(CreateTimesheetRequest request) {
        log.debug("Calculating preview for times: {} to {}", 
                  request.getStartTime(), request.getEndTime());
        
        try {
            // Validate end time after start time
            if (request.getEndTime().isBefore(request.getStartTime()) || 
                request.getEndTime().equals(request.getStartTime())) {
                return WorkHoursCalculationResponse.builder()
                    .valid(false)
                    .validationMessage("結束時間必須晚於開始時間")
                    .build();
            }
            
            // Calculate work hours
            WorkHoursCalculator.WorkHoursResult result = 
                WorkHoursCalculator.calculate(request.getStartTime(), request.getEndTime());
            
            // Validate hour increments
            if (!WorkHoursCalculator.isValidHoursIncrement(result.hours())) {
                return WorkHoursCalculationResponse.builder()
                    .valid(false)
                    .validationMessage("工時必須是 0.5 小時的倍數")
                    .calculatedHours(result.hours())
                    .lunchDeducted(result.lunchDeducted())
                    .lunchHours(result.lunchHours())
                    .build();
            }
            
            return WorkHoursCalculationResponse.builder()
                .calculatedHours(result.hours())
                .lunchDeducted(result.lunchDeducted())
                .lunchHours(result.lunchHours())
                .message(result.getMessage())
                .valid(true)
                .build();
        } catch (Exception e) {
            log.error("Error calculating hours preview", e);
            return WorkHoursCalculationResponse.builder()
                .valid(false)
                .validationMessage(e.getMessage())
                .build();
        }
    }
    
    /**
     * Create a new timesheet entry.
     * 
     * @param request the create request
     * @param userId the user ID (executive)
     * @return created timesheet response
     */
    public TimesheetResponse createTimesheet(CreateTimesheetRequest request, Long userId) {
        log.info("Creating timesheet for user {} on date {}", 
                 userId, request.getWorkDate());
        
        // Verify user exists and is executive
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Verify task exists
        Task task = taskRepository.findById(request.getTaskId())
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        
        // Validate times
        if (request.getEndTime().isBefore(request.getStartTime()) || 
            request.getEndTime().equals(request.getStartTime())) {
            throw new IllegalArgumentException("結束時間必須晚於開始時間");
        }
        
        // Calculate work hours
        WorkHoursCalculator.WorkHoursResult result = 
            WorkHoursCalculator.calculate(request.getStartTime(), request.getEndTime());
        
        // Validate hour increments
        if (!WorkHoursCalculator.isValidHoursIncrement(result.hours())) {
            throw new IllegalArgumentException("工時必須是 0.5 小時的倍數，計算得: " + result.hours());
        }
        
        // Create timesheet entry
        TimesheetEntry entry = TimesheetEntry.builder()
            .user(user)
            .task(task)
            .workDate(request.getWorkDate())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .calculatedHours(result.hours())
            .lunchDeducted(result.lunchDeducted())
            .build();
        
        entry = timesheetRepository.save(entry);
        log.info("Created timesheet entry {} with {} hours", entry.getId(), result.hours());
        
        // Update task used_hours
        task.setUsedHours(task.getUsedHours() + result.hours().doubleValue());
        taskRepository.save(task);
        log.info("Updated task {} used_hours to {}", task.getId(), task.getUsedHours());
        
        // Check if hours exceed estimated (PM notification)
        if (task.getUsedHours() > task.getEstimatedHours()) {
            log.warn("Task {} hours ({}) exceed estimated ({}). PM should be notified.",
                    task.getId(), task.getUsedHours(), task.getEstimatedHours());
            // TODO: Send PM notification when notification service is available
        }
        
        return timesheetMapper.toResponse(entry);
    }
    
    /**
     * Update an existing timesheet entry.
     * Only allowed within 3 working days of work date.
     * 
     * @param timesheetId the timesheet ID
     * @param request the update request
     * @param userId the user ID (must be owner)
     * @return updated timesheet response
     */
    public TimesheetResponse updateTimesheet(Long timesheetId, UpdateTimesheetRequest request, Long userId) {
        log.info("Updating timesheet {} for user {}", timesheetId, userId);
        
        // Verify user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Find timesheet
        TimesheetEntry entry = timesheetRepository.findById(timesheetId)
            .orElseThrow(() -> new IllegalArgumentException("Timesheet not found"));
        
        // Verify ownership
        if (!entry.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot edit timesheet of another user");
        }
        
        // Validate edit window (3 working days)
        if (!DateUtils.isWithinWorkingDays(entry.getWorkDate(), EDIT_WINDOW_WORKING_DAYS)) {
            throw new IllegalArgumentException(
                "Timesheet cannot be edited. Only entries from the last 3 working days can be edited."
            );
        }
        
        // Validate times
        if (request.getEndTime().isBefore(request.getStartTime()) || 
            request.getEndTime().equals(request.getStartTime())) {
            throw new IllegalArgumentException("結束時間必須晚於開始時間");
        }
        
        // Calculate new work hours
        WorkHoursCalculator.WorkHoursResult newResult = 
            WorkHoursCalculator.calculate(request.getStartTime(), request.getEndTime());
        
        // Validate hour increments
        if (!WorkHoursCalculator.isValidHoursIncrement(newResult.hours())) {
            throw new IllegalArgumentException("工時必須是 0.5 小時的倍數");
        }
        
        // Get old hours for task update
        BigDecimal oldHours = entry.getCalculatedHours();
        
        // Update entry
        entry.setStartTime(request.getStartTime());
        entry.setEndTime(request.getEndTime());
        entry.setCalculatedHours(newResult.hours());
        entry.setLunchDeducted(newResult.lunchDeducted());
        
        entry = timesheetRepository.save(entry);
        log.info("Updated timesheet {} with new hours: {}", entry.getId(), newResult.hours());
        
        // Update task used_hours
        Task task = entry.getTask();
        double oldUsedHours = task.getUsedHours();
        double newUsedHours = oldUsedHours - oldHours.doubleValue() + newResult.hours().doubleValue();
        task.setUsedHours(newUsedHours);
        taskRepository.save(task);
        log.info("Updated task {} used_hours from {} to {}", 
                 task.getId(), oldUsedHours, newUsedHours);
        
        // Check if hours exceed estimated (PM notification)
        if (newUsedHours > task.getEstimatedHours()) {
            log.warn("Task {} hours ({}) exceed estimated ({}). PM should be notified.",
                    task.getId(), newUsedHours, task.getEstimatedHours());
        }
        
        return timesheetMapper.toResponse(entry);
    }
    
    /**
     * Delete a timesheet entry.
     * 
     * @param timesheetId the timesheet ID
     * @param userId the user ID (must be owner)
     */
    public void deleteTimesheet(Long timesheetId, Long userId) {
        log.info("Deleting timesheet {} for user {}", timesheetId, userId);
        
        // Find timesheet
        TimesheetEntry entry = timesheetRepository.findById(timesheetId)
            .orElseThrow(() -> new IllegalArgumentException("Timesheet not found"));
        
        // Verify ownership
        if (!entry.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot delete timesheet of another user");
        }
        
        // Get hours before deletion for task update
        BigDecimal hoursToDeduct = entry.getCalculatedHours();
        Long taskId = entry.getTask().getId();
        
        // Delete entry
        timesheetRepository.delete(entry);
        log.info("Deleted timesheet {}", timesheetId);
        
        // Update task used_hours
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setUsedHours(task.getUsedHours() - hoursToDeduct.doubleValue());
        taskRepository.save(task);
        log.info("Updated task {} used_hours to {}", taskId, task.getUsedHours());
    }
    
    /**
     * Get timesheet details by ID.
     * 
     * @param timesheetId the timesheet ID
     * @return timesheet response
     */
    @Transactional(readOnly = true)
    public TimesheetResponse getTimesheetById(Long timesheetId) {
        TimesheetEntry entry = timesheetRepository.findById(timesheetId)
            .orElseThrow(() -> new IllegalArgumentException("Timesheet not found"));
        return timesheetMapper.toResponse(entry);
    }
    
    /**
     * Get user's timesheets within a date range.
     * 
     * @param userId the user ID
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @param pageable pagination info
     * @return paginated timesheet response
     */
    @Transactional(readOnly = true)
    public TimesheetPageResponse getUserTimesheets(Long userId, LocalDate startDate, 
                                                   LocalDate endDate, Pageable pageable) {
        log.debug("Fetching timesheets for user {} from {} to {}", 
                  userId, startDate, endDate);
        
        Page<TimesheetEntry> page = timesheetRepository.findByUserIdAndDateRange(
            userId, startDate, endDate, pageable
        );
        
        return TimesheetPageResponse.builder()
            .content(page.map(timesheetMapper::toResponse).getContent())
            .page(PageMetadata.builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build())
            .build();
    }
    
    /**
     * Get all timesheets for a task.
     * 
     * @param taskId the task ID
     * @param pageable pagination info
     * @return paginated timesheet response
     */
    @Transactional(readOnly = true)
    public TimesheetPageResponse getTaskTimesheets(Long taskId, Pageable pageable) {
        log.debug("Fetching timesheets for task {}", taskId);
        
        Page<TimesheetEntry> page = timesheetRepository.findByTaskId(taskId, pageable);
        
        return TimesheetPageResponse.builder()
            .content(page.map(timesheetMapper::toResponse).getContent())
            .page(PageMetadata.builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build())
            .build();
    }
}
