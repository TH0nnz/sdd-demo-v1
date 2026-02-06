package com.example.timesheet.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for complete department information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    
    private Long id;
    private String name;
    private UserSimpleResponse manager;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
}
