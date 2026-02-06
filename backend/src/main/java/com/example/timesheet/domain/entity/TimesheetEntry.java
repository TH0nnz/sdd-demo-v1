package com.example.timesheet.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * TimesheetEntry entity representing work hours recorded by an executive.
 * 
 * Business Rules:
 * - Auto lunch deduction: 12:00-13:00 (1 hour)
 * - Edit window: 3 working days (validated in service)
 * - Time increments: 0.5 hours only
 * - When submitted, auto-updates task.used_hours
 */
@Entity
@Table(name = "timesheet_entries", indexes = {
    @Index(name = "idx_user_work_date", columnList = "user_id, work_date"),
    @Index(name = "idx_task_user", columnList = "task_id, user_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "calculated_hours", nullable = false)
    private BigDecimal calculatedHours;
    
    @Column(name = "lunch_deducted_flag", nullable = false)
    private Boolean lunchDeducted = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 0;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
