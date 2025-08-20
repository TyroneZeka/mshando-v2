package com.mshando.taskservice.dto;

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
    private String imageUrl;
    private String originalFilename;
    private Long fileSize;
    private String contentType;
    private Boolean isPrimary;
    private LocalDateTime uploadedAt;
}
