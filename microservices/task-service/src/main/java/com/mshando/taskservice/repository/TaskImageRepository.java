package com.mshando.taskservice.repository;

import com.mshando.taskservice.model.TaskImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for TaskImage entity
 */
@Repository
public interface TaskImageRepository extends JpaRepository<TaskImage, Long> {
    
    /**
     * Find images by task ID
     * @param taskId task ID
     * @return list of task images
     */
    List<TaskImage> findByTaskIdOrderByCreatedAtAsc(Long taskId);
    
    /**
     * Find primary image for a task
     * @param taskId task ID
     * @return primary task image
     */
    @Query("SELECT ti FROM TaskImage ti WHERE ti.task.id = :taskId AND ti.isPrimary = true")
    TaskImage findPrimaryImageByTaskId(@Param("taskId") Long taskId);
    
    /**
     * Count images for a task
     * @param taskId task ID
     * @return count of images
     */
    long countByTaskId(Long taskId);
    
    /**
     * Delete images by task ID
     * @param taskId task ID
     */
    void deleteByTaskId(Long taskId);
    
    /**
     * Check if a file name already exists for a task
     * @param taskId task ID
     * @param fileName file name
     * @return true if exists
     */
    boolean existsByTaskIdAndFileName(Long taskId, String fileName);
}
