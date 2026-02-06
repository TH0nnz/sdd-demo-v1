package com.example.timesheet.dto.response;

import com.example.timesheet.domain.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for complete user information.
 * Used in API responses when returning user details.
 * 
 * Corresponds to UserResponse schema in api-spec.yaml
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private DepartmentSimpleResponse department;
    
    @JsonProperty("active")
    private Boolean active;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
}
