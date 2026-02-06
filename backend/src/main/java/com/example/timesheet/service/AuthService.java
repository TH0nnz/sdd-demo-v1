package com.example.timesheet.service;

import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.request.ChangePasswordRequest;
import com.example.timesheet.dto.request.LoginRequest;
import com.example.timesheet.dto.response.LoginResponse;
import com.example.timesheet.dto.response.UserResponse;
import com.example.timesheet.mapper.UserMapper;
import com.example.timesheet.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for Authentication operations (T150).
 * Handles user login, password changes, and JWT token generation.
 * 
 * Business Rules:
 * - Passwords must match confirmation
 * - Current password must be correct for password change
 * - JWT tokens valid for 24 hours
 * - Account must be active to login
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticate user and generate JWT token.
     * 
     * @param loginRequest email and password
     * @return login response with JWT token and user details
     * @throws AuthenticationException if credentials are invalid
     */
    public LoginResponse login(LoginRequest loginRequest) throws AuthenticationException {
        log.info("Login attempt for user: {}", loginRequest.getEmail());

        // Authenticate using Spring Security
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(authentication);
            
            // Fetch user details
            User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Verify user is active
            if (!user.getActive()) {
                log.warn("Login denied: User account is inactive - {}", loginRequest.getEmail());
                throw new IllegalArgumentException("User account is inactive");
            }

            UserResponse userResponse = userMapper.userToUserResponse(user);

            log.info("Login successful for user: {}", loginRequest.getEmail());
            
            return LoginResponse.builder()
                .token(token)
                .user(userResponse)
                .expiresIn("24h")
                .build();
        } catch (AuthenticationException e) {
            log.warn("Login failed for user: {} - {}", loginRequest.getEmail(), e.getMessage());
            throw e;
        }
    }

    /**
     * Change user password.
     * 
     * @param userId the user ID
     * @param request containing current password, new password, and confirmation
     * @throws IllegalArgumentException if current password is incorrect or passwords don't match
     */
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Password change request for user ID: {}", userId);

        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation password do not match");
        }

        // Fetch user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            log.warn("Password change failed: Current password is incorrect for user ID: {}", userId);
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user ID: {}", userId);
    }

    /**
     * Validate JWT token.
     * 
     * @param token the JWT token
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * Get username from JWT token.
     * 
     * @param token the JWT token
     * @return the username (email) from token
     */
    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }

    /**
     * Get current authenticated user.
     * 
     * @param email the user's email
     * @return user response DTO
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.userToUserResponse(user);
    }
}
