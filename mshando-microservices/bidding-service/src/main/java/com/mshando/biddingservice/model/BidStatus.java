package com.mshando.biddingservice.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumeration representing the various states of a bid in the bidding system.
 * 
 * The bid lifecycle follows these transitions:
 * PENDING -> ACCEPTED/REJECTED/WITHDRAWN
 * ACCEPTED -> COMPLETED/CANCELLED
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Schema(description = "Current status of the bid in its lifecycle", 
        example = "PENDING",
        allowableValues = {"PENDING", "ACCEPTED", "REJECTED", "WITHDRAWN", "COMPLETED", "CANCELLED"})
public enum BidStatus {
    
    @Schema(description = "🕐 Bid has been submitted and is awaiting customer's decision")
    PENDING,
    
    @Schema(description = "✅ Bid has been accepted by the customer")
    ACCEPTED,
    
    @Schema(description = "❌ Bid has been rejected by the customer")
    REJECTED,
    
    @Schema(description = "🔙 Bid has been withdrawn by the tasker")
    WITHDRAWN,
    
    @Schema(description = "🎉 Work has been completed and verified")
    COMPLETED,
    
    @Schema(description = "⚠️ Accepted bid was cancelled before completion")
    CANCELLED
}
