package com.mshando.biddingservice.service;

import com.mshando.biddingservice.dto.*;
import com.mshando.biddingservice.model.BidStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for bid management operations.
 * 
 * Defines the contract for bid-related business logic including
 * CRUD operations, status management, and validation.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
public interface BidService {
    
    /**
     * Create a new bid for a task
     */
    BidResponseDTO createBid(BidCreateDTO bidCreateDTO, Long taskerId);
    
    /**
     * Update an existing bid (only pending bids can be updated)
     */
    BidResponseDTO updateBid(Long bidId, BidUpdateDTO bidUpdateDTO, Long taskerId);
    
    /**
     * Get bid by ID
     */
    BidResponseDTO getBidById(Long bidId);
    
    /**
     * Get all bids for a specific task
     */
    List<BidResponseDTO> getBidsByTaskId(Long taskId);
    
    /**
     * Get paginated bids for a specific task
     */
    Page<BidResponseDTO> getBidsByTaskId(Long taskId, Pageable pageable);
    
    /**
     * Get all bids made by a tasker
     */
    Page<BidResponseDTO> getBidsByTaskerId(Long taskerId, Pageable pageable);
    
    /**
     * Get all bids for tasks owned by a customer
     */
    Page<BidResponseDTO> getBidsByCustomerId(Long customerId, Pageable pageable);
    
    /**
     * Get bids by status
     */
    Page<BidResponseDTO> getBidsByStatus(BidStatus status, Pageable pageable);
    
    /**
     * Get bids by tasker and status
     */
    Page<BidResponseDTO> getBidsByTaskerIdAndStatus(Long taskerId, BidStatus status, Pageable pageable);
    
    /**
     * Get bids by customer and status
     */
    Page<BidResponseDTO> getBidsByCustomerIdAndStatus(Long customerId, BidStatus status, Pageable pageable);
    
    /**
     * Accept a bid (customer action)
     */
    BidResponseDTO acceptBid(Long bidId, Long customerId);
    
    /**
     * Reject a bid (customer action)
     */
    BidResponseDTO rejectBid(Long bidId, Long customerId);
    
    /**
     * Withdraw a bid (tasker action)
     */
    BidResponseDTO withdrawBid(Long bidId, Long taskerId);
    
    /**
     * Mark bid as completed (tasker action)
     */
    BidResponseDTO completeBid(Long bidId, Long taskerId);
    
    /**
     * Cancel an accepted bid (customer or tasker action)
     */
    BidResponseDTO cancelBid(Long bidId, String cancellationReason, Long userId);
    
    /**
     * Check if a tasker has already bid on a task
     */
    boolean hasTaskerBidOnTask(Long taskId, Long taskerId);
    
    /**
     * Get bid count for a task
     */
    long getBidCountByTaskId(Long taskId);
    
    /**
     * Get pending bid count for a task
     */
    long getPendingBidCountByTaskId(Long taskId);
    
    /**
     * Get the accepted bid for a task
     */
    BidResponseDTO getAcceptedBidByTaskId(Long taskId);
    
    /**
     * Get bid statistics for a tasker
     */
    Object getTaskerBidStatistics(Long taskerId);
    
    /**
     * Process auto-acceptance of old pending bids
     */
    void processAutoAcceptance();
    
    /**
     * Validate if a bid can be placed on a task
     */
    void validateBidCreation(Long taskId, Long taskerId);
    
    /**
     * Validate if a bid operation is allowed
     */
    void validateBidOperation(Long bidId, Long userId, String operation);
}
