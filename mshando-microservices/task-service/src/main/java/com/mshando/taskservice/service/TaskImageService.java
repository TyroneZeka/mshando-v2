package com.mshando.taskservice.service;

import com.mshando.taskservice.dto.response.TaskImageResponseDTO;
import com.mshando.taskservice.exception.TaskNotFoundException;
import com.mshando.taskservice.exception.UnauthorizedAccessException;
import com.mshando.taskservice.model.Task;
import com.mshando.taskservice.model.TaskImage;
import com.mshando.taskservice.repository.TaskImageRepository;
import com.mshando.taskservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing Task Images
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskImageService {
    
    private final TaskImageRepository taskImageRepository;
    private final TaskRepository taskRepository;
    
    @Value("${mshando.file-upload.directory:./uploads/tasks}")
    private String uploadDirectory;
    
    @Value("${mshando.file-upload.max-images-per-task:5}")
    private int maxImagesPerTask;
    
    @Value("${mshando.file-upload.max-file-size:5242880}")
    private long maxFileSize; // 5MB default
    
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    /**
     * Upload an image for a task
     * @param taskId task ID
     * @param file image file
     * @param userId user ID from JWT token
     * @return uploaded task image response
     */
    public TaskImageResponseDTO uploadTaskImage(Long taskId, MultipartFile file, Long userId) {
        log.info("Uploading image for task ID: {} by user: {}", taskId, userId);
        
        // Validate task exists and user is owner
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));
        
        if (!task.getCustomerId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to upload images for this task");
        }
        
        // Validate file
        validateFile(file);
        
        // Check image count limit
        long currentImageCount = taskImageRepository.countByTaskId(taskId);
        if (currentImageCount >= maxImagesPerTask) {
            throw new IllegalArgumentException("Maximum " + maxImagesPerTask + " images allowed per task");
        }
        
        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = generateUniqueFilename(taskId, fileExtension);
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Save file to disk
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Determine if this should be the primary image (first image uploaded)
            boolean isPrimary = currentImageCount == 0;
            
            // Save image metadata to database
            TaskImage taskImage = TaskImage.builder()
                    .task(task)
                    .fileName(uniqueFilename)
                    .originalFileName(originalFilename)
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .isPrimary(isPrimary)
                    .build();
            
            TaskImage savedImage = taskImageRepository.save(taskImage);
            log.info("Image uploaded successfully with ID: {}", savedImage.getId());
            
            return mapToResponseDTO(savedImage);
            
        } catch (IOException e) {
            log.error("Error uploading file for task ID: {}", taskId, e);
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }
    
    /**
     * Get all images for a task
     * @param taskId task ID
     * @return list of task images
     */
    @Transactional(readOnly = true)
    public List<TaskImageResponseDTO> getTaskImages(Long taskId) {
        log.debug("Fetching images for task ID: {}", taskId);
        
        // Validate task exists
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException("Task not found with ID: " + taskId);
        }
        
        List<TaskImage> images = taskImageRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        return images.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get primary image for a task
     * @param taskId task ID
     * @return primary task image or null if none exists
     */
    @Transactional(readOnly = true)
    public TaskImageResponseDTO getPrimaryImage(Long taskId) {
        log.debug("Fetching primary image for task ID: {}", taskId);
        
        TaskImage primaryImage = taskImageRepository.findPrimaryImageByTaskId(taskId);
        return primaryImage != null ? mapToResponseDTO(primaryImage) : null;
    }
    
    /**
     * Set an image as primary for a task
     * @param taskId task ID
     * @param imageId image ID
     * @param userId user ID from JWT token
     * @return updated task image response
     */
    public TaskImageResponseDTO setPrimaryImage(Long taskId, Long imageId, Long userId) {
        log.info("Setting primary image {} for task ID: {} by user: {}", imageId, taskId, userId);
        
        // Validate task exists and user is owner
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));
        
        if (!task.getCustomerId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to modify images for this task");
        }
        
        // Get the image to set as primary
        TaskImage newPrimaryImage = taskImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found with ID: " + imageId));
        
        if (!newPrimaryImage.getTask().getId().equals(taskId)) {
            throw new IllegalArgumentException("Image does not belong to the specified task");
        }
        
        // Remove primary flag from current primary image
        TaskImage currentPrimary = taskImageRepository.findPrimaryImageByTaskId(taskId);
        if (currentPrimary != null) {
            currentPrimary.setIsPrimary(false);
            taskImageRepository.save(currentPrimary);
        }
        
        // Set new primary image
        newPrimaryImage.setIsPrimary(true);
        TaskImage updatedImage = taskImageRepository.save(newPrimaryImage);
        
        log.info("Primary image set successfully for task ID: {}", taskId);
        return mapToResponseDTO(updatedImage);
    }
    
    /**
     * Delete a task image
     * @param taskId task ID
     * @param imageId image ID
     * @param userId user ID from JWT token
     */
    public void deleteTaskImage(Long taskId, Long imageId, Long userId) {
        log.info("Deleting image {} for task ID: {} by user: {}", imageId, taskId, userId);
        
        // Validate task exists and user is owner
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));
        
        if (!task.getCustomerId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to delete images for this task");
        }
        
        // Get the image to delete
        TaskImage imageToDelete = taskImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found with ID: " + imageId));
        
        if (!imageToDelete.getTask().getId().equals(taskId)) {
            throw new IllegalArgumentException("Image does not belong to the specified task");
        }
        
        try {
            // Delete file from disk
            Path filePath = Paths.get(imageToDelete.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            
            // If this was the primary image, set another image as primary
            if (imageToDelete.getIsPrimary()) {
                List<TaskImage> remainingImages = taskImageRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
                remainingImages.remove(imageToDelete); // Remove the image we're about to delete
                
                if (!remainingImages.isEmpty()) {
                    TaskImage newPrimary = remainingImages.get(0);
                    newPrimary.setIsPrimary(true);
                    taskImageRepository.save(newPrimary);
                }
            }
            
            // Delete from database
            taskImageRepository.delete(imageToDelete);
            
            log.info("Image deleted successfully with ID: {}", imageId);
            
        } catch (IOException e) {
            log.error("Error deleting file for image ID: {}", imageId, e);
            // Continue with database deletion even if file deletion fails
            taskImageRepository.delete(imageToDelete);
        }
    }
    
    /**
     * Delete all images for a task (used when task is deleted)
     * @param taskId task ID
     */
    public void deleteAllTaskImages(Long taskId) {
        log.info("Deleting all images for task ID: {}", taskId);
        
        List<TaskImage> images = taskImageRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        
        for (TaskImage image : images) {
            try {
                Path filePath = Paths.get(image.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException e) {
                log.warn("Failed to delete file: {}", image.getFilePath(), e);
            }
        }
        
        taskImageRepository.deleteByTaskId(taskId);
        log.info("All images deleted for task ID: {}", taskId);
    }
    
    /**
     * Validate uploaded file
     * @param file multipart file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: " + String.join(", ", ALLOWED_CONTENT_TYPES));
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File must have a valid filename");
        }
    }
    
    /**
     * Generate unique filename for uploaded file
     * @param taskId task ID
     * @param fileExtension file extension
     * @return unique filename
     */
    private String generateUniqueFilename(Long taskId, String fileExtension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("task_%d_%s_%s%s", taskId, timestamp, uuid, fileExtension);
    }
    
    /**
     * Get file extension from filename
     * @param filename original filename
     * @return file extension including the dot
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
    
    /**
     * Map TaskImage entity to TaskImageResponseDTO
     * @param taskImage task image entity
     * @return task image response DTO
     */
    private TaskImageResponseDTO mapToResponseDTO(TaskImage taskImage) {
        return TaskImageResponseDTO.builder()
                .id(taskImage.getId())
                .taskId(taskImage.getTask().getId())
                .fileName(taskImage.getFileName())
                .originalFileName(taskImage.getOriginalFileName())
                .fileSize(taskImage.getFileSize())
                .contentType(taskImage.getContentType())
                .isPrimary(taskImage.getIsPrimary())
                .createdAt(taskImage.getCreatedAt())
                .build();
    }
}
