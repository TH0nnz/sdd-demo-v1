package com.example.timesheet.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Department entity representing organizational units.
 * Each department can have a manager (DEPT_HEAD role) and multiple members.
 * 
 * Uses optimistic locking with @Version to prevent concurrent modification conflicts.
 */
@Entity
@Table(name = "departments", indexes = {
    @Index(name = "idx_department_name", columnList = "name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Department {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Department name (e.g., "研發部", "銷售部")
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "部門名稱不可為空")
    @Size(min = 2, max = 100, message = "部門名稱長度必須在 2-100 字元之間")
    private String name;
    
    /**
     * Department manager (DEPT_HEAD role)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;
    
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
    
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    @Builder.Default
    private List<User> members = new ArrayList<>();
    
    /**
     * Lifecycle callback to update the timestamp before persisting changes
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
