package com.mshando.taskservice.service;

import com.mshando.taskservice.dto.request.CategoryCreateRequestDTO;
import com.mshando.taskservice.dto.response.CategoryResponseDTO;
import com.mshando.taskservice.exception.CategoryNotFoundException;
import com.mshando.taskservice.model.Category;
import com.mshando.taskservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing Categories
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    /**
     * Create a new category
     * @param requestDTO category creation request
     * @return created category response
     */
    public CategoryResponseDTO createCategory(CategoryCreateRequestDTO requestDTO) {
        log.info("Creating new category with name: {}", requestDTO.getName());
        
        // Check if category name already exists
        if (categoryRepository.existsByNameIgnoreCase(requestDTO.getName())) {
            throw new IllegalArgumentException("Category with name '" + requestDTO.getName() + "' already exists");
        }
        
        Category category = Category.builder()
                .name(requestDTO.getName())
                .description(requestDTO.getDescription())
                .iconUrl(requestDTO.getIconUrl())
                .isActive(true)
                .build();
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        
        return mapToResponseDTO(savedCategory);
    }
    
    /**
     * Get category by ID
     * @param id category ID
     * @return category response
     */
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {
        log.debug("Fetching category with ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
        
        return mapToResponseDTO(category);
    }
    
    /**
     * Get all active categories
     * @return list of active categories
     */
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllActiveCategories() {
        log.debug("Fetching all active categories");
        
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        return categories.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get categories with pagination
     * @param pageable pagination information
     * @return page of categories
     */
    @Transactional(readOnly = true)
    public Page<CategoryResponseDTO> getCategoriesWithPagination(Pageable pageable) {
        log.debug("Fetching categories with pagination: {}", pageable);
        
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        return categoryPage.map(this::mapToResponseDTO);
    }
    
    /**
     * Update category
     * @param id category ID
     * @param requestDTO update request
     * @return updated category response
     */
    public CategoryResponseDTO updateCategory(Long id, CategoryCreateRequestDTO requestDTO) {
        log.info("Updating category with ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
        
        // Check if the new name conflicts with existing categories (excluding current category)
        if (!category.getName().equalsIgnoreCase(requestDTO.getName()) && 
            categoryRepository.existsByNameIgnoreCase(requestDTO.getName())) {
            throw new IllegalArgumentException("Category with name '" + requestDTO.getName() + "' already exists");
        }
        
        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());
        category.setIconUrl(requestDTO.getIconUrl());
        
        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", updatedCategory.getId());
        
        return mapToResponseDTO(updatedCategory);
    }
    
    /**
     * Deactivate category (soft delete)
     * @param id category ID
     */
    public void deactivateCategory(Long id) {
        log.info("Deactivating category with ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
        
        category.setIsActive(false);
        categoryRepository.save(category);
        
        log.info("Category deactivated successfully with ID: {}", id);
    }
    
    /**
     * Activate category
     * @param id category ID
     */
    public void activateCategory(Long id) {
        log.info("Activating category with ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
        
        category.setIsActive(true);
        categoryRepository.save(category);
        
        log.info("Category activated successfully with ID: {}", id);
    }
    
    /**
     * Delete category permanently
     * @param id category ID
     */
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);
        
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category not found with ID: " + id);
        }
        
        // TODO: Check if category has associated tasks before deletion
        categoryRepository.deleteById(id);
        
        log.info("Category deleted successfully with ID: {}", id);
    }
    
    /**
     * Search categories by name
     * @param name category name
     * @return list of matching categories
     */
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> searchCategoriesByName(String name) {
        log.debug("Searching categories by name: {}", name);
        
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name);
        return categories.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if category exists and is active
     * @param id category ID
     * @return true if exists and active
     */
    @Transactional(readOnly = true)
    public boolean existsAndActive(Long id) {
        return categoryRepository.existsByIdAndIsActiveTrue(id);
    }
    
    /**
     * Map Category entity to CategoryResponseDTO
     * @param category category entity
     * @return category response DTO
     */
    private CategoryResponseDTO mapToResponseDTO(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .active(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
