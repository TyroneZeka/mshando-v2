package com.mshando.biddingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for bid count response.
 * 
 * This DTO contains the count of bids for a specific task
 * used in statistics and analytics endpoints.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidCountDTO {
    
    private Long taskId;
    private Long totalBids;
    private Long pendingBids;
    private Long acceptedBids;
    private Long rejectedBids;
    private Long withdrawnBids;
    private Long completedBids;
    private Long cancelledBids;
}
