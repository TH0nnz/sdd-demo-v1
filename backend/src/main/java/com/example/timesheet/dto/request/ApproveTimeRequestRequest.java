package com.example.timesheet.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for approving or rejecting a time request.
 * Used in POST /api/time-requests/{requestId}/approve endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveTimeRequestRequest {
    
    /**
     * Whether to approve (true) or reject (false) the request
     */
    @NotNull(message = "Approved field cannot be null")
    private Boolean approved;
    
    /**
     * Reason for approval or rejection
     */
    @NotBlank(message = "Reason cannot be blank")
    @Size(min = 1, max = 500, message = "Reason must be between 1 and 500 characters")
    private String reason;
}
