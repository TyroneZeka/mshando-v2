package com.mshando.notificationservice.service;

import com.mshando.notificationservice.dto.EmailNotificationDTO;
import com.mshando.notificationservice.dto.NotificationResponseDTO;
import com.mshando.notificationservice.dto.SmsNotificationDTO;
import com.mshando.notificationservice.model.Notification;
import com.mshando.notificationservice.model.NotificationStatus;
import com.mshando.notificationservice.model.NotificationType;
import com.mshando.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Main service for managing notifications across all channels.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    /**
     * Send email notification
     */
    @Async
    public CompletableFuture<NotificationResponseDTO> sendEmailNotification(EmailNotificationDTO emailDto) {
        log.info("Processing email notification request for recipient: {}", emailDto.getRecipientEmail());
        return emailService.sendEmailAsync(emailDto);
    }

    /**
     * Send SMS notification
     */
    @Async
    public CompletableFuture<NotificationResponseDTO> sendSmsNotification(SmsNotificationDTO smsDto) {
        log.info("Processing SMS notification request for recipient: {}", smsDto.getRecipientPhoneNumber());
        return smsService.sendSmsAsync(smsDto);
    }

    /**
     * Get notification by ID
     */
    @Transactional(readOnly = true)
    public Optional<NotificationResponseDTO> getNotificationById(Long id) {
        log.debug("Getting notification by ID: {}", id);
        return notificationRepository.findById(id)
                .map(this::mapToResponseDTO);
    }

    /**
     * Get notifications by recipient ID
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getNotificationsByRecipient(Long recipientId, Pageable pageable) {
        log.debug("Getting notifications for recipient: {}", recipientId);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable)
                .map(this::mapToResponseDTO);
    }

    /**
     * Get notifications by status
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getNotificationsByStatus(NotificationStatus status, Pageable pageable) {
        log.debug("Getting notifications by status: {}", status);
        return notificationRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(this::mapToResponseDTO);
    }

    /**
     * Get notifications by type
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getNotificationsByType(NotificationType type, Pageable pageable) {
        log.debug("Getting notifications by type: {}", type);
        return notificationRepository.findByTypeOrderByCreatedAtDesc(type, pageable)
                .map(this::mapToResponseDTO);
    }

    /**
     * Retry failed notification
     */
    @Transactional
    public CompletableFuture<NotificationResponseDTO> retryNotification(Long notificationId) {
        log.info("Retrying notification with ID: {}", notificationId);

        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            throw new IllegalArgumentException("Notification not found: " + notificationId);
        }

        Notification notification = notificationOpt.get();
        
        if (notification.getStatus() != NotificationStatus.FAILED) {
            throw new IllegalStateException("Can only retry failed notifications");
        }

        if (notification.getRetryCount() >= notification.getMaxRetries()) {
            throw new IllegalStateException("Maximum retry attempts exceeded");
        }

        // Increment retry count and reset status
        notification.incrementRetryCount();
        notification.setStatus(NotificationStatus.PENDING);
        notification.setErrorMessage(null);
        notification = notificationRepository.save(notification);

        // Re-send based on type
        if (notification.getType() == NotificationType.EMAIL) {
            EmailNotificationDTO emailDto = mapToEmailDTO(notification);
            return emailService.sendEmailAsync(emailDto);
        } else if (notification.getType() == NotificationType.SMS) {
            SmsNotificationDTO smsDto = mapToSmsDTO(notification);
            return smsService.sendSmsAsync(smsDto);
        }

        throw new IllegalStateException("Unsupported notification type: " + notification.getType());
    }

    /**
     * Cancel scheduled notification
     */
    @Transactional
    public void cancelNotification(Long notificationId) {
        log.info("Cancelling notification with ID: {}", notificationId);
        
        notificationRepository.findById(notificationId)
                .ifPresentOrElse(
                        notification -> {
                            if (notification.getStatus() == NotificationStatus.PENDING || 
                                notification.getStatus() == NotificationStatus.SCHEDULED) {
                                notification.setStatus(NotificationStatus.CANCELLED);
                                notificationRepository.save(notification);
                            } else {
                                throw new IllegalStateException("Cannot cancel notification in status: " + notification.getStatus());
                            }
                        },
                        () -> {
                            throw new IllegalArgumentException("Notification not found: " + notificationId);
                        }
                );
    }

    /**
     * Process scheduled notifications
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void processScheduledNotifications() {
        log.debug("Processing scheduled notifications");
        
        LocalDateTime now = LocalDateTime.now();
        List<Notification> scheduledNotifications = notificationRepository
                .findScheduledNotificationsDue(now);

        log.info("Found {} scheduled notifications due for processing", scheduledNotifications.size());

        for (Notification notification : scheduledNotifications) {
            try {
                if (notification.getType() == NotificationType.EMAIL) {
                    EmailNotificationDTO emailDto = mapToEmailDTO(notification);
                    emailService.sendEmailAsync(emailDto);
                } else if (notification.getType() == NotificationType.SMS) {
                    SmsNotificationDTO smsDto = mapToSmsDTO(notification);
                    smsService.sendSmsAsync(smsDto);
                }
            } catch (Exception e) {
                log.error("Failed to process scheduled notification ID: {}. Error: {}", 
                        notification.getId(), e.getMessage());
                notification.markAsFailed("Scheduled processing failed: " + e.getMessage());
                notificationRepository.save(notification);
            }
        }
    }

    /**
     * Retry failed notifications
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void retryFailedNotifications() {
        log.debug("Processing retry for failed notifications");
        
        List<Notification> failedNotifications = notificationRepository.findFailedNotificationsForRetry();
        
        log.info("Found {} failed notifications eligible for retry", failedNotifications.size());

        for (Notification notification : failedNotifications) {
            try {
                retryNotification(notification.getId());
            } catch (Exception e) {
                log.error("Failed to retry notification ID: {}. Error: {}", 
                        notification.getId(), e.getMessage());
            }
        }
    }

    /**
     * Clean up old notifications
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void cleanupOldNotifications() {
        log.info("Starting cleanup of old notifications");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90); // Keep 90 days
        int deletedCount = notificationRepository.deleteOldNotifications(cutoffDate);
        
        log.info("Cleaned up {} old notifications", deletedCount);
    }

    /**
     * Map notification entity to response DTO
     */
    private NotificationResponseDTO mapToResponseDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .recipientEmail(notification.getRecipientEmail())
                .recipientPhoneNumber(notification.getRecipientPhoneNumber())
                .type(notification.getType())
                .status(notification.getStatus())
                .priority(notification.getPriority())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .templateId(notification.getTemplateId())
                .templateParameters(notification.getTemplateParameters())
                .sentAt(notification.getSentAt())
                .deliveredAt(notification.getDeliveredAt())
                .scheduledFor(notification.getScheduledFor())
                .retryCount(notification.getRetryCount())
                .maxRetries(notification.getMaxRetries())
                .errorMessage(notification.getErrorMessage())
                .externalId(notification.getExternalId())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    /**
     * Map notification entity to email DTO
     */
    private EmailNotificationDTO mapToEmailDTO(Notification notification) {
        return EmailNotificationDTO.builder()
                .recipientId(notification.getRecipientId())
                .recipientEmail(notification.getRecipientEmail())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .priority(notification.getPriority())
                .templateId(notification.getTemplateId())
                .templateParameters(notification.getTemplateParameters())
                .scheduledFor(notification.getScheduledFor())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .build();
    }

    /**
     * Map notification entity to SMS DTO
     */
    private SmsNotificationDTO mapToSmsDTO(Notification notification) {
        return SmsNotificationDTO.builder()
                .recipientId(notification.getRecipientId())
                .recipientPhoneNumber(notification.getRecipientPhoneNumber())
                .content(notification.getContent())
                .priority(notification.getPriority())
                .templateId(notification.getTemplateId())
                .templateParameters(notification.getTemplateParameters())
                .scheduledFor(notification.getScheduledFor())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .build();
    }
}
