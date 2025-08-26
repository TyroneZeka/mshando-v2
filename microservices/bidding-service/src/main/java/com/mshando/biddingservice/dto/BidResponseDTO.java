package com.mshando.biddingservice.dto;

import com.mshando.biddingservice.model.BidStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Bid responses.
 * 
 * This DTO is used when returning bid information to clients,
 * including all relevant bid details and timestamps.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "BidResponse", description = "Complete bid information including status and timestamps")
public class BidResponseDTO {
    
    @Schema(description = "Unique bid identifier", example = "456")
    private Long id;
    
    @Schema(description = "ID of the task this bid is for", example = "123")
    private Long taskId;
    
    @Schema(description = "ID of the tasker who placed the bid", example = "789")
    private Long taskerId;
    
    @Schema(description = "ID of the customer who owns the task", example = "101")
    private Long customerId;
    
    @Schema(description = "Bid amount in USD", example = "75.50")
    private BigDecimal amount;
    
    @Schema(description = "Tasker's message to the customer", 
            example = "I have extensive experience with this type of work.")
    private String message;
    
    @Schema(description = "Current status of the bid")
    private BidStatus status;
    
    @Schema(description = "Estimated completion time in hours", example = "24")
    private Integer estimatedCompletionHours;
    
    // Timestamps
    @Schema(description = "When the bid was created", example = "2025-08-21T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "When the bid was last updated", example = "2025-08-21T11:15:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "When the bid was accepted (if applicable)", example = "2025-08-21T14:30:00")
    private LocalDateTime acceptedAt;
    
    @Schema(description = "When the bid was completed (if applicable)", example = "2025-08-22T10:30:00")
    private LocalDateTime completedAt;
    
    @Schema(description = "When the bid was withdrawn (if applicable)", example = "2025-08-21T16:00:00")
    private LocalDateTime withdrawnAt;
    
    @Schema(description = "When the bid was rejected (if applicable)", example = "2025-08-21T15:45:00")
    private LocalDateTime rejectedAt;
    
    @Schema(description = "When the bid was cancelled (if applicable)", example = "2025-08-21T17:00:00")
    private LocalDateTime cancelledAt;
    
    // Additional information
    @Schema(description = "Reason for cancellation (if cancelled)", 
            example = "Task requirements changed")
    private String cancellationReason;
    
    @Schema(description = "Version for optimistic locking", example = "3")
    private Long version;
    
    // Tasker information (for customer view)
    @Schema(description = "Information about the tasker (visible to customers)")
    private TaskerInfoDTO taskerInfo;
    
    // Task information (for tasker view)
    @Schema(description = "Information about the task (visible to taskers)")
    private TaskInfoDTO taskInfo;
}
