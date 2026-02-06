package com.example.timesheet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Detailed project response DTO with all fields.
 * Used in GET /api/projects/{projectId} and POST /api/projects endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailResponse {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private String status;
    
    @JsonProperty("totalHours")
    private Integer totalHours;
    
    @JsonProperty("allocatedHours")
    private Integer allocatedHours;
    
    @JsonProperty("remainingHours")
    private Integer remainingHours;
    
    @JsonProperty("pmId")
    private Long pmId;
    
    @JsonProperty("pmName")
    private String pmName;
    
    private Integer version;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}
