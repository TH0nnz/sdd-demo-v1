package com.example.timesheet.domain.enums;

/**
 * Project status enumeration.
 * Tracks the lifecycle state of a project.
 */
public enum ProjectStatus {
    /**
     * Project is active and accepting time allocations
     */
    ACTIVE,

    /**
     * Project is closed and no longer accepting new allocations
     */
    CLOSED
}
