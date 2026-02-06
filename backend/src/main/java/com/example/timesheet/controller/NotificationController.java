package com.example.timesheet.controller;

import com.example.timesheet.domain.entity.Notification;
import com.example.timesheet.domain.repository.UserRepository;
import com.example.timesheet.dto.response.MessageResponse;
import com.example.timesheet.dto.response.NotificationDto;
import com.example.timesheet.security.JwtTokenProvider;
import com.example.timesheet.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Notification operations.
 * Handles notification retrieval and read status management.
 * 
 * Endpoints:
 * - GET /api/notifications - Get all notifications for current user
 * - GET /api/notifications/unread - Get unread notifications
 * - GET /api/notifications/unread/count - Get unread notification count
 * - PUT /api/notifications/{id}/read - Mark notification as read
 * - PUT /api/notifications/read-all - Mark all notifications as read
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    
    /**
     * Get user ID from authentication.
     */
    private Long getUserId(Authentication authentication) {
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"))
            .getId();
    }
    
    /**
     * Get all notifications for the current user with pagination.
     * Returns both read and unread notifications, ordered by creation date descending.
     * 
     * @param page Page number (0-indexed)
     * @param size Page size (default: 20)
     * @param authentication Current user authentication
     * @return Page of notifications
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationDto>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = getUserId(authentication);
        log.debug("User {} fetching notifications, page: {}, size: {}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getNotifications(userId, pageable);
        
        Page<NotificationDto> response = notifications.map(this::mapToDto);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get unread notifications for the current user with pagination.
     * Used for notification dropdown/bell icon.
     * 
     * @param page Page number (0-indexed)
     * @param size Page size (default: 10)
     * @param authentication Current user authentication
     * @return Page of unread notifications
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationDto>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        Long userId = getUserId(authentication);
        log.debug("User {} fetching unread notifications, page: {}, size: {}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getUnreadNotifications(userId, pageable);
        
        Page<NotificationDto> response = notifications.map(this::mapToDto);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get count of unread notifications for the current user.
     * Used for notification badge/counter in UI.
     * 
     * @param authentication Current user authentication
     * @return Unread notification count
     */
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(Authentication authentication) {
        
        Long userId = getUserId(authentication);
        log.debug("User {} fetching unread notification count", userId);
        
        long count = notificationService.getUnreadCount(userId);
        
        return ResponseEntity.ok(UnreadCountResponse.builder()
            .count(count)
            .build());
    }
    
    /**
     * Mark a specific notification as read.
     * User can only mark their own notifications as read.
     * 
     * @param id Notification ID
     * @param authentication Current user authentication
     * @return Success message
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        
        Long userId = getUserId(authentication);
        log.info("User {} marking notification {} as read", userId, id);
        
        notificationService.markAsRead(id, userId);
        
        return ResponseEntity.ok(MessageResponse.builder()
            .message("通知已標記為已讀")
            .build());
    }
    
    /**
     * Mark all unread notifications as read for the current user.
     * Useful for "mark all as read" feature.
     * 
     * @param authentication Current user authentication
     * @return Success message with count of marked notifications
     */
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> markAllAsRead(Authentication authentication) {
        
        Long userId = getUserId(authentication);
        log.info("User {} marking all notifications as read", userId);
        
        int count = notificationService.markAllAsRead(userId);
        
        return ResponseEntity.ok(MessageResponse.builder()
            .message(String.format("已標記 %d 個通知為已讀", count))
            .build());
    }
    
    /**
     * Map Notification entity to NotificationDto.
     * 
     * @param notification Notification entity
     * @return NotificationDto
     */
    private NotificationDto mapToDto(Notification notification) {
        return NotificationDto.builder()
            .id(notification.getId())
            .type(notification.getType())
            .title(notification.getTitle())
            .message(notification.getMessage())
            .relatedEntityType(notification.getRelatedEntityType())
            .relatedEntityId(notification.getRelatedEntityId())
            .isRead(notification.getIsRead())
            .createdAt(notification.getCreatedAt())
            .build();
    }
    
    /**
     * Response DTO for unread count endpoint.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UnreadCountResponse {
        private long count;
    }
}
