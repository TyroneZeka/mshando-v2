package com.mshando.biddingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for task information in bid responses.
 * 
 * This DTO contains basic task information that is included
 * in bid responses for taskers to reference the task details.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskInfoDTO {
    
    private Long id;
    private String title;
    private String description;
    private BigDecimal budget;
    private LocalDateTime dueDate;
    private String location;
    private String status;
    private String categoryName;
    private String customerName;
    private Long customerId;
}
