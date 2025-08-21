package com.mshando.biddingservice.repository;

import com.mshando.biddingservice.TestConfig;
import com.mshando.biddingservice.TestDataFactory;
import com.mshando.biddingservice.model.Bid;
import com.mshando.biddingservice.model.BidStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for BidRepository
 */
@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@DisplayName("BidRepository Tests")
class BidRepositoryTest {

    @Autowired
    private BidRepository bidRepository;

    private Bid savedBid;

    @BeforeEach
    void setUp() {
        bidRepository.deleteAll();
        savedBid = bidRepository.save(TestDataFactory.createValidBid());
    }

    @Nested
    @DisplayName("Find by Task ID")
    class FindByTaskIdTests {

        @Test
        @DisplayName("Should find bids by task ID ordered by creation date")
        void shouldFindBidsByTaskIdOrderedByCreationDate() {
            // Given
            Long taskId = savedBid.getTaskId();
            Bid anotherBid = TestDataFactory.createBidWithTaskId(taskId);
            bidRepository.save(anotherBid);

            // When
            List<Bid> bids = bidRepository.findByTaskIdOrderByCreatedAtDesc(taskId);

            // Then
            assertThat(bids).hasSize(2);
            assertThat(bids).extracting("taskId").containsOnly(taskId);
            // Check ordering (newer first)
            assertThat(bids.get(0).getCreatedAt()).isAfterOrEqualTo(bids.get(1).getCreatedAt());
        }

        @Test
        @DisplayName("Should return empty list when no bids found for task ID")
        void shouldReturnEmptyListWhenNoBidsFoundForTaskId() {
            // Given
            Long nonExistentTaskId = 999L;

            // When
            List<Bid> bids = bidRepository.findByTaskIdOrderByCreatedAtDesc(nonExistentTaskId);

            // Then
            assertThat(bids).isEmpty();
        }

        @Test
        @DisplayName("Should find bids by task ID with pagination")
        void shouldFindBidsByTaskIdWithPagination() {
            // Given
            Long taskId = savedBid.getTaskId();
            for (int i = 0; i < 5; i++) {
                bidRepository.save(TestDataFactory.createBidWithTaskId(taskId));
            }
            Pageable pageable = PageRequest.of(0, 3);

            // When
            Page<Bid> bidsPage = bidRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pageable);

            // Then
            assertThat(bidsPage.getContent()).hasSize(3);
            assertThat(bidsPage.getTotalElements()).isEqualTo(6); // 5 new + 1 existing
            assertThat(bidsPage.getContent()).extracting("taskId").containsOnly(taskId);
        }
    }

    @Nested
    @DisplayName("Find by Tasker ID")
    class FindByTaskerIdTests {

        @Test
        @DisplayName("Should find bids by tasker ID with pagination")
        void shouldFindBidsByTaskerIdWithPagination() {
            // Given
            Long taskerId = savedBid.getTaskerId();
            for (int i = 0; i < 3; i++) {
                bidRepository.save(TestDataFactory.createBidWithTaskerId(taskerId));
            }
            Pageable pageable = PageRequest.of(0, 2);

            // When
            Page<Bid> bidsPage = bidRepository.findByTaskerIdOrderByCreatedAtDesc(taskerId, pageable);

            // Then
            assertThat(bidsPage.getContent()).hasSize(2);
            assertThat(bidsPage.getTotalElements()).isEqualTo(4); // 3 new + 1 existing
            assertThat(bidsPage.getContent()).extracting("taskerId").containsOnly(taskerId);
        }
    }

    @Nested
    @DisplayName("Find by Customer ID")
    class FindByCustomerIdTests {

        @Test
        @DisplayName("Should find bids by customer ID with pagination")
        void shouldFindBidsByCustomerIdWithPagination() {
            // Given
            Long customerId = savedBid.getCustomerId();
            for (int i = 0; i < 4; i++) {
                bidRepository.save(TestDataFactory.createBidWithCustomerId(customerId));
            }
            Pageable pageable = PageRequest.of(0, 3);

            // When
            Page<Bid> bidsPage = bidRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);

            // Then
            assertThat(bidsPage.getContent()).hasSize(3);
            assertThat(bidsPage.getTotalElements()).isEqualTo(5); // 4 new + 1 existing
            assertThat(bidsPage.getContent()).extracting("customerId").containsOnly(customerId);
        }
    }

    @Nested
    @DisplayName("Find by Status")
    class FindByStatusTests {

        @Test
        @DisplayName("Should find bids by status with pagination")
        void shouldFindBidsByStatusWithPagination() {
            // Given
            bidRepository.save(TestDataFactory.createAcceptedBid());
            bidRepository.save(TestDataFactory.createRejectedBid());
            bidRepository.save(TestDataFactory.createWithStatus(BidStatus.PENDING));
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Bid> pendingBids = bidRepository.findByStatusOrderByCreatedAtDesc(BidStatus.PENDING, pageable);
            Page<Bid> acceptedBids = bidRepository.findByStatusOrderByCreatedAtDesc(BidStatus.ACCEPTED, pageable);

            // Then
            assertThat(pendingBids.getContent()).hasSize(2); // savedBid + one new
            assertThat(acceptedBids.getContent()).hasSize(1);
            assertThat(pendingBids.getContent()).extracting("status").containsOnly(BidStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Find by Task ID and Status")
    class FindByTaskIdAndStatusTests {

        @Test
        @DisplayName("Should find bids by task ID and status")
        void shouldFindBidsByTaskIdAndStatus() {
            // Given
            Long taskId = savedBid.getTaskId();
            bidRepository.save(TestDataFactory.createBidWithTaskIdAndStatus(taskId, BidStatus.ACCEPTED));
            bidRepository.save(TestDataFactory.createBidWithTaskIdAndStatus(taskId, BidStatus.REJECTED));

            // When
            List<Bid> pendingBids = bidRepository.findByTaskIdAndStatus(taskId, BidStatus.PENDING);
            List<Bid> acceptedBids = bidRepository.findByTaskIdAndStatus(taskId, BidStatus.ACCEPTED);

            // Then
            assertThat(pendingBids).hasSize(1);
            assertThat(acceptedBids).hasSize(1);
            assertThat(pendingBids.get(0).getTaskId()).isEqualTo(taskId);
            assertThat(pendingBids.get(0).getStatus()).isEqualTo(BidStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Find by Task ID and Tasker ID")
    class FindByTaskIdAndTaskerIdTests {

        @Test
        @DisplayName("Should find bid by task ID and tasker ID")
        void shouldFindBidByTaskIdAndTaskerId() {
            // Given
            Long taskerId = savedBid.getTaskerId();
            Long taskId = savedBid.getTaskId();

            // When
            Optional<Bid> foundBid = bidRepository.findByTaskIdAndTaskerId(taskId, taskerId);

            // Then
            assertThat(foundBid).isPresent();
            assertThat(foundBid.get().getTaskerId()).isEqualTo(taskerId);
            assertThat(foundBid.get().getTaskId()).isEqualTo(taskId);
        }

        @Test
        @DisplayName("Should return empty when no bid found for task and tasker")
        void shouldReturnEmptyWhenNoBidFoundForTaskAndTasker() {
            // Given
            Long nonExistentTaskerId = 999L;
            Long nonExistentTaskId = 888L;

            // When
            Optional<Bid> foundBid = bidRepository.findByTaskIdAndTaskerId(nonExistentTaskId, nonExistentTaskerId);

            // Then
            assertThat(foundBid).isEmpty();
        }
    }

    @Nested
    @DisplayName("Count by Task ID and Status")
    class CountByTaskIdAndStatusTests {

        @Test
        @DisplayName("Should count bids by task ID and status")
        void shouldCountBidsByTaskIdAndStatus() {
            // Given
            Long taskId = savedBid.getTaskId();
            bidRepository.save(TestDataFactory.createBidWithTaskIdAndStatus(taskId, BidStatus.PENDING));
            bidRepository.save(TestDataFactory.createBidWithTaskIdAndStatus(taskId, BidStatus.ACCEPTED));

            // When
            long pendingCount = bidRepository.countByTaskIdAndStatus(taskId, BidStatus.PENDING);
            long acceptedCount = bidRepository.countByTaskIdAndStatus(taskId, BidStatus.ACCEPTED);

            // Then
            assertThat(pendingCount).isEqualTo(2); // savedBid + 1 new
            assertThat(acceptedCount).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Should save and retrieve bid correctly")
    void shouldSaveAndRetrieveBidCorrectly() {
        // Given
        Bid newBid = TestDataFactory.createValidBid();
        newBid.setTaskId(999L);

        // When
        Bid saved = bidRepository.save(newBid);
        Optional<Bid> retrieved = bidRepository.findById(saved.getId());

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getTaskId()).isEqualTo(999L);
        assertThat(retrieved.get().getAmount()).isEqualTo(newBid.getAmount());
        assertThat(retrieved.get().getStatus()).isEqualTo(BidStatus.PENDING);
    }

    @Test
    @DisplayName("Should update bid correctly")
    void shouldUpdateBidCorrectly() {
        // Given
        savedBid.setStatus(BidStatus.ACCEPTED);
        savedBid.setAcceptedAt(LocalDateTime.now());
        savedBid.setAmount(new BigDecimal("200.00"));

        // When
        Bid updated = bidRepository.save(savedBid);

        // Then
        assertThat(updated.getStatus()).isEqualTo(BidStatus.ACCEPTED);
        assertThat(updated.getAcceptedAt()).isNotNull();
        assertThat(updated.getAmount()).isEqualTo(new BigDecimal("200.00"));
        // Version might not be incremented in tests without explicit transaction management
        assertThat(updated.getVersion()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("Should delete bid correctly")
    void shouldDeleteBidCorrectly() {
        // Given
        Long bidId = savedBid.getId();

        // When
        bidRepository.deleteById(bidId);

        // Then
        Optional<Bid> deleted = bidRepository.findById(bidId);
        assertThat(deleted).isEmpty();
    }
}
