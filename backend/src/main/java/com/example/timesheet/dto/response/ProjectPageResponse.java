package com.example.timesheet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Paginated response wrapper for projects.
 * Contains project list and pagination metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPageResponse {
    
    private List<ProjectDetailResponse> content;
    
    @JsonProperty("pageInfo")
    private PageMetadata pageInfo;
}
