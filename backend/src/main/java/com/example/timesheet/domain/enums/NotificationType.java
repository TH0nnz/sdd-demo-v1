package com.example.timesheet.domain.enums;

/**
 * Notification type enumeration.
 * Defines the types of system notifications that can be sent to users.
 */
public enum NotificationType {
    /**
     * Notification when a task is assigned to an employee
     */
    TASK_ASSIGNED,
    
    /**
     * Notification when task hours are running low
     */
    TASK_HOURS_LOW,
    
    /**
     * Notification when a task is completed
     */
    TASK_COMPLETED,
    
    /**
     * Notification when a task is closed by PM
     */
    TASK_CLOSED,
    
    /**
     * Notification when an hours request is approved
     */
    HOURS_REQUEST_APPROVED,
    
    /**
     * Notification when an hours request is rejected
     */
    HOURS_REQUEST_REJECTED,
    
    /**
     * Notification when a new hours request is submitted
     */
    HOURS_REQUEST_SUBMITTED,
    
    /**
     * Notification when a project is assigned to a PM
     */
    PROJECT_ASSIGNED,
    
    /**
     * Notification when a project PM is changed
     */
    PROJECT_PM_CHANGED,
    
    /**
     * Notification when a project is closed
     */
    PROJECT_CLOSED,
    
    /**
     * General system notification
     */
    SYSTEM
}
