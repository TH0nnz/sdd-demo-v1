package com.example.timesheet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * TimeRequest response DTO with all fields.
 * Used in time request listings and details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeRequestResponse {
    
    private Long id;
    
    @JsonProperty("projectId")
    private Long projectId;
    
    private String projectName;
    
    @JsonProperty("requestedHours")
    private Integer requestedHours;
    
    private String reason;
    
    private String status;
    
    @JsonProperty("requesterId")
    private Long requesterId;
    
    private String requesterName;
    
    @JsonProperty("approvalReason")
    private String approvalReason;
    
    @JsonProperty("approverId")
    private Long approverId;
    
    private String approverName;
    
    @JsonProperty("approvedAt")
    private LocalDateTime approvedAt;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}
