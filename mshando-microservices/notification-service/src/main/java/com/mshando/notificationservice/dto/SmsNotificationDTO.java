package com.mshando.notificationservice.dto;

import com.mshando.notificationservice.model.NotificationPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for creating SMS notifications.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsNotificationDTO {

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotBlank(message = "Recipient phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Valid phone number is required")
    private String recipientPhoneNumber;

    @NotBlank(message = "Message content is required")
    private String content;

    private Long templateId;

    private Map<String, String> templateParameters;

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    private LocalDateTime scheduledFor;

    private String referenceType;

    private String referenceId;
}
