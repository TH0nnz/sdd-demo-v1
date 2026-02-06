package com.example.timesheet.dto.response;

import lombok.*;

/**
 * Response DTO for user summary report entries (T141).
 * Represents aggregated timesheet data by user within a department.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryReportResponse {
    private Long userId;
    private String userName;
    private String totalHours;
    private Integer entryCount;
    private Integer projectCount;
}
