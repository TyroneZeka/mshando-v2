package com.mshando.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for task information.
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
    private String status;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime deadline;
}
