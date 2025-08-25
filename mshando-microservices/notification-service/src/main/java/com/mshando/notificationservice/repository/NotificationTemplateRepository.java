package com.mshando.notificationservice.repository;

import com.mshando.notificationservice.model.NotificationTemplate;
import com.mshando.notificationservice.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for NotificationTemplate entity operations.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    /**
     * Find template by template ID
     */
    Optional<NotificationTemplate> findByTemplateId(String templateId);

    /**
     * Find template by ID and type
     */
    Optional<NotificationTemplate> findByIdAndType(Long id, NotificationType type);

    /**
     * Find template by name and type
     */
    Optional<NotificationTemplate> findByNameAndType(String name, NotificationType type);

    /**
     * Find active templates by type
     */
    List<NotificationTemplate> findByTypeAndActiveTrue(NotificationType type);

    /**
     * Find all active templates
     */
    List<NotificationTemplate> findByActiveTrue();

    /**
     * Find templates by language
     */
    List<NotificationTemplate> findByLanguageAndActiveTrue(String language);

    /**
     * Find template by template ID and language
     */
    Optional<NotificationTemplate> findByTemplateIdAndLanguageAndActiveTrue(
            String templateId, String language);

    /**
     * Check if template ID exists
     */
    boolean existsByTemplateId(String templateId);
}
