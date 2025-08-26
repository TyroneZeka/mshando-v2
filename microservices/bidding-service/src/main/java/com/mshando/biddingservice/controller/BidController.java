package com.mshando.biddingservice.controller;

import com.mshando.biddingservice.dto.*;
import com.mshando.biddingservice.model.BidStatus;
import com.mshando.biddingservice.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * Provides comprehensive endpoints for creating, updating, and managing bids
 * in the Mshando marketplace platform with full lifecycle support.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/bids")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "🎯 Bid Management", 
     description = "Complete bid lifecycle management including creation, updates, status changes, and analytics")
public class BidController {

    private final BidService bidService;

    @Operation(
        summary = "📝 Create New Bid",
        description = """
                **Create a new bid on a task**
                
                This endpoint allows taskers to place bids on available tasks. Each tasker can only place one bid per task.
                
                ### Business Rules:
                - ✅ Only one bid per tasker per task
                - 💰 Amount must be between $5.00 and $10,000.00
                - ⏱️ Estimated time: 1-720 hours (1 hour to 30 days)
                - 📝 Optional message up to 1000 characters
                
                ### Workflow:
                1. Tasker submits bid with required details
                2. System validates business rules
                3. Bid is created with PENDING status
                4. Notifications sent to task owner
                """,
        tags = {"Bid Creation"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "✅ Bid created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BidResponseDTO.class),
                examples = @ExampleObject(
                    name = "Successful Bid Creation",
                    summary = "Example of successful bid creation",
                    value = """
                            {
                              "id": 456,
                              "taskId": 123,
                              "taskerId": 789,
                              "customerId": 101,
                              "amount": 75.50,
                              "message": "I have extensive experience with this type of work.",
                              "status": "PENDING",
                              "estimatedCompletionHours": 24,
                              "createdAt": "2025-08-21T10:30:00",
                              "updatedAt": "2025-08-21T10:30:00"
                            }
                            """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "❌ Invalid bid data or validation failed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(ref = "#/components/examples/ValidationError")
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "🔒 Authentication required",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(ref = "#/components/examples/Unauthorized")
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "⚠️ Business rule violation (e.g., already bid on this task)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
                examples = @ExampleObject(ref = "#/components/examples/BusinessRuleViolation")
            )
        )
    })
    @PostMapping
    public ResponseEntity<BidResponseDTO> createBid(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Bid creation details",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = BidCreateDTO.class),
                    examples = @ExampleObject(
                        name = "Create Bid Example",
                        summary = "Example bid creation request",
                        value = """
                                {
                                  "taskId": 123,
                                  "amount": 75.50,
                                  "message": "I have extensive experience with this type of work and can complete it efficiently.",
                                  "estimatedCompletionHours": 24
                                }
                                """
                    )
                )
            )
            @Valid @RequestBody BidCreateDTO bidCreateDTO,
            @Parameter(hidden = true) Authentication authentication) {
        
        log.info("Creating bid for task {} by user {}", bidCreateDTO.getTaskId(), authentication.getName());
        
        Long taskerId = extractUserIdFromAuthentication(authentication);
        BidResponseDTO createdBid = bidService.createBid(bidCreateDTO, taskerId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBid);
    }

    @Operation(
        summary = "✏️ Update Existing Bid",
        description = """
                **Update a pending bid's details**
                
                This endpoint allows taskers to modify their pending bids before they are accepted or rejected.
                
                ### Requirements:
                - 🎯 Must be the bid owner
                - ⏳ Bid must be in PENDING status
                - 💰 New amount must meet validation rules
                
                ### What can be updated:
                - Bid amount ($5.00 - $10,000.00)
                - Message to customer
                - Estimated completion time
                """,
        tags = {"Bid Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Bid updated successfully"),
        @ApiResponse(responseCode = "400", description = "❌ Invalid bid data"),
        @ApiResponse(responseCode = "401", description = "🔒 Authentication required"),
        @ApiResponse(responseCode = "403", description = "🚫 Not bid owner"),
        @ApiResponse(responseCode = "404", description = "❓ Bid not found"),
        @ApiResponse(responseCode = "409", description = "⚠️ Bid cannot be modified (not pending)")
    })
    @PutMapping("/{bidId}")
    public ResponseEntity<BidResponseDTO> updateBid(
            @Parameter(description = "Unique bid identifier", example = "456")
            @PathVariable Long bidId,
            @Valid @RequestBody BidUpdateDTO bidUpdateDTO,
            @Parameter(hidden = true) Authentication authentication) {
        
        log.info("Updating bid {} by user {}", bidId, authentication.getName());
        
        Long taskerId = extractUserIdFromAuthentication(authentication);
        BidResponseDTO updatedBid = bidService.updateBid(bidId, bidUpdateDTO, taskerId);
        
        return ResponseEntity.ok(updatedBid);
    }

    @Operation(
        summary = "🔍 Get Bid Details",
        description = """
                **Retrieve detailed information about a specific bid**
                
                Returns complete bid information including status, timestamps, and related data.
                """,
        tags = {"Bid Retrieval"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Bid retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "❓ Bid not found")
    })
    @GetMapping("/{bidId}")
    public ResponseEntity<BidResponseDTO> getBidById(
            @Parameter(description = "Unique bid identifier", example = "456")
            @PathVariable Long bidId) {
        log.debug("Fetching bid {}", bidId);
        
        BidResponseDTO bid = bidService.getBidById(bidId);
        return ResponseEntity.ok(bid);
    }

    @Operation(
        summary = "📋 Get Task Bids",
        description = """
                **Retrieve all bids for a specific task**
                
                Returns paginated list of all bids placed on the specified task, useful for task owners to review options.
                
                ### Features:
                - 📄 Paginated results
                - 🔄 Real-time status updates
                - 👤 Tasker information included
                """,
        tags = {"Bid Retrieval"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Task bids retrieved successfully")
    })
    @GetMapping("/task/{taskId}")
    public ResponseEntity<Page<BidResponseDTO>> getBidsByTaskId(
            @Parameter(description = "Task identifier to get bids for", example = "123")
            @PathVariable Long taskId,
            @Parameter(description = "Page number (0-based)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of bids per page", example = "10") 
            @RequestParam(defaultValue = "10") int size) {
        
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
