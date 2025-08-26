package com.mshando.biddingservice.service.impl;

import com.mshando.biddingservice.dto.TaskInfoDTO;
import com.mshando.biddingservice.dto.TaskerInfoDTO;
import com.mshando.biddingservice.service.ExternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Implementation of ExternalService for inter-service communication.
 * 
 * Handles HTTP communication with User Service and Task Service
 * using WebClient for reactive programming.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalServiceImpl implements ExternalService {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Value("${services.task-service.url}")
    private String taskServiceUrl;

    @Override
    public TaskInfoDTO getTaskInfo(Long taskId) {
        try {
            log.debug("Fetching task info for task ID: {}", taskId);
            
            WebClient webClient = webClientBuilder.baseUrl(taskServiceUrl).build();
            
            return webClient.get()
                    .uri("/api/v1/tasks/{taskId}/info", taskId)
                    .retrieve()
                    .bodyToMono(TaskInfoDTO.class)
                    .block();
                    
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Task {} not found", taskId);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch task info for task {}: {}", taskId, e.getMessage());
            throw new RuntimeException("Failed to communicate with Task Service", e);
        }
    }

    @Override
    public TaskerInfoDTO getTaskerInfo(Long taskerId) {
        try {
            log.debug("Fetching tasker info for tasker ID: {}", taskerId);
            
            WebClient webClient = webClientBuilder.baseUrl(userServiceUrl).build();
            
            return webClient.get()
                    .uri("/api/v1/users/{userId}/tasker-info", taskerId)
                    .retrieve()
                    .bodyToMono(TaskerInfoDTO.class)
                    .block();
                    
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Tasker {} not found", taskerId);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch tasker info for tasker {}: {}", taskerId, e.getMessage());
            throw new RuntimeException("Failed to communicate with User Service", e);
        }
    }

    @Override
    public void updateTaskStatus(Long taskId, String status, Long assignedTaskerId) {
        try {
            log.debug("Updating task {} status to {} with assigned tasker {}", taskId, status, assignedTaskerId);
            
            WebClient webClient = webClientBuilder.baseUrl(taskServiceUrl).build();
            
            TaskStatusUpdateDTO updateDTO = TaskStatusUpdateDTO.builder()
                    .status(status)
                    .assignedTaskerId(assignedTaskerId)
                    .build();
            
            webClient.patch()
                    .uri("/api/v1/tasks/{taskId}/status", taskId)
                    .bodyValue(updateDTO)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
                    
            log.info("Successfully updated task {} status to {}", taskId, status);
            
        } catch (Exception e) {
            log.error("Failed to update task {} status: {}", taskId, e.getMessage());
            throw new RuntimeException("Failed to update task status", e);
        }
    }

    @Override
    public boolean validateUserRole(Long userId, String expectedRole) {
        try {
            log.debug("Validating user {} has role {}", userId, expectedRole);
            
            WebClient webClient = webClientBuilder.baseUrl(userServiceUrl).build();
            
            UserRoleDTO userRole = webClient.get()
                    .uri("/api/v1/users/{userId}/role", userId)
                    .retrieve()
                    .bodyToMono(UserRoleDTO.class)
                    .block();
            
            return userRole != null && expectedRole.equals(userRole.getRole());
            
        } catch (Exception e) {
            log.error("Failed to validate user {} role: {}", userId, e.getMessage());
            return false;
        }
    }

    // Inner DTOs for communication
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class TaskStatusUpdateDTO {
        private String status;
        private Long assignedTaskerId;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class UserRoleDTO {
        private String role;
    }
}
