package com.example.timesheet.dto.response;

import lombok.*;

import java.util.List;

/**
 * DTO for paginated user list response.
 * Combines paginated content with metadata.
 * 
 * Corresponds to UserPageResponse schema in api-spec.yaml
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPageResponse {
    
    private List<UserResponse> content;
    private PageMetadata pageInfo;
}
