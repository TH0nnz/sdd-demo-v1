package com.example.timesheet.dto.response;

import lombok.*;

import java.util.List;

/**
 * DTO for paginated timesheet response.
 * Used in GET /api/timesheets endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetPageResponse {
    
    private List<TimesheetResponse> content;
    
    private PageMetadata page;
}
