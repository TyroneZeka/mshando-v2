package com.mshando.notificationservice.dto;

import com.mshando.notificationservice.model.NotificationPriority;
import com.mshando.notificationservice.model.NotificationStatus;
import com.mshando.notificationservice.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for notification response.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {

    private Long id;
    private Long recipientId;
    private String recipientEmail;
    private String recipientPhoneNumber;
    private NotificationType type;
    private NotificationStatus status;
    private NotificationPriority priority;
    private String subject;
    private String content;
    private Long templateId;
    private Map<String, String> templateParameters;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime scheduledFor;
    private Integer retryCount;
    private Integer maxRetries;
    private String errorMessage;
    private String externalId;
    private String referenceType;
    private String referenceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
