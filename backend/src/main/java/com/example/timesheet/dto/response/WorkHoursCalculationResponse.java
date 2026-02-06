package com.example.timesheet.dto.response;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for work hours calculation preview response.
 * Used in POST /api/timesheets/calculate-preview endpoint.
 * Provides real-time preview of calculated hours before submission.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkHoursCalculationResponse {
    
    private BigDecimal calculatedHours;
    
    private Boolean lunchDeducted;
    
    private BigDecimal lunchHours;
    
    private String message;
    
    private Boolean valid;
    
    private String validationMessage;
}
