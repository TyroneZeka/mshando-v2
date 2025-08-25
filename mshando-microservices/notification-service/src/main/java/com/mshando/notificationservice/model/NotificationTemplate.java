package com.mshando.notificationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a notification template.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Entity
@Table(name = "notification_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id", unique = true, nullable = false)
    private String templateId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "subject")
    private String subject;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "language")
    @Builder.Default
    private String language = "en";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Check if template is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set template active status
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
