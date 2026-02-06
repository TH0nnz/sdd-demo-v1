package com.example.timesheet.domain.entity;

import com.example.timesheet.domain.enums.TimeRequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * TimeRequest entity representing a request for additional project hours.
 * PM submits requests when project tasks need more time, Manager approves or rejects.
 * Uses optimistic locking with @Version to prevent concurrent modification conflicts.
 */
@Entity
@Table(name = "time_requests", indexes = {
    @Index(name = "idx_time_request_project", columnList = "project_id"),
    @Index(name = "idx_time_request_requester", columnList = "requester_id"),
    @Index(name = "idx_time_request_status", columnList = "status"),
    @Index(name = "idx_time_request_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TimeRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Project for which additional hours are requested
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @NotNull(message = "Project cannot be null")
    private Project project;
    
    /**
     * Number of additional hours being requested
     */
    @Column(nullable = false)
    @Min(value = 1, message = "Requested hours must be at least 1")
    private Integer requestedHours;
    
    /**
     * Reason for requesting additional hours
     */
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    /**
     * Status of the request: PENDING, APPROVED, or REJECTED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TimeRequestStatus status = TimeRequestStatus.PENDING;
    
    /**
     * User who submitted the request (typically PM)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    @NotNull(message = "Requester cannot be null")
    private User requester;
    
    /**
     * Approval or rejection reason from manager
     */
    @Column(columnDefinition = "TEXT", name = "approval_reason")
    private String approvalReason;
    
    /**
     * User who approved/rejected the request (typically Manager)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;
    
    /**
     * Timestamp when request was approved/rejected
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
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
    
    // ========== Lifecycle Callbacks ==========
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
