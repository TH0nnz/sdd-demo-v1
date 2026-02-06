package com.example.timesheet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Simple project response DTO with minimal fields.
 * Used in lists and nested responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSimpleResponse {
    
    private Long id;
    
    private String name;
    
    private String status;
    
    @JsonProperty("totalHours")
    private Integer totalHours;
    
    @JsonProperty("remainingHours")
    private Integer remainingHours;
    
    @JsonProperty("pmId")
    private Long pmId;
    
    @JsonProperty("pmName")
    private String pmName;
}
