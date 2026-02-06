package com.example.timesheet.service;

import com.example.timesheet.domain.entity.Department;
import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.enums.UserRole;
import com.example.timesheet.domain.repository.DepartmentRepository;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.request.CreateUserRequest;
import com.example.timesheet.dto.request.UpdateUserRequest;
import com.example.timesheet.dto.response.UserPageResponse;
import com.example.timesheet.dto.response.UserResponse;
import com.example.timesheet.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

/**
 * Service for managing users.
 * Handles user CRUD operations, role assignment, and activation/deactivation.
 * 
 * Corresponds to FR-025, FR-026, FR-027
 */
@Service
@Slf4j
@Transactional
public class UserService {
    
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int PASSWORD_LENGTH = 12;
    
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository,
                       DepartmentRepository departmentRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Create a new user.
     * Corresponds to FR-025, FR-025-1
     *
     * @param request the create user request
     * @return created user response
     * @throws IllegalArgumentException if email already exists
     */
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user: {}", request.getEmail());
        
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("電子郵件已存在");
        }
        
        // Load department if specified
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("部門不存在"));
        }
        
        // Generate password
        String password = request.getInitialPassword() != null && !request.getInitialPassword().isEmpty()
                ? request.getInitialPassword()
                : generatePassword();
        
        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(password))
                .role(request.getRole())
                .department(department)
                .active(true)
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully: {} (ID: {})", savedUser.getEmail(), savedUser.getId());
        
        return userMapper.userToUserResponse(savedUser);
    }
    
    /**
     * Get user by ID.
     *
     * @param userId the user ID
     * @return user response
     * @throws IllegalArgumentException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        return userMapper.userToUserResponse(user);
    }
    
    /**
     * Get all users with pagination and filtering.
     *
     * @param role filter by role (optional)
     * @param departmentId filter by department (optional)
     * @param active filter by active status (optional)
     * @param pageable pagination info
     * @return paginated user responses
     */
    @Transactional(readOnly = true)
    public UserPageResponse getAllUsers(UserRole role, Long departmentId, Boolean active, Pageable pageable) {
        Page<User> page;
        
        if (role != null && departmentId != null) {
            page = userRepository.findByRoleAndDepartmentIdAndActive(role, departmentId, active != null ? active : true, pageable);
        } else if (role != null) {
            page = userRepository.findByRoleAndActive(role, active != null ? active : true, pageable);
        } else if (departmentId != null) {
            page = userRepository.findByDepartmentIdAndActive(departmentId, active != null ? active : true, pageable);
        } else if (active != null) {
            page = userRepository.findByActive(active, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }
        
        return UserPageResponse.builder()
                .content(userMapper.usersToUserResponses(page.getContent()))
                .pageInfo(com.example.timesheet.dto.response.PageMetadata.from(page))
                .build();
    }
    
    /**
     * Update user information.
     * Corresponds to FR-026, FR-027
     *
     * @param userId the user ID
     * @param request the update request
     * @return updated user response
     * @throws IllegalArgumentException if user not found or version mismatch
     */
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        log.info("Updating user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        
        // Check optimistic locking version
        if (request.getVersion() != null && !request.getVersion().equals(user.getVersion())) {
            throw new IllegalArgumentException("資料已被修改，請重新載入");
        }
        
        // Load new department if specified
        if (request.getDepartmentId() != null && !request.getDepartmentId().equals(
                user.getDepartment() != null ? user.getDepartment().getId() : null)) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("部門不存在"));
            user.setDepartment(department);
        }
        
        // Update fields
        userMapper.updateUserFromUpdateRequest(request, user);
        
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", userId);
        
        return userMapper.userToUserResponse(updatedUser);
    }
    
    /**
     * Deactivate a user.
     * Corresponds to FR-027
     *
     * @param userId the user ID
     * @throws IllegalArgumentException if user not found
     */
    public void deactivateUser(Long userId) {
        log.info("Deactivating user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        
        user.setActive(false);
        userRepository.save(user);
        
        log.info("User deactivated successfully: {}", userId);
    }
    
    /**
     * Activate a user.
     * Corresponds to FR-027
     *
     * @param userId the user ID
     * @throws IllegalArgumentException if user not found
     */
    public void activateUser(Long userId) {
        log.info("Activating user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        
        user.setActive(true);
        userRepository.save(user);
        
        log.info("User activated successfully: {}", userId);
    }
    
    /**
     * Generate a random password.
     *
     * @return generated password
     */
    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return password.toString();
    }
}
