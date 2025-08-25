package com.mshando.notificationservice.controller;

import com.mshando.notificationservice.dto.NotificationTemplateDTO;
import com.mshando.notificationservice.model.NotificationTemplate;
import com.mshando.notificationservice.model.NotificationType;
import com.mshando.notificationservice.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for notification template management.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/notifications/templates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Templates", description = "Notification template management API")
public class TemplateController {

    private final TemplateService templateService;

    /**
     * Create or update notification template
     */
    @PostMapping
    @Operation(summary = "Create or update template", description = "Create a new notification template or update an existing one")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Template created successfully"),
            @ApiResponse(responseCode = "200", description = "Template updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid template data")
    })
    public ResponseEntity<NotificationTemplate> saveTemplate(
            @Valid @RequestBody NotificationTemplateDTO templateDto) {
        
        log.info("Saving template: {} for type: {}", templateDto.getName(), templateDto.getType());
        
        try {
            NotificationTemplate template = mapFromDTO(templateDto);
            NotificationTemplate savedTemplate = templateService.saveTemplate(template);
            
            HttpStatus status = templateDto.getId() != null ? HttpStatus.OK : HttpStatus.CREATED;
            return ResponseEntity.status(status).body(savedTemplate);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid template data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get template by name and type
     */
    @GetMapping("/{name}/type/{type}")
    @Operation(summary = "Get template by name and type", description = "Retrieve a template by its name and type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template found"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    public ResponseEntity<NotificationTemplate> getTemplateByNameAndType(
            @Parameter(description = "Template name") @PathVariable String name,
            @Parameter(description = "Notification type") @PathVariable NotificationType type) {
        
        log.debug("Getting template by name: {} and type: {}", name, type);
        
        return templateService.getTemplateByName(name, type)
                .map(template -> ResponseEntity.ok(template))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Activate template
     */
    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate template", description = "Activate a notification template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Template activated successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    public ResponseEntity<Void> activateTemplate(
            @Parameter(description = "Template ID") @PathVariable Long id) {
        
        log.info("Activating template with ID: {}", id);
        
        try {
            templateService.activateTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Template not found for activation: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deactivate template
     */
    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate template", description = "Deactivate a notification template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Template deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    public ResponseEntity<Void> deactivateTemplate(
            @Parameter(description = "Template ID") @PathVariable Long id) {
        
        log.info("Deactivating template with ID: {}", id);
        
        try {
            templateService.deactivateTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Template not found for deactivation: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Map DTO to entity
     */
    private NotificationTemplate mapFromDTO(NotificationTemplateDTO dto) {
        NotificationTemplate template = new NotificationTemplate();
        template.setId(dto.getId());
        template.setName(dto.getName());
        template.setType(dto.getType());
        template.setSubject(dto.getSubject());
        template.setContent(dto.getContent());
        template.setDescription(dto.getDescription());
        template.setActive(dto.isActive());
        return template;
    }
}
