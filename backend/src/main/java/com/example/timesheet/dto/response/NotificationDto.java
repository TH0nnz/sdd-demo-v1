package com.example.timesheet.dto.response;

import com.example.timesheet.domain.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Notification entity.
 * Returned when querying notifications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    
    /**
     * Notification ID
     */
    private Long id;
    
    /**
     * Notification type
     */
    private NotificationType type;
    
    /**
     * Notification title
     */
    private String title;
    
    /**
     * Notification message content
     */
    private String message;
    
    /**
     * Related entity type (PROJECT, TASK, HOURS_REQUEST, etc.)
     */
    private String relatedEntityType;
    
    /**
     * Related entity ID
     */
    private Long relatedEntityId;
    
    /**
     * Read status
     */
    private Boolean isRead;
    
    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;
}
