package com.mshando.biddingservice;

import com.mshando.biddingservice.model.Bid;
import com.mshando.biddingservice.model.BidStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Test data factory for creating test objects
 */
public class TestDataFactory {

    public static Bid createValidBid() {
        return Bid.builder()
                .taskId(1L)
                .taskerId(100L)
                .customerId(200L)
                .amount(new BigDecimal("150.00"))
                .estimatedCompletionHours(8)
                .message("I can complete this task efficiently with high quality.")
                .status(BidStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(0L)
                .build();
    }

    public static Bid createAcceptedBid() {
        Bid bid = createValidBid();
        bid.setStatus(BidStatus.ACCEPTED);
        bid.setAcceptedAt(LocalDateTime.now());
        return bid;
    }

    public static Bid createRejectedBid() {
        Bid bid = createValidBid();
        bid.setStatus(BidStatus.REJECTED);
        bid.setRejectedAt(LocalDateTime.now());
        return bid;
    }

    public static Bid createCompletedBid() {
        Bid bid = createAcceptedBid();
        bid.setStatus(BidStatus.COMPLETED);
        bid.setCompletedAt(LocalDateTime.now());
        return bid;
    }

    public static Bid createCancelledBid() {
        Bid bid = createAcceptedBid();
        bid.setStatus(BidStatus.CANCELLED);
        bid.setCancelledAt(LocalDateTime.now());
        bid.setCancellationReason("Customer changed requirements");
        return bid;
    }

    public static Bid createWithdrawnBid() {
        Bid bid = createValidBid();
        bid.setStatus(BidStatus.WITHDRAWN);
        bid.setWithdrawnAt(LocalDateTime.now());
        return bid;
    }

    public static Bid createBidWithTaskId(Long taskId) {
        Bid bid = createValidBid();
        bid.setTaskId(taskId);
        return bid;
    }

    public static Bid createBidWithTaskerId(Long taskerId) {
        Bid bid = createValidBid();
        bid.setTaskerId(taskerId);
        return bid;
    }

    public static Bid createBidWithCustomerId(Long customerId) {
        Bid bid = createValidBid();
        bid.setCustomerId(customerId);
        return bid;
    }

    public static Bid createBidWithAmount(BigDecimal amount) {
        Bid bid = createValidBid();
        bid.setAmount(amount);
        return bid;
    }

    public static Bid createBidWithStatus(BidStatus status) {
        Bid bid = createValidBid();
        bid.setStatus(status);
        return bid;
    }

    public static Bid createBidWithTaskIdAndStatus(Long taskId, BidStatus status) {
        Bid bid = createValidBid();
        bid.setTaskId(taskId);
        bid.setStatus(status);
        return bid;
    }

    public static Bid createWithStatus(BidStatus status) {
        return createBidWithStatus(status);
    }
}
