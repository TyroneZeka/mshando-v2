package com.mshando.biddingservice.service.impl;

import com.mshando.biddingservice.dto.*;
import com.mshando.biddingservice.exception.BidNotFoundException;
import com.mshando.biddingservice.exception.InvalidBidOperationException;
import com.mshando.biddingservice.model.Bid;
import com.mshando.biddingservice.model.BidStatus;
import com.mshando.biddingservice.repository.BidRepository;
import com.mshando.biddingservice.service.BidService;
import com.mshando.biddingservice.service.ExternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of BidService for managing bid operations.
 * 
 * Handles all bid-related business logic including creation, updates,
 * status changes, and validation rules.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BidServiceImpl implements BidService {
    
    private final BidRepository bidRepository;
    private final ExternalService externalService;
    
    @Value("${bidding.max-bids-per-task:10}")
    private int maxBidsPerTask;
    
    @Value("${bidding.auto-accept.enabled:false}")
    private boolean autoAcceptEnabled;
    
    @Value("${bidding.auto-accept.threshold-hours:24}")
    private int autoAcceptThresholdHours;

    @Override
    public BidResponseDTO createBid(BidCreateDTO bidCreateDTO, Long taskerId) {
        log.info("Creating bid for task {} by tasker {}", bidCreateDTO.getTaskId(), taskerId);
        
        // Validate bid creation
        validateBidCreation(bidCreateDTO.getTaskId(), taskerId);
        
        // Get task and customer information
        TaskInfoDTO taskInfo = externalService.getTaskInfo(bidCreateDTO.getTaskId());
        if (taskInfo == null) {
            throw new InvalidBidOperationException("Task not found or not available for bidding");
        }
        
        // Create bid entity
        Bid bid = Bid.builder()
                .taskId(bidCreateDTO.getTaskId())
                .taskerId(taskerId)
                .customerId(taskInfo.getCustomerId())
                .amount(bidCreateDTO.getAmount())
                .message(bidCreateDTO.getMessage())
                .estimatedCompletionHours(bidCreateDTO.getEstimatedCompletionHours())
                .status(BidStatus.PENDING)
                .build();
        
        Bid savedBid = bidRepository.save(bid);
        log.info("Created bid with ID {} for task {} by tasker {}", savedBid.getId(), bidCreateDTO.getTaskId(), taskerId);
        
        return convertToResponseDTO(savedBid);
    }

    @Override
    public BidResponseDTO updateBid(Long bidId, BidUpdateDTO bidUpdateDTO, Long taskerId) {
        log.info("Updating bid {} by tasker {}", bidId, taskerId);
        
        Bid bid = findBidById(bidId);
        
        // Validate ownership and status
        if (!bid.getTaskerId().equals(taskerId)) {
            throw new InvalidBidOperationException("You can only update your own bids");
        }
        
        if (!bid.canBeModified()) {
            throw new InvalidBidOperationException("Only pending bids can be modified");
        }
        
        // Update bid fields
        bid.setAmount(bidUpdateDTO.getAmount());
        bid.setMessage(bidUpdateDTO.getMessage());
        bid.setEstimatedCompletionHours(bidUpdateDTO.getEstimatedCompletionHours());
        
        Bid updatedBid = bidRepository.save(bid);
        log.info("Updated bid {}", bidId);
        
        return convertToResponseDTO(updatedBid);
    }

    @Override
    @Transactional(readOnly = true)
    public BidResponseDTO getBidById(Long bidId) {
        Bid bid = findBidById(bidId);
        return convertToResponseDTO(bid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidResponseDTO> getBidsByTaskId(Long taskId) {
        List<Bid> bids = bidRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
        return bids.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BidResponseDTO> getBidsByTaskId(Long taskId, Pageable pageable) {
        Page<Bid> bids = bidRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pageable);
        return bids.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BidResponseDTO> getBidsByTaskerId(Long taskerId, Pageable pageable) {
        Page<Bid> bids = bidRepository.findByTaskerIdOrderByCreatedAtDesc(taskerId, pageable);
        return bids.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BidResponseDTO> getBidsByCustomerId(Long customerId, Pageable pageable) {
        Page<Bid> bids = bidRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        return bids.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BidResponseDTO> getBidsByStatus(BidStatus status, Pageable pageable) {
        Page<Bid> bids = bidRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return bids.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BidResponseDTO> getBidsByTaskerIdAndStatus(Long taskerId, BidStatus status, Pageable pageable) {
        Page<Bid> bids = bidRepository.findByTaskerIdAndStatusOrderByCreatedAtDesc(taskerId, status, pageable);
        return bids.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BidResponseDTO> getBidsByCustomerIdAndStatus(Long customerId, BidStatus status, Pageable pageable) {
        Page<Bid> bids = bidRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status, pageable);
        return bids.map(this::convertToResponseDTO);
    }

    @Override
    public BidResponseDTO acceptBid(Long bidId, Long customerId) {
        log.info("Accepting bid {} by customer {}", bidId, customerId);
        
        Bid bid = findBidById(bidId);
        
        // Validate ownership and status
        if (!bid.getCustomerId().equals(customerId)) {
            throw new InvalidBidOperationException("You can only accept bids for your own tasks");
        }
        
        if (!bid.isPending()) {
            throw new InvalidBidOperationException("Only pending bids can be accepted");
        }
        
        // Check if task already has accepted bid
        if (bidRepository.findByTaskIdAndStatus(bid.getTaskId(), BidStatus.ACCEPTED).isPresent()) {
            throw new InvalidBidOperationException("Task already has an accepted bid");
        }
        
        // Update bid status
        bid.setStatus(BidStatus.ACCEPTED);
        bid.setAcceptedAt(LocalDateTime.now());
        
        Bid acceptedBid = bidRepository.save(bid);
        
        // Reject all other pending bids for this task
        rejectOtherPendingBids(bid.getTaskId(), bidId);
        
        // Update task status in task service
        externalService.updateTaskStatus(bid.getTaskId(), "IN_PROGRESS", bid.getTaskerId());
        
        log.info("Accepted bid {} for task {}", bidId, bid.getTaskId());
        
        return convertToResponseDTO(acceptedBid);
    }

    @Override
    public BidResponseDTO rejectBid(Long bidId, Long customerId) {
        log.info("Rejecting bid {} by customer {}", bidId, customerId);
        
        Bid bid = findBidById(bidId);
        
        // Validate ownership and status
        if (!bid.getCustomerId().equals(customerId)) {
            throw new InvalidBidOperationException("You can only reject bids for your own tasks");
        }
        
        if (!bid.isPending()) {
            throw new InvalidBidOperationException("Only pending bids can be rejected");
        }
        
        // Update bid status
        bid.setStatus(BidStatus.REJECTED);
        bid.setRejectedAt(LocalDateTime.now());
        
        Bid rejectedBid = bidRepository.save(bid);
        log.info("Rejected bid {}", bidId);
        
        return convertToResponseDTO(rejectedBid);
    }

    @Override
    public BidResponseDTO withdrawBid(Long bidId, Long taskerId) {
        log.info("Withdrawing bid {} by tasker {}", bidId, taskerId);
        
        Bid bid = findBidById(bidId);
        
        // Validate ownership and status
        if (!bid.getTaskerId().equals(taskerId)) {
            throw new InvalidBidOperationException("You can only withdraw your own bids");
        }
        
        if (!bid.canBeWithdrawn()) {
            throw new InvalidBidOperationException("This bid cannot be withdrawn");
        }
        
        // Update bid status
        bid.setStatus(BidStatus.WITHDRAWN);
        bid.setWithdrawnAt(LocalDateTime.now());
        
        // If bid was accepted, update task status back to open
        if (bid.isAccepted()) {
            externalService.updateTaskStatus(bid.getTaskId(), "OPEN", null);
        }
        
        Bid withdrawnBid = bidRepository.save(bid);
        log.info("Withdrawn bid {}", bidId);
        
        return convertToResponseDTO(withdrawnBid);
    }

    @Override
    public BidResponseDTO completeBid(Long bidId, Long taskerId) {
        log.info("Completing bid {} by tasker {}", bidId, taskerId);
        
        Bid bid = findBidById(bidId);
        
        // Validate ownership and status
        if (!bid.getTaskerId().equals(taskerId)) {
            throw new InvalidBidOperationException("You can only complete your own bids");
        }
        
        if (!bid.isAccepted()) {
            throw new InvalidBidOperationException("Only accepted bids can be completed");
        }
        
        // Update bid status
        bid.setStatus(BidStatus.COMPLETED);
        bid.setCompletedAt(LocalDateTime.now());
        
        // Update task status in task service
        externalService.updateTaskStatus(bid.getTaskId(), "COMPLETED", taskerId);
        
        Bid completedBid = bidRepository.save(bid);
        log.info("Completed bid {}", bidId);
        
        return convertToResponseDTO(completedBid);
    }

    @Override
    public BidResponseDTO cancelBid(Long bidId, String cancellationReason, Long userId) {
        log.info("Cancelling bid {} by user {} with reason: {}", bidId, userId, cancellationReason);
        
        Bid bid = findBidById(bidId);
        
        // Validate ownership
        if (!bid.getTaskerId().equals(userId) && !bid.getCustomerId().equals(userId)) {
            throw new InvalidBidOperationException("You can only cancel bids you are involved in");
        }
        
        if (!bid.isAccepted()) {
            throw new InvalidBidOperationException("Only accepted bids can be cancelled");
        }
        
        // Update bid status
        bid.setStatus(BidStatus.CANCELLED);
        bid.setCancelledAt(LocalDateTime.now());
        bid.setCancellationReason(cancellationReason);
        
        // Update task status back to open
        externalService.updateTaskStatus(bid.getTaskId(), "OPEN", null);
        
        Bid cancelledBid = bidRepository.save(bid);
        log.info("Cancelled bid {}", bidId);
        
        return convertToResponseDTO(cancelledBid);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasTaskerBidOnTask(Long taskId, Long taskerId) {
        return bidRepository.existsByTaskIdAndTaskerId(taskId, taskerId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getBidCountByTaskId(Long taskId) {
        return bidRepository.countByTaskId(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getPendingBidCountByTaskId(Long taskId) {
        return bidRepository.countPendingBidsByTaskId(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public BidResponseDTO getAcceptedBidByTaskId(Long taskId) {
        return bidRepository.findByTaskIdAndStatus(taskId, BidStatus.ACCEPTED)
                .map(this::convertToResponseDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getTaskerBidStatistics(Long taskerId) {
        return bidRepository.getTaskerBidStatistics(taskerId);
    }

    @Override
    public void processAutoAcceptance() {
        if (!autoAcceptEnabled) {
            log.debug("Auto-acceptance is disabled");
            return;
        }
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(autoAcceptThresholdHours);
        List<Bid> oldBids = bidRepository.findPendingBidsOlderThan(cutoffDate);
        
        log.info("Processing {} old pending bids for auto-acceptance", oldBids.size());
        
        for (Bid bid : oldBids) {
            try {
                // Auto-accept the oldest bid for each task
                acceptBid(bid.getId(), bid.getCustomerId());
                log.info("Auto-accepted bid {} for task {}", bid.getId(), bid.getTaskId());
            } catch (Exception e) {
                log.error("Failed to auto-accept bid {}: {}", bid.getId(), e.getMessage());
            }
        }
    }

    @Override
    public void validateBidCreation(Long taskId, Long taskerId) {
        // Check if tasker already bid on this task
        if (hasTaskerBidOnTask(taskId, taskerId)) {
            throw new InvalidBidOperationException("You have already placed a bid on this task");
        }
        
        // Check maximum bids per task
        long bidCount = getBidCountByTaskId(taskId);
        if (bidCount >= maxBidsPerTask) {
            throw new InvalidBidOperationException("Maximum number of bids reached for this task");
        }
        
        // Validate task exists and is available for bidding
        TaskInfoDTO taskInfo = externalService.getTaskInfo(taskId);
        if (taskInfo == null) {
            throw new InvalidBidOperationException("Task not found");
        }
        
        if (!"OPEN".equals(taskInfo.getStatus())) {
            throw new InvalidBidOperationException("Task is not available for bidding");
        }
        
        // Check if tasker is the task owner
        if (taskInfo.getCustomerId().equals(taskerId)) {
            throw new InvalidBidOperationException("You cannot bid on your own task");
        }
    }

    @Override
    public void validateBidOperation(Long bidId, Long userId, String operation) {
        Bid bid = findBidById(bidId);
        
        switch (operation.toLowerCase()) {
            case "accept":
            case "reject":
                if (!bid.getCustomerId().equals(userId)) {
                    throw new InvalidBidOperationException("Only task owner can " + operation + " bids");
                }
                break;
            case "withdraw":
            case "complete":
                if (!bid.getTaskerId().equals(userId)) {
                    throw new InvalidBidOperationException("Only bid owner can " + operation + " bids");
                }
                break;
            case "cancel":
                if (!bid.getTaskerId().equals(userId) && !bid.getCustomerId().equals(userId)) {
                    throw new InvalidBidOperationException("Only involved parties can cancel bids");
                }
                break;
            default:
                throw new InvalidBidOperationException("Unknown operation: " + operation);
        }
    }

    private Bid findBidById(Long bidId) {
        return bidRepository.findById(bidId)
                .orElseThrow(() -> BidNotFoundException.withId(bidId));
    }

    private void rejectOtherPendingBids(Long taskId, Long acceptedBidId) {
        List<Bid> pendingBids = bidRepository.findByTaskIdAndStatus(taskId, BidStatus.PENDING);
        for (Bid bid : pendingBids) {
            if (!bid.getId().equals(acceptedBidId)) {
                bid.setStatus(BidStatus.REJECTED);
                bid.setRejectedAt(LocalDateTime.now());
                bidRepository.save(bid);
            }
        }
    }

    private BidResponseDTO convertToResponseDTO(Bid bid) {
        BidResponseDTO.BidResponseDTOBuilder builder = BidResponseDTO.builder()
                .id(bid.getId())
                .taskId(bid.getTaskId())
                .taskerId(bid.getTaskerId())
                .customerId(bid.getCustomerId())
                .amount(bid.getAmount())
                .message(bid.getMessage())
                .status(bid.getStatus())
                .estimatedCompletionHours(bid.getEstimatedCompletionHours())
                .createdAt(bid.getCreatedAt())
                .updatedAt(bid.getUpdatedAt())
                .acceptedAt(bid.getAcceptedAt())
                .completedAt(bid.getCompletedAt())
                .withdrawnAt(bid.getWithdrawnAt())
                .rejectedAt(bid.getRejectedAt())
                .cancelledAt(bid.getCancelledAt())
                .cancellationReason(bid.getCancellationReason())
                .version(bid.getVersion());
        
        // Add tasker info if available
        try {
            TaskerInfoDTO taskerInfo = externalService.getTaskerInfo(bid.getTaskerId());
            builder.taskerInfo(taskerInfo);
        } catch (Exception e) {
            log.warn("Failed to get tasker info for tasker {}: {}", bid.getTaskerId(), e.getMessage());
        }
        
        // Add task info if available
        try {
            TaskInfoDTO taskInfo = externalService.getTaskInfo(bid.getTaskId());
            builder.taskInfo(taskInfo);
        } catch (Exception e) {
            log.warn("Failed to get task info for task {}: {}", bid.getTaskId(), e.getMessage());
        }
        
        return builder.build();
    }
}
