package com.mshando.biddingservice.dto;

import com.mshando.biddingservice.model.BidStatus;
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
public class BidResponseDTO {
    
    private Long id;
    private Long taskId;
    private Long taskerId;
    private Long customerId;
    private BigDecimal amount;
    private String message;
    private BidStatus status;
    private Integer estimatedCompletionHours;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime withdrawnAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime cancelledAt;
    
    // Additional information
    private String cancellationReason;
    private Long version;
    
    // Tasker information (for customer view)
    private TaskerInfoDTO taskerInfo;
    
    // Task information (for tasker view)
    private TaskInfoDTO taskInfo;
}
