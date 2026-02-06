package com.example.timesheet.dto.response;

import lombok.*;

/**
 * Response DTO for login (T151).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserResponse user;
    private String expiresIn;
}
