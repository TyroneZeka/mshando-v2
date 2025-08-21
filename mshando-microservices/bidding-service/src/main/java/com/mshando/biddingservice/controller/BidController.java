package com.mshando.biddingservice.controller;

import com.mshando.biddingservice.dto.*;
import com.mshando.biddingservice.model.BidStatus;
import com.mshando.biddingservice.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;



/**
 * REST controller for bid management operations.
 * 
 * Provides endpoints for creating, updating, and managing bids
 * in the Mshando marketplace platform.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/bids")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bid Management", description = "Endpoints for managing bids on tasks")
public class BidController {

    private final BidService bidService;

    @Operation(summary = "Create a new bid", description = "Create a new bid for a task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Bid created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid bid data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "409", description = "Bid already exists or business rule violated")
    })
    @PostMapping
    public ResponseEntity<BidResponseDTO> createBid(
            @Valid @RequestBody BidCreateDTO bidCreateDTO,
            Authentication authentication) {
        
        log.info("Creating bid for task {} by user {}", bidCreateDTO.getTaskId(), authentication.getName());
        
        Long taskerId = extractUserIdFromAuthentication(authentication);
        BidResponseDTO createdBid = bidService.createBid(bidCreateDTO, taskerId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBid);
    }

    @Operation(summary = "Update an existing bid", description = "Update a pending bid's details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bid updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid bid data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not bid owner"),
        @ApiResponse(responseCode = "404", description = "Bid not found"),
        @ApiResponse(responseCode = "409", description = "Bid cannot be modified")
    })
    @PutMapping("/{bidId}")
    public ResponseEntity<BidResponseDTO> updateBid(
            @PathVariable Long bidId,
            @Valid @RequestBody BidUpdateDTO bidUpdateDTO,
            Authentication authentication) {
        
        log.info("Updating bid {} by user {}", bidId, authentication.getName());
        
        Long taskerId = extractUserIdFromAuthentication(authentication);
        BidResponseDTO updatedBid = bidService.updateBid(bidId, bidUpdateDTO, taskerId);
        
        return ResponseEntity.ok(updatedBid);
    }

    @Operation(summary = "Get bid by ID", description = "Retrieve a specific bid by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bid retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Bid not found")
    })
    @GetMapping("/{bidId}")
    public ResponseEntity<BidResponseDTO> getBidById(@PathVariable Long bidId) {
        log.debug("Fetching bid {}", bidId);
        
        BidResponseDTO bid = bidService.getBidById(bidId);
        return ResponseEntity.ok(bid);
    }

    @Operation(summary = "Get bids for a task", description = "Retrieve all bids for a specific task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bids retrieved successfully")
    })
    @GetMapping("/task/{taskId}")
    public ResponseEntity<Page<BidResponseDTO>> getBidsByTaskId(
            @PathVariable Long taskId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        log.debug("Fetching bids for task {} - page: {}, size: {}", taskId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BidResponseDTO> bids = bidService.getBidsByTaskId(taskId, pageable);
        
        return ResponseEntity.ok(bids);
    }

    @Operation(summary = "Get my bids", description = "Retrieve all bids made by the authenticated tasker")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bids retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-bids")
    public ResponseEntity<Page<BidResponseDTO>> getMyBids(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by status") @RequestParam(required = false) BidStatus status,
            Authentication authentication) {
        
        Long taskerId = extractUserIdFromAuthentication(authentication);
        log.debug("Fetching bids for tasker {} - page: {}, size: {}, status: {}", taskerId, page, size, status);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BidResponseDTO> bids;
        
        if (status != null) {
            bids = bidService.getBidsByTaskerIdAndStatus(taskerId, status, pageable);
        } else {
            bids = bidService.getBidsByTaskerId(taskerId, pageable);
        }
        
        return ResponseEntity.ok(bids);
    }

    @Operation(summary = "Get bids for my tasks", description = "Retrieve all bids for tasks owned by the authenticated customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bids retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-tasks-bids")
    public ResponseEntity<Page<BidResponseDTO>> getBidsForMyTasks(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by status") @RequestParam(required = false) BidStatus status,
            Authentication authentication) {
        
        Long customerId = extractUserIdFromAuthentication(authentication);
        log.debug("Fetching bids for customer {} tasks - page: {}, size: {}, status: {}", customerId, page, size, status);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BidResponseDTO> bids;
        
        if (status != null) {
            bids = bidService.getBidsByCustomerIdAndStatus(customerId, status, pageable);
        } else {
            bids = bidService.getBidsByCustomerId(customerId, pageable);
        }
        
        return ResponseEntity.ok(bids);
    }

    @Operation(summary = "Accept a bid", description = "Accept a bid for your task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bid accepted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not task owner"),
        @ApiResponse(responseCode = "404", description = "Bid not found"),
        @ApiResponse(responseCode = "409", description = "Bid cannot be accepted")
    })
    @PatchMapping("/{bidId}/accept")
    public ResponseEntity<BidResponseDTO> acceptBid(
            @PathVariable Long bidId,
            Authentication authentication) {
        
        log.info("Accepting bid {} by user {}", bidId, authentication.getName());
        
        Long customerId = extractUserIdFromAuthentication(authentication);
        BidResponseDTO acceptedBid = bidService.acceptBid(bidId, customerId);
        
        return ResponseEntity.ok(acceptedBid);
    }

    @Operation(summary = "Reject a bid", description = "Reject a bid for your task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bid rejected successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not task owner"),
        @ApiResponse(responseCode = "404", description = "Bid not found"),
        @ApiResponse(responseCode = "409", description = "Bid cannot be rejected")
    })
    @PatchMapping("/{bidId}/reject")
    public ResponseEntity<BidResponseDTO> rejectBid(
            @PathVariable Long bidId,
            Authentication authentication) {
        
        log.info("Rejecting bid {} by user {}", bidId, authentication.getName());
        
        Long customerId = extractUserIdFromAuthentication(authentication);
        BidResponseDTO rejectedBid = bidService.rejectBid(bidId, customerId);
        
        return ResponseEntity.ok(rejectedBid);
    }

    @Operation(summary = "Withdraw a bid", description = "Withdraw your own bid")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bid withdrawn successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not bid owner"),
        @ApiResponse(responseCode = "404", description = "Bid not found"),
        @ApiResponse(responseCode = "409", description = "Bid cannot be withdrawn")
    })
    @PatchMapping("/{bidId}/withdraw")
    public ResponseEntity<BidResponseDTO> withdrawBid(
            @PathVariable Long bidId,
            Authentication authentication) {
        
        log.info("Withdrawing bid {} by user {}", bidId, authentication.getName());
        
        Long taskerId = extractUserIdFromAuthentication(authentication);
        BidResponseDTO withdrawnBid = bidService.withdrawBid(bidId, taskerId);
        
        return ResponseEntity.ok(withdrawnBid);
    }

    @Operation(summary = "Complete a bid", description = "Mark your accepted bid as completed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bid completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not bid owner"),
        @ApiResponse(responseCode = "404", description = "Bid not found"),
        @ApiResponse(responseCode = "409", description = "Bid cannot be completed")
    })
    @PatchMapping("/{bidId}/complete")
    public ResponseEntity<BidResponseDTO> completeBid(
            @PathVariable Long bidId,
            Authentication authentication) {
        
        log.info("Completing bid {} by user {}", bidId, authentication.getName());
        
        Long taskerId = extractUserIdFromAuthentication(authentication);
        BidResponseDTO completedBid = bidService.completeBid(bidId, taskerId);
        
        return ResponseEntity.ok(completedBid);
    }

    @Operation(summary = "Cancel a bid", description = "Cancel an accepted bid")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bid cancelled successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not involved in bid"),
        @ApiResponse(responseCode = "404", description = "Bid not found"),
        @ApiResponse(responseCode = "409", description = "Bid cannot be cancelled")
    })
    @PatchMapping("/{bidId}/cancel")
    public ResponseEntity<BidResponseDTO> cancelBid(
            @PathVariable Long bidId,
            @Valid @RequestBody BidCancellationDTO cancellationDTO,
            Authentication authentication) {
        
        log.info("Cancelling bid {} by user {} with reason: {}", bidId, authentication.getName(), cancellationDTO.getReason());
        
        Long userId = extractUserIdFromAuthentication(authentication);
        BidResponseDTO cancelledBid = bidService.cancelBid(bidId, cancellationDTO.getReason(), userId);
        
        return ResponseEntity.ok(cancelledBid);
    }

    @Operation(summary = "Get bid statistics", description = "Get bidding statistics for the authenticated tasker")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/statistics")
    public ResponseEntity<Object> getBidStatistics(Authentication authentication) {
        Long taskerId = extractUserIdFromAuthentication(authentication);
        log.debug("Fetching bid statistics for tasker {}", taskerId);
        
        Object statistics = bidService.getTaskerBidStatistics(taskerId);
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Get bid count for task", description = "Get the total number of bids for a specific task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bid count retrieved successfully")
    })
    @GetMapping("/task/{taskId}/count")
    public ResponseEntity<BidCountDTO> getBidCountByTaskId(@PathVariable Long taskId) {
        log.debug("Fetching bid count for task {}", taskId);
        
        long totalBids = bidService.getBidCountByTaskId(taskId);
        long pendingBids = bidService.getPendingBidCountByTaskId(taskId);
        
        BidCountDTO bidCount = BidCountDTO.builder()
                .taskId(taskId)
                .totalBids(totalBids)
                .pendingBids(pendingBids)
                .build();
        
        return ResponseEntity.ok(bidCount);
    }

    /**
     * Extract user ID from JWT authentication token
     */
    private Long extractUserIdFromAuthentication(Authentication authentication) {
        // This would extract the user ID from the JWT token
        // Implementation depends on your JWT structure
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid user ID in authentication token", e);
        }
    }
}
