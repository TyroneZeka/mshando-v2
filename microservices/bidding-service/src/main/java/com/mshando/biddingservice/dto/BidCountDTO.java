package com.mshando.biddingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "BidCount", description = "Bid count statistics for a specific task")
public class BidCountDTO {
    
    @Schema(description = "ID of the task", example = "123")
    private Long taskId;
    
    @Schema(description = "Total number of bids", example = "15")
    private Long totalBids;
    
    @Schema(description = "Number of pending bids", example = "8")
    private Long pendingBids;
    
    @Schema(description = "Number of accepted bids", example = "1")
    private Long acceptedBids;
    
    @Schema(description = "Number of rejected bids", example = "3")
    private Long rejectedBids;
    
    @Schema(description = "Number of withdrawn bids", example = "2")
    private Long withdrawnBids;
    
    @Schema(description = "Number of completed bids", example = "1")
    private Long completedBids;
    
    @Schema(description = "Number of cancelled bids", example = "0")
    private Long cancelledBids;
}
