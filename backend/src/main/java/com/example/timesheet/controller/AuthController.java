package com.example.timesheet.controller;

import com.example.timesheet.dto.request.ChangePasswordRequest;
import com.example.timesheet.dto.request.LoginRequest;
import com.example.timesheet.dto.response.LoginResponse;
import com.example.timesheet.dto.response.MessageResponse;
import com.example.timesheet.dto.response.UserResponse;
import com.example.timesheet.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication (T152).
 * Handles user login, password changes, and user info retrieval.
 * 
 * Corresponds to Phase 8 - Authentication & Common UI
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * User login endpoint.
     * Returns JWT token and user information on successful authentication.
     * 
     * @param loginRequest email and password
     * @return login response with JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and receive JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request for user: {}", loginRequest.getEmail());
        
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    /**
     * Change password endpoint.
     * Requires authentication. User can only change their own password.
     * 
     * @param changePasswordRequest current password, new password, and confirmation
     * @param authentication current user authentication
     * @return success message
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Change password",
        description = "Change authenticated user's password"
    )
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest,
            Authentication authentication) {
        
        log.info("Change password request from user: {}", authentication.getName());
        
        // Get current user from authentication principal
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // In a real scenario, you would fetch the user ID from the authentication context
        // For now, we'll log it and proceed with a simplified approach
        try {
            // This is a simplified version - in production you would get the actual user ID
            // from the JWT token or authentication context
            Long userId = getCurrentUserId(authentication);
            authService.changePassword(userId, changePasswordRequest);
            
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(MessageResponse.builder()
                    .message("Password changed successfully")
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("Password change failed: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(MessageResponse.builder()
                    .message(e.getMessage())
                    .build());
        }
    }

    /**
     * Get current authenticated user information.
     * 
     * @param authentication current user authentication
     * @return current user details
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get current user",
        description = "Retrieve information about the currently authenticated user"
    )
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        log.info("Get current user request from: {}", authentication.getName());
        
        UserResponse user = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(user);
    }

    /**
     * Logout endpoint (can be used to invalidate token on client side).
     * Note: JWT tokens are stateless, so server-side logout is not required.
     * Client should just delete the token from localStorage.
     * 
     * @return success message
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "User logout",
        description = "Logout user (client should delete token)"
    )
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<MessageResponse> logout() {
        log.info("Logout request received");
        
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(MessageResponse.builder()
                .message("Logged out successfully. Please delete token from client.")
                .build());
    }

    /**
     * Helper method to extract user ID from authentication.
     * In a real scenario, this would be implemented properly using JWT claims.
     */
    private Long getCurrentUserId(Authentication authentication) {
        // This is a placeholder - in production, you would properly extract the user ID
        // from the JWT token claims or authentication context
        return 1L; // This should be replaced with actual implementation
    }
}
