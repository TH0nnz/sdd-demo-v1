package com.example.timesheet.controller;

import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.dto.request.CreateUserRequest;
import com.example.timesheet.dto.request.UpdateUserRequest;
import com.example.timesheet.dto.response.MessageResponse;
import com.example.timesheet.dto.response.UserPageResponse;
import com.example.timesheet.dto.response.UserResponse;
import com.example.timesheet.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user management.
 * Handles user CRUD operations (create, read, update) and activation/deactivation.
 * Only HR users have access to these endpoints.
 * 
 * Corresponds to FR-025, FR-026, FR-027
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "用戶管理 API (HR)")
@Slf4j
@SecurityRequirement(name = "BearerAuth")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Get all users with optional filtering and pagination.
     * 
     * @param role optional role filter
     * @param departmentId optional department filter
     * @param active optional active status filter
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated user list
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'DEPT_HEAD')")
    @Operation(summary = "查詢用戶列表")
    public ResponseEntity<UserPageResponse> listUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Listing users with filters - role: {}, dept: {}, active: {}", role, departmentId, active);
        
        Pageable pageable = PageRequest.of(page, size);
        UserPageResponse response = userService.getAllUsers(role, departmentId, active, pageable);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create a new user.
     * Corresponds to FR-025, FR-025-1
     * 
     * @param request create user request DTO
     * @return created user response with 201 status
     */
    @PostMapping
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "建立新用戶", description = "HR 新增員工帳號，系統自動生成初始密碼")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating new user: {}", request.getEmail());
        
        try {
            UserResponse response = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            if ("電子郵件已存在".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            throw e;
        }
    }
    
    /**
     * Get a specific user by ID.
     * 
     * @param userId the user ID
     * @return user response
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('HR', 'DEPT_HEAD')")
    @Operation(summary = "取得用戶詳情")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        log.info("Getting user: {}", userId);
        
        try {
            UserResponse response = userService.getUserById(userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Update a user's information.
     * Corresponds to FR-026, FR-027
     * 
     * @param userId the user ID
     * @param request update user request DTO
     * @return updated user response
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "更新用戶資訊", description = "HR 修改員工的角色、部門、狀態")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, 
                                                   @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating user: {}", userId);
        
        try {
            UserResponse response = userService.updateUser(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if ("用戶不存在".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if ("資料已被修改，請重新載入".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            throw e;
        }
    }
    
    /**
     * Deactivate a user account (soft delete).
     * Corresponds to FR-027
     * 
     * @param userId the user ID
     * @return message response
     */
    @PostMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "停用用戶帳號", description = "HR 停用員工帳號（軟刪除），保留歷史記錄")
    public ResponseEntity<MessageResponse> deactivateUser(@PathVariable Long userId) {
        log.info("Deactivating user: {}", userId);
        
        try {
            userService.deactivateUser(userId);
            return ResponseEntity.ok(MessageResponse.builder()
                    .message("用戶已停用")
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Activate a user account.
     * Corresponds to FR-027
     * 
     * @param userId the user ID
     * @return message response
     */
    @PostMapping("/{userId}/activate")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "啟用用戶帳號", description = "HR 重新啟用已停用的員工帳號")
    public ResponseEntity<MessageResponse> activateUser(@PathVariable Long userId) {
        log.info("Activating user: {}", userId);
        
        try {
            userService.activateUser(userId);
            return ResponseEntity.ok(MessageResponse.builder()
                    .message("用戶已啟用")
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
