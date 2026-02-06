package com.example.timesheet.domain.entity;

import com.example.timesheet.domain.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

/**
 * User entity representing system users with five possible roles:
 * MANAGER, PM, DEPT_HEAD, EXECUTIVE, HR
 * 
 * Implements UserDetails for Spring Security integration.
 * Uses optimistic locking with @Version to prevent concurrent modification conflicts.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_department", columnList = "department_id"),
    @Index(name = "idx_user_role", columnList = "role")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * User's full name (e.g., "張三", "李四")
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "姓名不可為空")
    @Size(min = 2, max = 100, message = "姓名長度必須在 2-100 字元之間")
    private String name;
    
    /**
     * User's email address (unique identifier for login)
     */
    @Column(nullable = false, unique = true, length = 255)
    @NotBlank(message = "電子郵件不可為空")
    @Email(message = "電子郵件格式不正確")
    private String email;
    
    /**
     * BCrypt hashed password
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    @NotBlank(message = "密碼不可為空")
    private String passwordHash;
    
    /**
     * User's role in the system
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "角色不可為空")
    private UserRole role;
    
    /**
     * User's department
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    /**
     * Account activation status (true = enabled, false = disabled)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    /**
     * Record creation timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Last update timestamp
     */
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    /**
     * Optimistic locking version
     */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;
    
    // ========== Relationships ==========
    
    @OneToMany(mappedBy = "assignee", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Task> assignedTasks = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TimesheetEntry> timesheetEntries = new ArrayList<>();
    
    @OneToMany(mappedBy = "pm", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Project> managedProjects = new ArrayList<>();
    
    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TimeRequest> timeRequests = new ArrayList<>();
    
    // ========== UserDetails Implementation ==========
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }
    
    @Override
    public String getPassword() {
        return passwordHash;
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return active;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return active;
    }
    
    // ========== Lifecycle Callbacks ==========
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
