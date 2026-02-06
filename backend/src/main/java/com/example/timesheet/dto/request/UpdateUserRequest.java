package com.example.timesheet.dto.request;

import com.example.timesheet.domain.enums.UserRole;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for updating user information.
 * Used by HR to modify employee details, roles, and status.
 * 
 * Corresponds to FR-026 and FR-027.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    /**
     * User's full name (optional)
     */
    @Size(min = 2, max = 100, message = "姓名長度必須在 2-100 字元之間")
    private String name;
    
    /**
     * User's role in the system (optional)
     */
    private UserRole role;
    
    /**
     * Department ID (optional)
     */
    private Long departmentId;
    
    /**
     * Optimistic locking version (required for concurrency control)
     * Used to detect and prevent concurrent modification conflicts
     */
    private Integer version;
}
