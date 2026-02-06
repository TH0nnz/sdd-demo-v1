package com.example.timesheet.domain.enums;

/**
 * Task status enumeration.
 * Represents the lifecycle states of a task.
 */
public enum TaskStatus {
    /**
     * Task is actively being worked on
     */
    IN_PROGRESS,
    
    /**
     * Task has been completed by the assignee
     */
    COMPLETED,
    
    /**
     * Task has been closed by PM (cannot be reopened)
     */
    CLOSED,
    
    /**
     * Task is pending reassignment (previous assignee removed or role changed)
     */
    PENDING_REASSIGNMENT
}
