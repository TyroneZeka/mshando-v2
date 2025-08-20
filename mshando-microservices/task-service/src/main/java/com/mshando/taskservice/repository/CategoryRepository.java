package com.mshando.taskservice.repository;

import com.mshando.taskservice.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find category by name
     * @param name category name
     * @return Optional category
     */
    Optional<Category> findByName(String name);
    
    /**
     * Find all active categories
     * @return list of active categories
     */
    List<Category> findByIsActiveTrue();
    
    /**
     * Check if category exists by name
     * @param name category name
     * @return true if exists
     */
    boolean existsByName(String name);
    
    /**
     * Check if category exists by name (case insensitive)
     * @param name category name
     * @return true if exists
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Find categories by name containing string (case insensitive) and active status
     * @param name search string
     * @return list of active categories
     */
    List<Category> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    
    /**
     * Check if category exists by ID and is active
     * @param id category ID
     * @return true if exists and active
     */
    boolean existsByIdAndIsActiveTrue(Long id);
    
    /**
     * Find categories with task count
     * @return list of categories with task counts
     */
    @Query("SELECT c FROM Category c LEFT JOIN c.tasks t WHERE c.isActive = true GROUP BY c ORDER BY COUNT(t) DESC")
    List<Category> findActiveCategoriesOrderByTaskCount();
}
