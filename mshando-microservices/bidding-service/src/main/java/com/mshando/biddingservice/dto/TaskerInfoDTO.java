package com.mshando.biddingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for tasker information in bid responses.
 * 
 * This DTO contains basic tasker information that is included
 * in bid responses for customers to evaluate bids.
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
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Double rating;
    private Integer completedTasks;
    private String profilePictureUrl;
    private String bio;
    private Boolean isVerified;
}
