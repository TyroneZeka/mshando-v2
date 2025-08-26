package com.mshando.notificationservice.dto;

import com.mshando.notificationservice.model.NotificationPriority;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for creating email notifications.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationDTO {

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Valid email address is required")
    private String recipientEmail;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Content is required")
    private String content;

    private Long templateId;

    private Map<String, String> templateParameters;

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    private LocalDateTime scheduledFor;

    private String referenceType;

    private String referenceId;
}
