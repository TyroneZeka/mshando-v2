package com.mshando.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for payment refund requests.
 * 
 * This DTO contains the information needed to process
 * a refund for an existing payment.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRefundDTO {
    
    private BigDecimal refundAmount; // If null, full refund
    
    @NotBlank(message = "Refund reason is required")
    @Size(max = 500, message = "Refund reason cannot exceed 500 characters")
    private String reason;
    
    private String metadata;
    
    // Whether to refund service fee as well
    @Builder.Default
    private Boolean refundServiceFee = false;
}
