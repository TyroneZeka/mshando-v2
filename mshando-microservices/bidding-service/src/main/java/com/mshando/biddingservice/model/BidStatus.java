package com.mshando.biddingservice.model;

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
public enum BidStatus {
    /**
     * Bid has been submitted and is awaiting customer's decision
     */
    PENDING,
    
    /**
     * Bid has been accepted by the customer
     */
    ACCEPTED,
    
    /**
     * Bid has been rejected by the customer
     */
    REJECTED,
    
    /**
     * Bid has been withdrawn by the tasker
     */
    WITHDRAWN,
    
    /**
     * Work has been completed and verified
     */
    COMPLETED,
    
    /**
     * Accepted bid was cancelled before completion
     */
    CANCELLED
}
