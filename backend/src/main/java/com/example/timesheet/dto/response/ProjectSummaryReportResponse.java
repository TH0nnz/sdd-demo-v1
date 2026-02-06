package com.example.timesheet.dto.response;

import lombok.*;

/**
 * Response DTO for project summary report entries (T141).
 * Represents aggregated timesheet data by project.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryReportResponse {
    private Long projectId;
    private String projectName;
    private String totalHours;
    private Integer entryCount;
    private Long assignedUserCount;
}
