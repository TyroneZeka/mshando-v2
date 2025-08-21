package com.mshando.biddingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for updating existing bids.
 * 
 * This DTO allows taskers to modify their pending bids,
 * including amount, message, and estimated completion time.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "BidUpdate", description = "Request body for updating an existing pending bid")
public class BidUpdateDTO {
    
    @Schema(description = "Updated bid amount in USD", 
            example = "85.00", 
            minimum = "5.0", 
            maximum = "10000.0",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "5.0", message = "Minimum bid amount is $5.00")
    @DecimalMax(value = "10000.0", message = "Maximum bid amount is $10,000.00")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal amount;
    
    @Schema(description = "Updated message to the task owner", 
            example = "I can complete this task faster than estimated with my new equipment.",
            maxLength = 1000)
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message;
    
    @Schema(description = "Updated estimated time to complete the task in hours", 
            example = "18", 
            minimum = "1", 
            maximum = "720")
    @Min(value = 1, message = "Estimated completion time must be at least 1 hour")
    @Max(value = 720, message = "Estimated completion time cannot exceed 720 hours (30 days)")
    private Integer estimatedCompletionHours;
}
