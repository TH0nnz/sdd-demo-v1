package com.example.timesheet.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for updating an existing project.
 * Used in PUT /api/projects/{projectId} endpoint.
 * Supports partial updates with optimistic locking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectRequest {
    
    /**
     * Project name (optional)
     */
    @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
    private String name;
    
    /**
     * Project description (optional)
     */
    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;
    
    /**
     * Total allocated hours (optional)
     */
    @Min(value = 1, message = "Total hours must be at least 1")
    private Integer totalHours;
    
    /**
     * New PM user ID (optional) - Only EXECUTIVE can change PM
     */
    private Long pmId;
    
    /**
     * Current version for optimistic locking (required for updates)
     */
    @NotNull(message = "Version cannot be null (required for optimistic locking)")
    @Min(value = 0, message = "Version cannot be negative")
    private Integer version;
}
