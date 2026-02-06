package com.example.timesheet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Detailed task response DTO with complete information including nested objects.
 * Used in GET /api/tasks/{taskId} and POST /api/tasks endpoints.
 * Includes nested project and assignee information for comprehensive task details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailResponse {
    
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
    
    @JsonProperty("projectName")
    private String projectName;
    
    @JsonProperty("assigneeId")
    private Long assigneeId;
    
    @JsonProperty("assigneeName")
    private String assigneeName;
    
    @JsonProperty("assigneeEmail")
    private String assigneeEmail;
    
    private Integer version;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @JsonProperty("completedAt")
    private LocalDateTime completedAt;
}
