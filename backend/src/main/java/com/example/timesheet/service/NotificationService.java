package com.example.timesheet.service;

import com.example.timesheet.domain.entity.Notification;
import com.example.timesheet.domain.entity.User;
import com.example.timesheet.domain.enums.NotificationType;
import com.example.timesheet.domain.repository.NotificationRepository;
import com.example.timesheet.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for managing notifications.
 * Handles creating, reading, and querying notifications for users.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    
    /**
     * Create a notification for a user.
     * 
     * @param userId Recipient user ID
     * @param type Notification type
     * @param title Notification title
     * @param message Notification message
     * @param relatedEntityType Related entity type (PROJECT, TASK, HOURS_REQUEST, etc.)
     * @param relatedEntityId Related entity ID
     * @return Created notification
     */
    @Transactional
    public Notification createNotification(
            Long userId,
            NotificationType type,
            String title,
            String message,
            String relatedEntityType,
            Long relatedEntityId) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("找不到使用者 ID: " + userId));
        
        Notification notification = Notification.builder()
            .user(user)
            .type(type)
            .title(title)
            .message(message)
            .relatedEntityType(relatedEntityType)
            .relatedEntityId(relatedEntityId)
            .isRead(false)
            .build();
        
        Notification saved = notificationRepository.save(notification);
        log.info("Created notification for user {} - Type: {}, Title: {}", userId, type, title);
        
        return saved;
    }
    
    /**
     * Create a simple notification without related entity.
     * 
     * @param userId Recipient user ID
     * @param type Notification type
     * @param title Notification title
     * @param message Notification message
     * @return Created notification
     */
    @Transactional
    public Notification createNotification(
            Long userId,
            NotificationType type,
            String title,
            String message) {
        return createNotification(userId, type, title, message, null, null);
    }
    
    /**
     * Mark a notification as read.
     * 
     * @param notificationId Notification ID
     * @param userId User ID (for authorization check)
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("找不到通知 ID: " + notificationId));
        
        // Verify the notification belongs to the user
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("無權限標記此通知為已讀");
        }
        
        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
            log.info("Marked notification {} as read for user {}", notificationId, userId);
        }
    }
    
    /**
     * Mark all unread notifications as read for a user.
     * 
     * @param userId User ID
     * @return Number of notifications marked as read
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        Page<Notification> unreadNotifications = notificationRepository
            .findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false, Pageable.unpaged());
        
        int count = 0;
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
            count++;
        }
        
        log.info("Marked {} notifications as read for user {}", count, userId);
        return count;
    }
    
    /**
     * Get all notifications for a user with pagination.
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of notifications
     */
    @Transactional(readOnly = true)
    public Page<Notification> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * Get unread notifications for a user with pagination.
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of unread notifications
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUnreadNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false, pageable);
    }
    
    /**
     * Get count of unread notifications for a user.
     * 
     * @param userId User ID
     * @return Number of unread notifications
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }
    
    /**
     * Get notifications by type for a user.
     * 
     * @param userId User ID
     * @param type Notification type
     * @param pageable Pagination parameters
     * @return Page of notifications
     */
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsByType(Long userId, NotificationType type, Pageable pageable) {
        return notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
    }
    
    /**
     * Delete old read notifications (cleanup task).
     * Should be run periodically (e.g., daily) to keep database clean.
     * 
     * @param daysToKeep Number of days to keep read notifications
     * @return Number of notifications deleted
     */
    @Transactional
    public int deleteOldReadNotifications(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        notificationRepository.deleteOldReadNotifications(cutoffDate, true);
        log.info("Deleted read notifications older than {} days", daysToKeep);
        return 0; // Note: Delete query doesn't return count in JPQL
    }
    
    /**
     * Create notification for task assignment.
     * 
     * @param userId Employee user ID
     * @param taskId Task ID
     * @param taskName Task name
     */
    @Transactional
    public void notifyTaskAssigned(Long userId, Long taskId, String taskName) {
        createNotification(
            userId,
            NotificationType.TASK_ASSIGNED,
            "新任務指派",
            String.format("您已被指派任務：%s", taskName),
            "TASK",
            taskId
        );
    }
    
    /**
     * Create notification for task hours running low.
     * 
     * @param pmUserId PM user ID
     * @param taskId Task ID
     * @param taskName Task name
     * @param remainingHours Remaining hours
     */
    @Transactional
    public void notifyTaskHoursLow(Long pmUserId, Long taskId, String taskName, Double remainingHours) {
        createNotification(
            pmUserId,
            NotificationType.TASK_HOURS_LOW,
            "任務時數不足警告",
            String.format("任務「%s」剩餘時數不足：%.1f 小時", taskName, remainingHours),
            "TASK",
            taskId
        );
    }
    
    /**
     * Create notification for hours request approval.
     * 
     * @param pmUserId PM user ID
     * @param requestId Hours request ID
     * @param projectName Project name
     * @param approvedHours Approved hours
     */
    @Transactional
    public void notifyHoursRequestApproved(Long pmUserId, Long requestId, String projectName, Double approvedHours) {
        createNotification(
            pmUserId,
            NotificationType.HOURS_REQUEST_APPROVED,
            "時數申請已核准",
            String.format("專案「%s」的時數申請已核准：%.1f 小時", projectName, approvedHours),
            "HOURS_REQUEST",
            requestId
        );
    }
    
    /**
     * Create notification for hours request rejection.
     * 
     * @param pmUserId PM user ID
     * @param requestId Hours request ID
     * @param projectName Project name
     * @param reason Rejection reason
     */
    @Transactional
    public void notifyHoursRequestRejected(Long pmUserId, Long requestId, String projectName, String reason) {
        createNotification(
            pmUserId,
            NotificationType.HOURS_REQUEST_REJECTED,
            "時數申請已拒絕",
            String.format("專案「%s」的時數申請已被拒絕。原因：%s", projectName, reason),
            "HOURS_REQUEST",
            requestId
        );
    }
    
    /**
     * Create notification for new hours request submission.
     * 
     * @param managerUserId Manager user ID
     * @param requestId Hours request ID
     * @param projectName Project name
     * @param requestedHours Requested hours
     * @param pmName PM name
     */
    @Transactional
    public void notifyHoursRequestSubmitted(Long managerUserId, Long requestId, String projectName, 
                                           Double requestedHours, String pmName) {
        createNotification(
            managerUserId,
            NotificationType.HOURS_REQUEST_SUBMITTED,
            "新的時數申請",
            String.format("PM %s 為專案「%s」申請 %.1f 小時", pmName, projectName, requestedHours),
            "HOURS_REQUEST",
            requestId
        );
    }
    
    /**
     * Create notification for project assignment to PM.
     * 
     * @param pmUserId PM user ID
     * @param projectId Project ID
     * @param projectName Project name
     */
    @Transactional
    public void notifyProjectAssigned(Long pmUserId, Long projectId, String projectName) {
        createNotification(
            pmUserId,
            NotificationType.PROJECT_ASSIGNED,
            "新專案指派",
            String.format("您已被指派為專案「%s」的 PM", projectName),
            "PROJECT",
            projectId
        );
    }
    
    /**
     * Create notifications for PM change on a project.
     * Notifies both the old PM (project removed) and new PM (project assigned).
     * 
     * @param oldPmUserId Old PM user ID (can be null if no previous PM)
     * @param newPmUserId New PM user ID
     * @param projectId Project ID
     * @param projectName Project name
     */
    @Transactional
    public void notifyPmChange(Long oldPmUserId, Long newPmUserId, Long projectId, String projectName) {
        // Notify old PM that project has been reassigned
        if (oldPmUserId != null && !oldPmUserId.equals(newPmUserId)) {
            createNotification(
                oldPmUserId,
                NotificationType.PROJECT_PM_CHANGED,
                "專案 PM 變更",
                String.format("專案「%s」已由其他 PM 接手", projectName),
                "PROJECT",
                projectId
            );
            log.info("Notified old PM {} of project {} reassignment", oldPmUserId, projectId);
        }
        
        // Notify new PM of project assignment
        createNotification(
            newPmUserId,
            NotificationType.PROJECT_ASSIGNED,
            "新專案指派",
            String.format("您已被指派為專案「%s」的 PM", projectName),
            "PROJECT",
            projectId
        );
        log.info("Notified new PM {} of project {} assignment", newPmUserId, projectId);
    }
}
