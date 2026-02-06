package com.example.timesheet.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for timesheet entry response.
 * Used in GET, POST, PUT endpoints for single timesheet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetResponse {
    
    private Long id;
    
    private Long userId;
    
    private String userName;
    
    private Long taskId;
    
    private String taskName;
    
    private Long projectId;
    
    private String projectName;
    
    private LocalDate workDate;
    
    private LocalTime startTime;
    
    private LocalTime endTime;
    
    private BigDecimal calculatedHours;
    
    private Boolean lunchDeducted;
    
    private String message;
    
    private String createdAt;
    
    private String updatedAt;
}
