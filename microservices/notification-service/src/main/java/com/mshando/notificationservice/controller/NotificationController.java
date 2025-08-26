package com.mshando.notificationservice.controller;

import com.mshando.notificationservice.dto.EmailNotificationDTO;
import com.mshando.notificationservice.dto.NotificationResponseDTO;
import com.mshando.notificationservice.dto.SmsNotificationDTO;
import com.mshando.notificationservice.model.NotificationStatus;
import com.mshando.notificationservice.model.NotificationType;
import com.mshando.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for notification management.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Notification management API")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Send email notification
     */
    @PostMapping("/email")
    @Operation(summary = "Send email notification", description = "Send an email notification to a recipient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Email notification accepted for processing"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CompletableFuture<NotificationResponseDTO>> sendEmailNotification(
            @Valid @RequestBody EmailNotificationDTO emailDto) {
        
        log.info("Received email notification request for: {}", emailDto.getRecipientEmail());
        
        CompletableFuture<NotificationResponseDTO> response = notificationService.sendEmailNotification(emailDto);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Send SMS notification
     */
    @PostMapping("/sms")
    @Operation(summary = "Send SMS notification", description = "Send an SMS notification to a recipient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "SMS notification accepted for processing"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CompletableFuture<NotificationResponseDTO>> sendSmsNotification(
            @Valid @RequestBody SmsNotificationDTO smsDto) {
        
        log.info("Received SMS notification request for: {}", smsDto.getRecipientPhoneNumber());
        
        CompletableFuture<NotificationResponseDTO> response = notificationService.sendSmsNotification(smsDto);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Get notification by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieve a notification by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification found"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<NotificationResponseDTO> getNotificationById(
            @Parameter(description = "Notification ID") @PathVariable Long id) {
        
        log.debug("Getting notification by ID: {}", id);
        
        return notificationService.getNotificationById(id)
                .map(notification -> ResponseEntity.ok(notification))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get notifications by recipient
     */
    @GetMapping("/recipient/{recipientId}")
    @Operation(summary = "Get notifications by recipient", description = "Retrieve notifications for a specific recipient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully")
    })
    public ResponseEntity<Page<NotificationResponseDTO>> getNotificationsByRecipient(
            @Parameter(description = "Recipient ID") @PathVariable Long recipientId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting notifications for recipient: {} (page: {}, size: {})", recipientId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponseDTO> notifications = notificationService
                .getNotificationsByRecipient(recipientId, pageable);
        
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notifications by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get notifications by status", description = "Retrieve notifications by their status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status")
    })
    public ResponseEntity<Page<NotificationResponseDTO>> getNotificationsByStatus(
            @Parameter(description = "Notification status") @PathVariable NotificationStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting notifications by status: {} (page: {}, size: {})", status, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponseDTO> notifications = notificationService
                .getNotificationsByStatus(status, pageable);
        
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notifications by type
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get notifications by type", description = "Retrieve notifications by their type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid type")
    })
    public ResponseEntity<Page<NotificationResponseDTO>> getNotificationsByType(
            @Parameter(description = "Notification type") @PathVariable NotificationType type,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting notifications by type: {} (page: {}, size: {})", type, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponseDTO> notifications = notificationService
                .getNotificationsByType(type, pageable);
        
        return ResponseEntity.ok(notifications);
    }

    /**
     * Retry failed notification
     */
    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry failed notification", description = "Retry a failed notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Retry accepted for processing"),
            @ApiResponse(responseCode = "400", description = "Cannot retry notification"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<CompletableFuture<NotificationResponseDTO>> retryNotification(
            @Parameter(description = "Notification ID") @PathVariable Long id) {
        
        log.info("Retrying notification with ID: {}", id);
        
        try {
            CompletableFuture<NotificationResponseDTO> response = notificationService.retryNotification(id);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Notification not found for retry: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Cannot retry notification {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancel notification
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel notification", description = "Cancel a pending or scheduled notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notification cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot cancel notification"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<Void> cancelNotification(
            @Parameter(description = "Notification ID") @PathVariable Long id) {
        
        log.info("Cancelling notification with ID: {}", id);
        
        try {
            notificationService.cancelNotification(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Notification not found for cancellation: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Cannot cancel notification {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
