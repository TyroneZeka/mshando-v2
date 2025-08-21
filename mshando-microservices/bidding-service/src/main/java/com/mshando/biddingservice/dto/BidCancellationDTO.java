package com.mshando.biddingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for bid cancellation requests.
 * 
 * This DTO contains the cancellation reason when
 * cancelling an accepted bid.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "BidCancellation", description = "Request body for cancelling an accepted bid")
public class BidCancellationDTO {
    
    @Schema(description = "Reason for cancelling the bid", 
            example = "Task requirements have changed and are no longer suitable.",
            maxLength = 500,
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Cancellation reason is required")
    @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
    private String reason;
}
