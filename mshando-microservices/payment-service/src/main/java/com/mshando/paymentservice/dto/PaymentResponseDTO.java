package com.mshando.paymentservice.dto;

import com.mshando.paymentservice.model.PaymentMethod;
import com.mshando.paymentservice.model.PaymentStatus;
import com.mshando.paymentservice.model.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Payment responses.
 * 
 * This DTO is used when returning payment information to clients,
 * including all relevant payment details and status information.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    
    private Long id;
    private Long customerId;
    private Long taskerId;
    private Long taskId;
    private Long bidId;
    private BigDecimal amount;
    private BigDecimal serviceFee;
    private BigDecimal netAmount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentType paymentType;
    private PaymentStatus status;
    private String externalTransactionId;
    private String paymentIntentId;
    private String description;
    private String metadata;
    private String failureReason;
    private Integer retryCount;
    private Integer maxRetries;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private LocalDateTime refundedAt;
    
    private Long version;
    
    // Additional information
    private CustomerInfoDTO customerInfo;
    private TaskerInfoDTO taskerInfo;
    private TaskInfoDTO taskInfo;
}
