package com.mshando.taskservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Task image response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskImageResponseDTO {
    
    private Long id;
    private Long taskId;
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
}
