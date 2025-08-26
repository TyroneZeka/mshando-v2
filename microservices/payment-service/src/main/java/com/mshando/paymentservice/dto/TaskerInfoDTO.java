package com.mshando.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for tasker information.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskerInfoDTO {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Double rating;
    private Integer completedTasks;
    private String specialization;
}
