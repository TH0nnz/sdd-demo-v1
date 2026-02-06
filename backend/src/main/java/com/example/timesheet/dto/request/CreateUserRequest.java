package com.example.timesheet.dto.request;

import com.example.timesheet.domain.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for creating a new user.
 * Used by HR to add new employees to the system.
 * 
 * Corresponds to FR-025 and FR-025-1.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    
    /**
     * User's full name (required)
     */
    @NotBlank(message = "姓名不可為空")
    @Size(min = 2, max = 100, message = "姓名長度必須在 2-100 字元之間")
    private String name;
    
    /**
     * User's email address (required, must be unique)
     */
    @NotBlank(message = "電子郵件不可為空")
    @Email(message = "電子郵件格式不正確")
    private String email;
    
    /**
     * User's role in the system (required)
     */
    @NotNull(message = "角色不可為空")
    private UserRole role;
    
    /**
     * Department ID (optional)
     */
    private Long departmentId;
    
    /**
     * Initial password for the new user (optional)
     * If not provided, system will auto-generate a password
     */
    @Size(min = 8, message = "初始密碼最少需要 8 字元")
    private String initialPassword;
}
