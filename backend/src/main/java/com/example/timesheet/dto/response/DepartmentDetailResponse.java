package com.example.timesheet.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for detailed department information with member count.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDetailResponse {
    
    private Long id;
    private String name;
    private UserSimpleResponse manager;
    private Integer memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
}
