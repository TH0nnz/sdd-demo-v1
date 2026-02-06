package com.example.timesheet.domain.repository;

import com.example.timesheet.domain.entity.Notification;
import com.example.timesheet.domain.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Notification entity.
 * Provides database access and query methods for notifications.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find all notifications for a user, ordered by creation date descending
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of notifications
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Find unread notifications for a user
     * @param userId User ID
     * @param isRead Read status
     * @param pageable Pagination parameters
     * @return Page of notifications
     */
    Page<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead, Pageable pageable);
    
    /**
     * Count unread notifications for a user
     * @param userId User ID
     * @param isRead Read status (false for unread)
     * @return Number of unread notifications
     */
    long countByUserIdAndIsRead(Long userId, Boolean isRead);
    
    /**
     * Find notifications by type for a user
     * @param userId User ID
     * @param type Notification type
     * @param pageable Pagination parameters
     * @return Page of notifications
     */
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type, Pageable pageable);
    
    /**
     * Find notifications related to a specific entity
     * @param entityType Entity type (PROJECT, TASK, HOURS_REQUEST)
     * @param entityId Entity ID
     * @return List of related notifications
     */
    List<Notification> findByRelatedEntityTypeAndRelatedEntityId(String entityType, Long entityId);
    
    /**
     * Delete old read notifications (for cleanup)
     * @param cutoffDate Date before which notifications should be deleted
     * @param isRead Only delete read notifications
     */
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate AND n.isRead = :isRead")
    void deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("isRead") Boolean isRead);
}
