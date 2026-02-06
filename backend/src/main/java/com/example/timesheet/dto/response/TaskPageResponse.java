package com.example.timesheet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Paginated response wrapper for tasks.
 * Contains task list and pagination metadata.
 * Used in GET /api/tasks endpoint with pagination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskPageResponse {
    
    private List<TaskDetailResponse> content;
    
    @JsonProperty("pageInfo")
    private PageMetadata pageInfo;
}
