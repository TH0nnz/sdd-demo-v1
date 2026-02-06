package com.example.timesheet.dto.response;

import lombok.*;
import java.util.List;

/**
 * Response DTO for paginated timesheet report (T141).
 * Contains a page of timesheet report entries with metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetReportPageResponse {
    private List<TimesheetReportResponse> content;
    private PageMetadata page;
}
