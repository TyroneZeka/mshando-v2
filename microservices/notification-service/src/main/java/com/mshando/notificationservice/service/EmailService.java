package com.mshando.notificationservice.service;

import com.mshando.notificationservice.dto.EmailNotificationDTO;
import com.mshando.notificationservice.dto.NotificationResponseDTO;
import com.mshando.notificationservice.model.Notification;
import com.mshando.notificationservice.model.NotificationStatus;
import com.mshando.notificationservice.model.NotificationType;
import com.mshando.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling email notifications.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;
    private final TemplateService templateService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send email notification asynchronously
     */
    @Async
    @Transactional
    public CompletableFuture<NotificationResponseDTO> sendEmailAsync(EmailNotificationDTO emailDto) {
        log.info("Sending email notification to: {}", emailDto.getRecipientEmail());

        // Create notification record
        Notification notification = createNotificationRecord(emailDto);
        notification = notificationRepository.save(notification);

        try {
            if (!emailEnabled) {
                log.warn("Email sending is disabled. Marking notification as sent for testing purposes.");
                notification.markAsSent();
                notificationRepository.save(notification);
                return CompletableFuture.completedFuture(mapToResponseDTO(notification));
            }

            // Process template if provided
            String processedContent = emailDto.getContent();
            String processedSubject = emailDto.getSubject();

            if (emailDto.getTemplateId() != null) {
                processedContent = templateService.processTemplate(
                    emailDto.getTemplateId(), emailDto.getTemplateParameters(), NotificationType.EMAIL);
                processedSubject = templateService.getTemplateSubject(emailDto.getTemplateId());
            }

            // Send email
            sendHtmlEmail(emailDto.getRecipientEmail(), processedSubject, processedContent);

            // Update notification status
            notification.markAsSent();
            notification = notificationRepository.save(notification);

            log.info("Email sent successfully to: {}", emailDto.getRecipientEmail());
            return CompletableFuture.completedFuture(mapToResponseDTO(notification));

        } catch (Exception e) {
            log.error("Failed to send email to: {}. Error: {}", emailDto.getRecipientEmail(), e.getMessage());
            notification.markAsFailed(e.getMessage());
            notificationRepository.save(notification);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send simple text email
     */

    /**
     * Send HTML email
     */
    private void sendHtmlEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true); // true indicates HTML content
        helper.setSentDate(new java.util.Date());

        mailSender.send(message);
    }

    /**
     * Create notification record from DTO
     */
    private Notification createNotificationRecord(EmailNotificationDTO emailDto) {
        return Notification.builder()
                .recipientId(emailDto.getRecipientId())
                .recipientEmail(emailDto.getRecipientEmail())
                .type(NotificationType.EMAIL)
                .status(NotificationStatus.PENDING)
                .priority(emailDto.getPriority())
                .subject(emailDto.getSubject())
                .content(emailDto.getContent())
                .templateId(emailDto.getTemplateId())
                .templateParameters(emailDto.getTemplateParameters())
                .scheduledFor(emailDto.getScheduledFor())
                .referenceType(emailDto.getReferenceType())
                .referenceId(emailDto.getReferenceId())
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
