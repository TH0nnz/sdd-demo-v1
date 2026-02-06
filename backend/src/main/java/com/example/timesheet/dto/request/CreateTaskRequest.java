package com.example.timesheet.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for creating a new task.
 * Used in POST /api/tasks endpoint.
 * Validates required fields and constraints for task creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    
    /**
     * Task name (required)
     * Must be between 3 and 200 characters
     */
    @NotBlank(message = "Task name cannot be blank")
    @Size(min = 3, max = 200, message = "Task name must be between 3 and 200 characters")
    private String name;
    
    /**
     * Task description (optional)
     * Maximum 2000 characters
     */
    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;
    
    /**
     * Estimated hours for task completion (required)
     * Must be greater than 0
     */
    @NotNull(message = "Estimated hours cannot be null")
    @DecimalMin(value = "0.01", message = "Estimated hours must be greater than 0")
    private Double estimatedHours;
    
    /**
     * Project ID for this task (required)
     * The task will be assigned to this project
     */
    @NotNull(message = "Project ID cannot be null")
    private Long projectId;
    
    /**
     * Assignee user ID (required)
     * The user who will be responsible for this task
     */
    @NotNull(message = "Assignee ID cannot be null")
    private Long assigneeId;
}
