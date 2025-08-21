package com.mshando.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for customer information.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerInfoDTO {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Double rating;
    private Integer totalTasks;
}
