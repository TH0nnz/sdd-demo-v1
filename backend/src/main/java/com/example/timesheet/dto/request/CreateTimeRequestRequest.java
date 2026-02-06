package com.example.timesheet.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for creating a time request for additional project hours.
 * Used when PM needs to request more hours from manager.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTimeRequestRequest {
    
    /**
     * Project ID for which additional hours are requested
     */
    @NotNull(message = "Project ID cannot be null")
    private Long projectId;
    
    /**
     * Number of additional hours being requested
     */
    @NotNull(message = "Requested hours cannot be null")
    @Min(value = 1, message = "Requested hours must be at least 1")
    private Integer requestedHours;
    
    /**
     * Reason for requesting additional hours
     */
    @Size(max = 1000, message = "Reason must be at most 1000 characters")
    private String reason;
}
