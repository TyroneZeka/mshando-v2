package com.mshando.biddingservice.service;

import com.mshando.biddingservice.dto.BidCreateDTO;
import com.mshando.biddingservice.dto.BidResponseDTO;
import com.mshando.biddingservice.exception.BidNotFoundException;
import com.mshando.biddingservice.exception.InvalidBidOperationException;
import com.mshando.biddingservice.model.Bid;
import com.mshando.biddingservice.model.BidStatus;
import com.mshando.biddingservice.repository.BidRepository;
import com.mshando.biddingservice.service.impl.BidServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BidServiceImpl.
 * 
 * This test class validates the business logic of the bidding service
 * including bid creation, status transitions, and validation rules.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class BidServiceImplTest {

    @Mock
    private BidRepository bidRepository;
    
    @Mock
    private ExternalService externalService;
    
    @InjectMocks
    private BidServiceImpl bidService;
    
    private Bid sampleBid;
    private BidCreateDTO sampleCreateDTO;
    
    @BeforeEach
    void setUp() {
        sampleBid = Bid.builder()
                .id(1L)
                .taskId(100L)
                .taskerId("tasker123")
                .amount(new BigDecimal("150.00"))
                .message("I can complete this task efficiently")
                .status(BidStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
        sampleCreateDTO = BidCreateDTO.builder()
                .taskId(100L)
                .taskerId("tasker123")
                .amount(new BigDecimal("150.00"))
                .message("I can complete this task efficiently")
                .build();
    }
    
    @Test
    void createBid_ValidInput_ShouldReturnBidResponseDTO() {
        // Given
        when(externalService.validateTask(100L)).thenReturn(true);
        when(externalService.validateTasker("tasker123")).thenReturn(true);
        when(bidRepository.countByTaskIdAndTaskerId(100L, "tasker123")).thenReturn(0L);
        when(bidRepository.save(any(Bid.class))).thenReturn(sampleBid);
        
        // When
        BidResponseDTO result = bidService.createBid(sampleCreateDTO);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getTaskId());
        assertEquals("tasker123", result.getTaskerId());
        assertEquals(new BigDecimal("150.00"), result.getAmount());
        assertEquals(BidStatus.PENDING, result.getStatus());
        
        verify(bidRepository).save(any(Bid.class));
        verify(externalService).validateTask(100L);
        verify(externalService).validateTasker("tasker123");
    }
    
    @Test
    void createBid_InvalidTask_ShouldThrowException() {
        // Given
        when(externalService.validateTask(100L)).thenReturn(false);
        
        // When & Then
        assertThrows(InvalidBidOperationException.class, 
                () -> bidService.createBid(sampleCreateDTO));
        
        verify(bidRepository, never()).save(any(Bid.class));
    }
    
    @Test
    void createBid_InvalidTasker_ShouldThrowException() {
        // Given
        when(externalService.validateTask(100L)).thenReturn(true);
        when(externalService.validateTasker("tasker123")).thenReturn(false);
        
        // When & Then
        assertThrows(InvalidBidOperationException.class, 
                () -> bidService.createBid(sampleCreateDTO));
        
        verify(bidRepository, never()).save(any(Bid.class));
    }
    
    @Test
    void createBid_ExceedsMaxBidsPerTask_ShouldThrowException() {
        // Given
        when(externalService.validateTask(100L)).thenReturn(true);
        when(externalService.validateTasker("tasker123")).thenReturn(true);
        when(bidRepository.countByTaskIdAndTaskerId(100L, "tasker123")).thenReturn(5L);
        
        // When & Then
        assertThrows(InvalidBidOperationException.class, 
                () -> bidService.createBid(sampleCreateDTO));
        
        verify(bidRepository, never()).save(any(Bid.class));
    }
    
    @Test
    void getBidById_ExistingBid_ShouldReturnBidResponseDTO() {
        // Given
        when(bidRepository.findById(1L)).thenReturn(Optional.of(sampleBid));
        
        // When
        BidResponseDTO result = bidService.getBidById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getTaskId());
        assertEquals("tasker123", result.getTaskerId());
    }
    
    @Test
    void getBidById_NonExistentBid_ShouldThrowException() {
        // Given
        when(bidRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(BidNotFoundException.class, () -> bidService.getBidById(1L));
    }
    
    @Test
    void acceptBid_ValidBid_ShouldUpdateStatus() {
        // Given
        when(bidRepository.findById(1L)).thenReturn(Optional.of(sampleBid));
        when(bidRepository.save(any(Bid.class))).thenReturn(sampleBid);
        
        // When
        BidResponseDTO result = bidService.acceptBid(1L, "customer123");
        
        // Then
        assertNotNull(result);
        verify(bidRepository).save(argThat(bid -> bid.getStatus() == BidStatus.ACCEPTED));
        verify(externalService).updateTaskStatus(100L, "IN_PROGRESS");
    }
    
    @Test
    void acceptBid_AlreadyAcceptedBid_ShouldThrowException() {
        // Given
        sampleBid.setStatus(BidStatus.ACCEPTED);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(sampleBid));
        
        // When & Then
        assertThrows(InvalidBidOperationException.class, 
                () -> bidService.acceptBid(1L, "customer123"));
    }
    
    @Test
    void rejectBid_ValidBid_ShouldUpdateStatus() {
        // Given
        when(bidRepository.findById(1L)).thenReturn(Optional.of(sampleBid));
        when(bidRepository.save(any(Bid.class))).thenReturn(sampleBid);
        
        // When
        BidResponseDTO result = bidService.rejectBid(1L, "customer123", "Found better offer");
        
        // Then
        assertNotNull(result);
        verify(bidRepository).save(argThat(bid -> 
                bid.getStatus() == BidStatus.REJECTED && 
                "Found better offer".equals(bid.getRejectionReason())));
    }
    
    @Test
    void withdrawBid_ValidBid_ShouldUpdateStatus() {
        // Given
        when(bidRepository.findById(1L)).thenReturn(Optional.of(sampleBid));
        when(bidRepository.save(any(Bid.class))).thenReturn(sampleBid);
        
        // When
        BidResponseDTO result = bidService.withdrawBid(1L, "tasker123", "No longer available");
        
        // Then
        assertNotNull(result);
        verify(bidRepository).save(argThat(bid -> 
                bid.getStatus() == BidStatus.WITHDRAWN && 
                "No longer available".equals(bid.getWithdrawalReason())));
    }
    
    @Test
    void withdrawBid_NotBidOwner_ShouldThrowException() {
        // Given
        when(bidRepository.findById(1L)).thenReturn(Optional.of(sampleBid));
        
        // When & Then
        assertThrows(InvalidBidOperationException.class, 
                () -> bidService.withdrawBid(1L, "otherTasker", "Reason"));
    }
    
    @Test
    void completeBid_ValidBid_ShouldUpdateStatus() {
        // Given
        sampleBid.setStatus(BidStatus.ACCEPTED);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(sampleBid));
        when(bidRepository.save(any(Bid.class))).thenReturn(sampleBid);
        
        // When
        BidResponseDTO result = bidService.completeBid(1L, "customer123");
        
        // Then
        assertNotNull(result);
        verify(bidRepository).save(argThat(bid -> bid.getStatus() == BidStatus.COMPLETED));
        verify(externalService).updateTaskStatus(100L, "COMPLETED");
    }
    
    @Test
    void completeBid_NotAcceptedBid_ShouldThrowException() {
        // Given
        when(bidRepository.findById(1L)).thenReturn(Optional.of(sampleBid));
        
        // When & Then
        assertThrows(InvalidBidOperationException.class, 
                () -> bidService.completeBid(1L, "customer123"));
    }
    
    @Test
    void getBidsByTask_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Bid> mockPage = new PageImpl<>(List.of(sampleBid));
        when(bidRepository.findByTaskIdOrderByCreatedAtDesc(100L, pageable))
                .thenReturn(mockPage);
        
        // When
        Page<BidResponseDTO> result = bidService.getBidsByTask(100L, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
    }
}
