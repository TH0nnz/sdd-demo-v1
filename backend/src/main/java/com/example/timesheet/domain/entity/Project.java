package com.example.timesheet.domain.entity;

import com.example.timesheet.domain.enums.ProjectStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Project entity representing a project in the timesheet management system.
 * Tracks project details, time allocation, and status.
 * Uses optimistic locking with @Version to prevent concurrent modification conflicts.
 */
@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_project_pm", columnList = "pm_id"),
    @Index(name = "idx_project_status", columnList = "status"),
    @Index(name = "idx_project_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Project name
     */
    @Column(nullable = false, length = 255)
    @NotBlank(message = "Project name cannot be blank")
    @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
    private String name;
    
    /**
     * Project description (optional)
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * Total allocated hours for this project
     */
    @Column(nullable = false)
    @Min(value = 1, message = "Total hours must be at least 1")
    private Integer totalHours;
    
    /**
     * Already allocated hours (sum of all task estimates)
     */
    @Column(nullable = false)
    @Min(value = 0, message = "Allocated hours cannot be negative")
    @Builder.Default
    private Integer allocatedHours = 0;
    
    /**
     * Project status: ACTIVE or CLOSED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.ACTIVE;
    
    /**
     * PM assigned to this project
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pm_id", nullable = false)
    @NotNull(message = "PM cannot be null")
    private User pm;
    
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
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TimeRequest> timeRequests = new ArrayList<>();
    
    // ========== Computed Properties ==========
    
    /**
     * Calculate remaining hours available for allocation
     * @return totalHours - allocatedHours
     */
    @Transient
    public Integer getRemainingHours() {
        return totalHours - allocatedHours;
    }
    
    // ========== Lifecycle Callbacks ==========
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
