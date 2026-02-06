package com.example.timesheet.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;

/**
 * DTO for updating an existing timesheet entry.
 * Used in PUT /api/timesheets/{timesheetId} endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTimesheetRequest {
    
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
