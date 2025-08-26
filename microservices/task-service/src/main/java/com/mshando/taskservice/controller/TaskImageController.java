package com.mshando.taskservice.controller;

import com.mshando.taskservice.dto.response.TaskImageResponseDTO;
import com.mshando.taskservice.service.TaskImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for Task Image management
 */
@RestController
@RequestMapping("/api/v1/tasks/{taskId}/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Task Image Management", description = "APIs for managing task images and file uploads")
public class TaskImageController {

    private final TaskImageService taskImageService;

    @Operation(summary = "Upload task image", description = "Upload an image for a task (Task owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Task owner access required"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "413", description = "File too large")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TaskImageResponseDTO> uploadTaskImage(
            @Parameter(description = "Task ID") @PathVariable Long taskId,
            @Parameter(description = "Image file") @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Uploading image for task {} by user: {}", taskId, userDetails.getUsername());

        Long userId = extractUserIdFromUserDetails(userDetails);
        TaskImageResponseDTO responseDTO = taskImageService.uploadTaskImage(taskId, file, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(summary = "Get task images", description = "Get all images for a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Images retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping
    public ResponseEntity<List<TaskImageResponseDTO>> getTaskImages(
            @Parameter(description = "Task ID") @PathVariable Long taskId) {
        log.debug("Fetching images for task: {}", taskId);

        List<TaskImageResponseDTO> images = taskImageService.getTaskImages(taskId);
        return ResponseEntity.ok(images);
    }

    @Operation(summary = "Get primary image", description = "Get the primary image for a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Primary image retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Task or primary image not found")
    })
    @GetMapping("/primary")
    public ResponseEntity<TaskImageResponseDTO> getPrimaryImage(
            @Parameter(description = "Task ID") @PathVariable Long taskId) {
        log.debug("Fetching primary image for task: {}", taskId);

        TaskImageResponseDTO primaryImage = taskImageService.getPrimaryImage(taskId);
        if (primaryImage == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(primaryImage);
    }

    @Operation(summary = "Set primary image", description = "Set an image as the primary image for a task (Task owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Primary image set successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Task owner access required"),
            @ApiResponse(responseCode = "404", description = "Task or image not found")
    })
    @PatchMapping("/{imageId}/set-primary")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TaskImageResponseDTO> setPrimaryImage(
            @Parameter(description = "Task ID") @PathVariable Long taskId,
            @Parameter(description = "Image ID") @PathVariable Long imageId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Setting primary image {} for task {} by user: {}", imageId, taskId, userDetails.getUsername());

        Long userId = extractUserIdFromUserDetails(userDetails);
        TaskImageResponseDTO responseDTO = taskImageService.setPrimaryImage(taskId, imageId, userId);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Delete task image", description = "Delete a task image (Task owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Task owner access required"),
            @ApiResponse(responseCode = "404", description = "Task or image not found")
    })
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> deleteTaskImage(
            @Parameter(description = "Task ID") @PathVariable Long taskId,
            @Parameter(description = "Image ID") @PathVariable Long imageId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Deleting image {} for task {} by user: {}", imageId, taskId, userDetails.getUsername());

        Long userId = extractUserIdFromUserDetails(userDetails);
        taskImageService.deleteTaskImage(taskId, imageId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Upload multiple images", description = "Upload multiple images for a task at once (Task owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Images uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid files or request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Task owner access required"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "413", description = "Files too large")
    })
    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<TaskImageResponseDTO>> uploadMultipleImages(
            @Parameter(description = "Task ID") @PathVariable Long taskId,
            @Parameter(description = "Image files") @RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Uploading {} images for task {} by user: {}", files.length, taskId, userDetails.getUsername());

        Long userId = extractUserIdFromUserDetails(userDetails);
        List<TaskImageResponseDTO> uploadedImages = new java.util.ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    TaskImageResponseDTO uploadedImage = taskImageService.uploadTaskImage(taskId, file, userId);
                    uploadedImages.add(uploadedImage);
                } catch (Exception e) {
                    log.warn("Failed to upload file {}: {}", file.getOriginalFilename(), e.getMessage());
                    // Continue with other files, but could also implement partial success response
                }
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedImages);
    }

    /**
     * Extract user ID from UserDetails (assuming the username is the user ID)
     * This will be properly implemented once we integrate with the user service
     */
    private Long extractUserIdFromUserDetails(UserDetails userDetails) {
        // TODO: Implement proper user ID extraction from JWT token
        // For now, assuming username contains the user ID
        try {
            return Long.parseLong(userDetails.getUsername());
        } catch (NumberFormatException e) {
            log.warn("Unable to extract user ID from username: {}", userDetails.getUsername());
            return 1L; // Default fallback - will be replaced with proper JWT implementation
        }
    }
}
