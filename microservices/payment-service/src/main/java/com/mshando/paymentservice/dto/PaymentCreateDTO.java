package com.mshando.paymentservice.dto;

import com.mshando.paymentservice.model.PaymentMethod;
import com.mshando.paymentservice.model.PaymentType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for creating new payments.
 * 
 * This DTO contains the required information to initiate
 * a payment transaction in the system.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCreateDTO {
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    private Long taskerId;
    
    private Long taskId;
    
    private Long bidId;
    
    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    @DecimalMax(value = "100000.00", message = "Payment amount cannot exceed $100,000")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal amount;
    
    @Size(max = 3, message = "Currency code must be 3 characters")
    @Builder.Default
    private String currency = "USD";
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    private String metadata;
    
    // Payment method specific fields
    private String paymentMethodId; // For saved payment methods
    private String paymentIntentId; // For existing payment intents
    
    // Customer information for new payments
    private String customerEmail;
    private String customerName;
}
