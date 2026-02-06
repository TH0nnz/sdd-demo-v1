package com.example.timesheet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Simplified task response DTO with minimal fields.
 * Used in lists and nested responses where full task details are not needed.
 * Provides only essential task information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSimpleResponse {
    
    private Long id;
    
    private String name;
    
    private String status;
    
    @JsonProperty("estimatedHours")
    private Double estimatedHours;
    
    @JsonProperty("usedHours")
    private Double usedHours;
    
    @JsonProperty("projectId")
    private Long projectId;
    
    @JsonProperty("assigneeId")
    private Long assigneeId;
}
