package com.mshando.biddingservice.service;

import com.mshando.biddingservice.dto.TaskInfoDTO;
import com.mshando.biddingservice.dto.TaskerInfoDTO;

/**
 * Service interface for external microservice communication.
 * 
 * Handles communication with User Service and Task Service
 * to retrieve necessary information for bid operations.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
public interface ExternalService {
    
    /**
     * Get task information from Task Service
     */
    TaskInfoDTO getTaskInfo(Long taskId);
    
    /**
     * Get tasker information from User Service
     */
    TaskerInfoDTO getTaskerInfo(Long taskerId);
    
    /**
     * Update task status in Task Service
     */
    void updateTaskStatus(Long taskId, String status, Long assignedTaskerId);
    
    /**
     * Validate user exists and has correct role
     */
    boolean validateUserRole(Long userId, String expectedRole);
}
