package com.mshando.biddingservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for creating new bids.
 * 
 * This DTO contains the required information for a tasker
 * to place a bid on a specific task.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidCreateDTO {
    
    @NotNull(message = "Task ID is required")
    private Long taskId;
    
    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "5.0", message = "Minimum bid amount is $5.00")
    @DecimalMax(value = "10000.0", message = "Maximum bid amount is $10,000.00")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal amount;
    
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message;
    
    @Min(value = 1, message = "Estimated completion time must be at least 1 hour")
    @Max(value = 720, message = "Estimated completion time cannot exceed 720 hours (30 days)")
    private Integer estimatedCompletionHours;
}
