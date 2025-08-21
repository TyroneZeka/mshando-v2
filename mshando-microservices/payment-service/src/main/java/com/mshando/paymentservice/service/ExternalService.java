package com.mshando.paymentservice.service;

import com.mshando.paymentservice.dto.CustomerInfoDTO;
import com.mshando.paymentservice.dto.TaskerInfoDTO;
import com.mshando.paymentservice.dto.TaskInfoDTO;

/**
 * Service interface for external service integrations.
 * 
 * Handles communication with other microservices in the platform.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
public interface ExternalService {
    
    /**
     * Get customer information from user service
     */
    CustomerInfoDTO getCustomerInfo(Long customerId);
    
    /**
     * Get tasker information from user service
     */
    TaskerInfoDTO getTaskerInfo(Long taskerId);
    
    /**
     * Get task information from task service
     */
    TaskInfoDTO getTaskInfo(Long taskId);
    
    /**
     * Validate customer exists and is active
     */
    boolean validateCustomer(Long customerId);
    
    /**
     * Validate tasker exists and is active
     */
    boolean validateTasker(Long taskerId);
    
    /**
     * Validate task exists and is available for payment
     */
    boolean validateTask(Long taskId);
    
    /**
     * Notify customer about payment status
     */
    void notifyCustomer(Long customerId, String message);
    
    /**
     * Notify tasker about payment status
     */
    void notifyTasker(Long taskerId, String message);
}
