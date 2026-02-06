package com.example.timesheet.domain.enums;

/**
 * TimeRequest status enumeration.
 * Tracks the approval state of a time request for additional hours.
 */
public enum TimeRequestStatus {
    /**
     * Time request is pending manager approval
     */
    PENDING,

    /**
     * Time request has been approved and hours added to project
     */
    APPROVED,

    /**
     * Time request has been rejected
     */
    REJECTED
}
