package com.mshando.notificationservice.dto;

import com.mshando.notificationservice.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for notification template operations.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateDTO {

    private Long id;

    @NotBlank(message = "Template ID is required")
    private String templateId;

    @NotBlank(message = "Template name is required")
    private String name;

    private String description;

    private NotificationType type;

    private String subject;

    @NotBlank(message = "Template content is required")
    private String content;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private String language = "en";
}
