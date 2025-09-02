package com.mshando.taskservice.controller;

import com.mshando.taskservice.dto.request.CategoryCreateRequestDTO;
import com.mshando.taskservice.dto.response.CategoryResponseDTO;
import com.mshando.taskservice.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Category management
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management", description = "APIs for managing task categories")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @Operation(summary = "Create a new category", description = "Create a new task category (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @ApiResponse(responseCode = "409", description = "Category name already exists")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CategoryCreateRequestDTO requestDTO) {
        log.info("Creating new category: {}", requestDTO.getName());
        
        CategoryResponseDTO responseDTO = categoryService.createCategory(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
    
    @Operation(summary = "Get category by ID", description = "Retrieve a specific category by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.debug("Fetching category with ID: {}", id);
        
        CategoryResponseDTO responseDTO = categoryService.getCategoryById(id);
        return ResponseEntity.ok(responseDTO);
    }
    
    @Operation(summary = "Get all active categories", description = "Retrieve all active categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    @GetMapping("/active")
    public ResponseEntity<List<CategoryResponseDTO>> getAllActiveCategories() {
        log.debug("Fetching all active categories");
        
        List<CategoryResponseDTO> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }
    
    @Operation(summary = "Get categories with pagination", description = "Retrieve categories with pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CategoryResponseDTO>> getCategoriesWithPagination(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.debug("Fetching categories with pagination: {}", pageable);
        
        Page<CategoryResponseDTO> categoryPage = categoryService.getCategoriesWithPagination(pageable);
        return ResponseEntity.ok(categoryPage);
    }
    
    @Operation(summary = "Update category", description = "Update an existing category (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Category name already exists")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Valid @RequestBody CategoryCreateRequestDTO requestDTO) {
        log.info("Updating category with ID: {}", id);
        
        CategoryResponseDTO responseDTO = categoryService.updateCategory(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }
    
    @Operation(summary = "Deactivate category", description = "Deactivate a category (soft delete) (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deactivated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.info("Deactivating category with ID: {}", id);
        
        categoryService.deactivateCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Activate category", description = "Activate a category (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category activated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.info("Activating category with ID: {}", id);
        
        categoryService.activateCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Delete category", description = "Permanently delete a category (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Category has associated tasks")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.info("Deleting category with ID: {}", id);
        
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Search categories", description = "Search categories by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories found")
    })
    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponseDTO>> searchCategories(
            @Parameter(description = "Search query") @RequestParam String q) {
        log.debug("Searching categories with query: {}", q);
        
        List<CategoryResponseDTO> categories = categoryService.searchCategoriesByName(q);
        return ResponseEntity.ok(categories);
    }
}
