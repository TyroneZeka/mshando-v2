package com.mshando.notificationservice.service;

import com.mshando.notificationservice.dto.SmsNotificationDTO;
import com.mshando.notificationservice.dto.NotificationResponseDTO;
import com.mshando.notificationservice.model.Notification;
import com.mshando.notificationservice.model.NotificationStatus;
import com.mshando.notificationservice.model.NotificationType;
import com.mshando.notificationservice.repository.NotificationRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling SMS notifications using Twilio.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final NotificationRepository notificationRepository;
    private final TemplateService templateService;

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @Value("${notification.sms.enabled:true}")
    private boolean smsEnabled;

    @PostConstruct
    public void initTwilio() {
        if (smsEnabled && accountSid != null && authToken != null) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio initialized successfully");
        } else {
            log.warn("Twilio not initialized. SMS functionality may be disabled or missing configuration.");
        }
    }

    /**
     * Send SMS notification asynchronously
     */
    @Async
    @Transactional
    public CompletableFuture<NotificationResponseDTO> sendSmsAsync(SmsNotificationDTO smsDto) {
        log.info("Sending SMS notification to: {}", smsDto.getRecipientPhoneNumber());

        // Create notification record
        Notification notification = createNotificationRecord(smsDto);
        notification = notificationRepository.save(notification);

        try {
            if (!smsEnabled) {
                log.warn("SMS sending is disabled. Marking notification as sent for testing purposes.");
                notification.markAsSent();
                notificationRepository.save(notification);
                return CompletableFuture.completedFuture(mapToResponseDTO(notification));
            }

            // Process template if provided
            String processedContent = smsDto.getContent();
            if (smsDto.getTemplateId() != null) {
                processedContent = templateService.processTemplate(
                    smsDto.getTemplateId(), smsDto.getTemplateParameters(), NotificationType.SMS);
            }

            // Send SMS using Twilio
            Message message = Message.creator(
                    new PhoneNumber(smsDto.getRecipientPhoneNumber()),
                    new PhoneNumber(fromPhoneNumber),
                    processedContent)
                    .create();

            // Update notification with external ID and mark as sent
            notification.setExternalId(message.getSid());
            notification.markAsSent();
            notification = notificationRepository.save(notification);

            log.info("SMS sent successfully to: {}. Twilio SID: {}", 
                    smsDto.getRecipientPhoneNumber(), message.getSid());
            return CompletableFuture.completedFuture(mapToResponseDTO(notification));

        } catch (Exception e) {
            log.error("Failed to send SMS to: {}. Error: {}", 
                    smsDto.getRecipientPhoneNumber(), e.getMessage());
            notification.markAsFailed(e.getMessage());
            notificationRepository.save(notification);
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

    /**
     * Check SMS delivery status using Twilio
     */
    @Transactional
    public void checkDeliveryStatus(String notificationId) {
        notificationRepository.findById(Long.valueOf(notificationId))
                .ifPresent(notification -> {
                    if (notification.getExternalId() != null && notification.getType() == NotificationType.SMS) {
                        try {
                            Message message = Message.fetcher(notification.getExternalId()).fetch();
                            String status = message.getStatus().toString();
                            
                            log.info("SMS delivery status for notification {}: {}", 
                                    notificationId, status);
                            
                            // Update notification based on Twilio status
                            switch (status.toLowerCase()) {
                                case "delivered":
                                    notification.markAsDelivered();
                                    break;
                                case "failed":
                                case "undelivered":
                                    notification.markAsFailed("SMS delivery failed: " + status);
                                    break;
                                // Other statuses like 'sent', 'queued', 'sending' don't need action
                            }
                            
                            notificationRepository.save(notification);
                        } catch (Exception e) {
                            log.error("Failed to check SMS delivery status for notification {}: {}", 
                                    notificationId, e.getMessage());
                        }
                    }
                });
    }

    /**
     * Create notification record from DTO
     */
    private Notification createNotificationRecord(SmsNotificationDTO smsDto) {
        return Notification.builder()
                .recipientId(smsDto.getRecipientId())
                .recipientPhoneNumber(smsDto.getRecipientPhoneNumber())
                .type(NotificationType.SMS)
                .status(NotificationStatus.PENDING)
                .priority(smsDto.getPriority())
                .content(smsDto.getContent())
                .templateId(smsDto.getTemplateId())
                .templateParameters(smsDto.getTemplateParameters())
                .scheduledFor(smsDto.getScheduledFor())
                .referenceType(smsDto.getReferenceType())
                .referenceId(smsDto.getReferenceId())
                .build();
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
}
