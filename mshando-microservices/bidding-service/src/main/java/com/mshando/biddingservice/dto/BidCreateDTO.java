package com.mshando.biddingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "BidCreate", description = "Request body for creating a new bid on a task")
public class BidCreateDTO {
    
    @Schema(description = "ID of the task to bid on", 
            example = "123", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Task ID is required")
    private Long taskId;
    
    @Schema(description = "Bid amount in USD", 
            example = "75.50", 
            minimum = "5.0", 
            maximum = "10000.0",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "5.0", message = "Minimum bid amount is $5.00")
    @DecimalMax(value = "10000.0", message = "Maximum bid amount is $10,000.00")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal amount;
    
    @Schema(description = "Optional message to the task owner", 
            example = "I have extensive experience with this type of work and can complete it efficiently.",
            maxLength = 1000)
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message;
    
    @Schema(description = "Estimated time to complete the task in hours", 
            example = "24", 
            minimum = "1", 
            maximum = "720")
    @Min(value = 1, message = "Estimated completion time must be at least 1 hour")
    @Max(value = 720, message = "Estimated completion time cannot exceed 720 hours (30 days)")
    private Integer estimatedCompletionHours;
}
