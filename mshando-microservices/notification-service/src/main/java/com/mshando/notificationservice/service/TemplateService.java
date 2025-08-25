package com.mshando.notificationservice.service;

import com.mshando.notificationservice.model.NotificationTemplate;
import com.mshando.notificationservice.model.NotificationType;
import com.mshando.notificationservice.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.Optional;

/**
 * Service for handling notification templates and content processing.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final TemplateEngine templateEngine;

    /**
     * Process template with parameters
     */
    @Transactional(readOnly = true)
    public String processTemplate(Long templateId, Map<String, String> parameters, NotificationType type) {
        log.debug("Processing template ID: {} for type: {}", templateId, type);

        Optional<NotificationTemplate> templateOpt = templateRepository.findByIdAndType(templateId, type);
        
        if (templateOpt.isEmpty()) {
            log.error("Template not found with ID: {} and type: {}", templateId, type);
            throw new IllegalArgumentException("Template not found: " + templateId);
        }

        NotificationTemplate template = templateOpt.get();
        
        if (!template.isActive()) {
            log.error("Template with ID: {} is not active", templateId);
            throw new IllegalStateException("Template is not active: " + templateId);
        }

        try {
            // Create Thymeleaf context with parameters
            Context context = new Context();
            if (parameters != null) {
                // Convert String values to Object for Thymeleaf
                parameters.forEach((key, value) -> context.setVariable(key, (Object) value));
            }

            // Process template content
            String processedContent = templateEngine.process(template.getContent(), context);
            
            log.debug("Template processed successfully for ID: {}", templateId);
            return processedContent;

        } catch (Exception e) {
            log.error("Failed to process template ID: {}. Error: {}", templateId, e.getMessage());
            throw new RuntimeException("Template processing failed", e);
        }
    }

    /**
     * Get template subject by ID
     */
    @Transactional(readOnly = true)
    public String getTemplateSubject(Long templateId) {
        log.debug("Getting template subject for ID: {}", templateId);

        return templateRepository.findById(templateId)
                .map(NotificationTemplate::getSubject)
                .orElseThrow(() -> {
                    log.error("Template not found with ID: {}", templateId);
                    return new IllegalArgumentException("Template not found: " + templateId);
                });
    }

    /**
     * Process template content without database lookup (for inline templates)
     */
    public String processInlineTemplate(String templateContent, Map<String, String> parameters) {
        log.debug("Processing inline template");

        try {
            Context context = new Context();
            if (parameters != null) {
                // Convert String values to Object for Thymeleaf
                parameters.forEach((key, value) -> context.setVariable(key, (Object) value));
            }

            return templateEngine.process(templateContent, context);

        } catch (Exception e) {
            log.error("Failed to process inline template. Error: {}", e.getMessage());
            throw new RuntimeException("Inline template processing failed", e);
        }
    }

    /**
     * Create or update template
     */
    @Transactional
    public NotificationTemplate saveTemplate(NotificationTemplate template) {
        log.info("Saving template: {} for type: {}", template.getName(), template.getType());
        
        // Validate template content by attempting to process it
        try {
            processInlineTemplate(template.getContent(), Map.of("test", "value"));
        } catch (Exception e) {
            log.error("Template validation failed for: {}. Error: {}", template.getName(), e.getMessage());
            throw new IllegalArgumentException("Invalid template syntax", e);
        }

        return templateRepository.save(template);
    }

    /**
     * Get template by name and type
     */
    @Transactional(readOnly = true)
    public Optional<NotificationTemplate> getTemplateByName(String name, NotificationType type) {
        log.debug("Getting template by name: {} and type: {}", name, type);
        return templateRepository.findByNameAndType(name, type);
    }

    /**
     * Activate template
     */
    @Transactional
    public void activateTemplate(Long templateId) {
        log.info("Activating template with ID: {}", templateId);
        templateRepository.findById(templateId)
                .ifPresentOrElse(
                        template -> {
                            template.setActive(true);
                            templateRepository.save(template);
                        },
                        () -> {
                            throw new IllegalArgumentException("Template not found: " + templateId);
                        }
                );
    }

    /**
     * Deactivate template
     */
    @Transactional
    public void deactivateTemplate(Long templateId) {
        log.info("Deactivating template with ID: {}", templateId);
        templateRepository.findById(templateId)
                .ifPresentOrElse(
                        template -> {
                            template.setActive(false);
                            templateRepository.save(template);
                        },
                        () -> {
                            throw new IllegalArgumentException("Template not found: " + templateId);
                        }
                );
    }
}
