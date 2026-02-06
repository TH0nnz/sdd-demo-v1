package com.example.timesheet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Project dashboard response with real-time statistics.
 * Includes project info and aggregated metrics for PM monitoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDashboardResponse {
    
    private Long id;
    
    private String name;
    
    private String status;
    
    @JsonProperty("totalHours")
    private Integer totalHours;
    
    @JsonProperty("allocatedHours")
    private Integer allocatedHours;
    
    @JsonProperty("remainingHours")
    private Integer remainingHours;
    
    @JsonProperty("taskCount")
    private Integer taskCount;
    
    @JsonProperty("completedTaskCount")
    private Integer completedTaskCount;
    
    @JsonProperty("completionRate")
    private Double completionRate;
    
    @JsonProperty("allocationRate")
    private Double allocationRate;
}
