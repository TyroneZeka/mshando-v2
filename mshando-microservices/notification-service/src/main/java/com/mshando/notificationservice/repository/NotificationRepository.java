package com.mshando.notificationservice.repository;

import com.mshando.notificationservice.model.Notification;
import com.mshando.notificationservice.model.NotificationStatus;
import com.mshando.notificationservice.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Notification entity operations.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find notifications by recipient ID
     */
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    /**
     * Find notifications by recipient ID and type
     */
    Page<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(
            Long recipientId, NotificationType type, Pageable pageable);

    /**
     * Find notifications by status
     */
    List<Notification> findByStatus(NotificationStatus status);

    /**
     * Find notifications by status with pagination
     */
    Page<Notification> findByStatusOrderByCreatedAtDesc(NotificationStatus status, Pageable pageable);

    /**
     * Find notifications by type with pagination
     */
    Page<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type, Pageable pageable);

    /**
     * Find failed notifications that can be retried
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.retryCount < n.maxRetries")
    List<Notification> findFailedNotificationsForRetry();

    /**
     * Find scheduled notifications ready to be sent
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.scheduledFor <= :now")
    List<Notification> findScheduledNotificationsReadyToSend(@Param("now") LocalDateTime now);

    /**
     * Find scheduled notifications due for processing
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'SCHEDULED' AND n.scheduledFor <= :now")
    List<Notification> findScheduledNotificationsDue(@Param("now") LocalDateTime now);

    /**
     * Find notifications by reference type and ID
     */
    List<Notification> findByReferenceTypeAndReferenceId(String referenceType, String referenceId);

    /**
     * Count notifications by status
     */
    long countByStatus(NotificationStatus status);

    /**
     * Count notifications by recipient and status
     */
    long countByRecipientIdAndStatus(Long recipientId, NotificationStatus status);

    /**
     * Find notifications created between dates
     */
    Page<Notification> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Find notifications by type and status
     */
    List<Notification> findByTypeAndStatus(NotificationType type, NotificationStatus status);

    /**
     * Delete old notifications
     */
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate AND n.status IN ('SENT', 'DELIVERED', 'FAILED')")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}
