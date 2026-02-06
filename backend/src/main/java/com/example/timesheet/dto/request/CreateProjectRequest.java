package com.example.timesheet.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for creating a new project.
 * Used in POST /api/projects endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {
    
    /**
     * Project name (required)
     */
    @NotBlank(message = "Project name cannot be blank")
    @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
    private String name;
    
    /**
     * Project description (optional)
     */
    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;
    
    /**
     * Total allocated hours for this project (required)
     */
    @NotNull(message = "Total hours cannot be null")
    @Min(value = 1, message = "Total hours must be at least 1")
    private Integer totalHours;
    
    /**
     * ID of the PM assigned to this project (required)
     */
    @NotNull(message = "PM ID cannot be null")
    private Long pmId;
}
