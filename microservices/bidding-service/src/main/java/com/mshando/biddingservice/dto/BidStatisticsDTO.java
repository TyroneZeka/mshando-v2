package com.mshando.biddingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for bid statistics response.
 * 
 * This DTO contains aggregated statistics about bids
 * in the system for reporting and analytics purposes.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidStatisticsDTO {
    
    private Long totalBids;
    private Long pendingBids;
    private Long acceptedBids;
    private Long rejectedBids;
    private Long withdrawnBids;
    private Long completedBids;
    private Long cancelledBids;
    private Double averageBidAmount;
    private LocalDateTime calculatedAt;
}
