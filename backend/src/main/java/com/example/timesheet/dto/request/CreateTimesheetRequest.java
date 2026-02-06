package com.example.timesheet.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for creating a new timesheet entry.
 * Used in POST /api/timesheets endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTimesheetRequest {
    
    /**
     * Task ID the timesheet is for (required)
     */
    @NotNull(message = "Task ID cannot be null")
    private Long taskId;
    
    /**
     * Work date (required)
     * Cannot be a future date
     */
    @NotNull(message = "Work date cannot be null")
    @PastOrPresent(message = "Work date cannot be in the future")
    private LocalDate workDate;
    
    /**
     * Work start time (required)
     */
    @NotNull(message = "Start time cannot be null")
    private LocalTime startTime;
    
    /**
     * Work end time (required)
     * Must be after start time
     */
    @NotNull(message = "End time cannot be null")
    private LocalTime endTime;
}
