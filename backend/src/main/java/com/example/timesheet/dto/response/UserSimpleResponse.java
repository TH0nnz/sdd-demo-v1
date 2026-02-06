package com.example.timesheet.dto.response;

import com.example.timesheet.domain.enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for simplified user information.
 * Used in list responses where minimal user details are needed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleResponse {
    
    private Long id;
    private String name;
    private String email;
    private UserRole role;
}
