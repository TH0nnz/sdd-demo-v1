package com.example.timesheet.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for updating an existing task.
 * Used in PUT /api/tasks/{taskId} endpoint.
 * Supports partial updates with optimistic locking via version field.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {
    
    /**
     * Task name (optional)
     * Must be between 3 and 200 characters if provided
     */
    @Size(min = 3, max = 200, message = "Task name must be between 3 and 200 characters")
    private String name;
    
    /**
     * Task description (optional)
     * Maximum 2000 characters
     */
    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;
    
    /**
     * Estimated hours (optional)
     * Must be greater than 0 if provided
     */
    @DecimalMin(value = "0.01", message = "Estimated hours must be greater than 0")
    private Double estimatedHours;
    
    /**
     * Used/spent hours (optional)
     * Must be non-negative
     */
    @DecimalMin(value = "0", message = "Used hours cannot be negative")
    private Double usedHours;
    
    /**
     * Task status (optional)
     * Valid values: TODO, IN_PROGRESS, COMPLETED
     */
    @Pattern(regexp = "TODO|IN_PROGRESS|COMPLETED", 
             message = "Status must be one of: TODO, IN_PROGRESS, COMPLETED")
    private String status;
    
    /**
     * Assignee user ID (optional)
     * Can be changed to reassign the task
     */
    private Long assigneeId;
    
    /**
     * Current version for optimistic locking (required for updates)
     * Used to prevent concurrent modification conflicts
     */
    @NotNull(message = "Version cannot be null (required for optimistic locking)")
    @Min(value = 0, message = "Version cannot be negative")
    private Integer version;
}
