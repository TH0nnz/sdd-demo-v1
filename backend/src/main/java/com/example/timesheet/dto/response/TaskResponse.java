package com.example.timesheet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Main task response DTO with complete information.
 * Used as a base response DTO for task endpoints.
 * Contains all essential task information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private String status;
    
    @JsonProperty("estimatedHours")
    private Double estimatedHours;
    
    @JsonProperty("usedHours")
    private Double usedHours;
    
    @JsonProperty("projectId")
    private Long projectId;
    
    @JsonProperty("assigneeId")
    private Long assigneeId;
    
    private Integer version;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @JsonProperty("completedAt")
    private LocalDateTime completedAt;
}
