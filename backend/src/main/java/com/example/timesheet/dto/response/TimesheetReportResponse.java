package com.example.timesheet.dto.response;

import lombok.*;

/**
 * Response DTO for individual timesheet report entries (T141).
 * Represents a single timesheet entry in a report context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetReportResponse {
    private Long id;
    private String date;
    private String userName;
    private Long userId;
    private String projectName;
    private Long projectId;
    private String taskName;
    private Long taskId;
    private Number hours;
}
