package com.example.timesheet.dto.response;

import com.example.timesheet.domain.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for department information (simplified response).
 * Used in contexts where only basic department info is needed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentSimpleResponse {
    
    private Long id;
    private String name;
}
