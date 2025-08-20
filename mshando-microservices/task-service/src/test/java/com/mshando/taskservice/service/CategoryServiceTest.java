package com.mshando.taskservice.service;

import com.mshando.taskservice.dto.request.CategoryCreateRequestDTO;
import com.mshando.taskservice.dto.response.CategoryResponseDTO;
import com.mshando.taskservice.exception.CategoryNotFoundException;
import com.mshando.taskservice.model.Category;
import com.mshando.taskservice.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private CategoryCreateRequestDTO createRequestDTO;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Home Cleaning")
                .description("Professional home cleaning services")
                .iconUrl("https://example.com/icon.png")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequestDTO = CategoryCreateRequestDTO.builder()
                .name("Home Cleaning")
                .description("Professional home cleaning services")
                .iconUrl("https://example.com/icon.png")
                .build();
    }

    @Test
    @DisplayName("Should create category successfully")
    void createCategory_Success() {
        // Given
        when(categoryRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        CategoryResponseDTO result = categoryService.createCategory(createRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Home Cleaning");
        assertThat(result.getDescription()).isEqualTo("Professional home cleaning services");
        assertThat(result.getActive()).isTrue();

        verify(categoryRepository).existsByNameIgnoreCase("Home Cleaning");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when category name already exists")
    void createCategory_NameExists_ThrowsException() {
        // Given
        when(categoryRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(createRequestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category with name 'Home Cleaning' already exists");

        verify(categoryRepository).existsByNameIgnoreCase("Home Cleaning");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should get category by ID successfully")
    void getCategoryById_Success() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When
        CategoryResponseDTO result = categoryService.getCategoryById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Home Cleaning");

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void getCategoryById_NotFound_ThrowsException() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryById(1L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("Category not found with ID: 1");

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("Should get all active categories successfully")
    void getAllActiveCategories_Success() {
        // Given
        Category category2 = Category.builder()
                .id(2L)
                .name("Garden Work")
                .description("Garden maintenance services")
                .isActive(true)
                .build();

        List<Category> activeCategories = Arrays.asList(testCategory, category2);
        when(categoryRepository.findByIsActiveTrue()).thenReturn(activeCategories);

        // When
        List<CategoryResponseDTO> result = categoryService.getAllActiveCategories();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Home Cleaning");
        assertThat(result.get(1).getName()).isEqualTo("Garden Work");

        verify(categoryRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("Should get categories with pagination successfully")
    void getCategoriesWithPagination_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> categoryPage = new PageImpl<>(Arrays.asList(testCategory));
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        // When
        Page<CategoryResponseDTO> result = categoryService.getCategoriesWithPagination(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Home Cleaning");

        verify(categoryRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should update category successfully")
    void updateCategory_Success() {
        // Given
        CategoryCreateRequestDTO updateRequest = CategoryCreateRequestDTO.builder()
                .name("Updated Cleaning")
                .description("Updated description")
                .iconUrl("https://example.com/new-icon.png")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByNameIgnoreCase("Updated Cleaning")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        CategoryResponseDTO result = categoryService.updateCategory(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should deactivate category successfully")
    void deactivateCategory_Success() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        categoryService.deactivateCategory(1L);

        // Then
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(argThat(category -> !category.getIsActive()));
    }

    @Test
    @DisplayName("Should activate category successfully")
    void activateCategory_Success() {
        // Given
        testCategory.setIsActive(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        categoryService.activateCategory(1L);

        // Then
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(argThat(category -> category.getIsActive()));
    }

    @Test
    @DisplayName("Should delete category successfully")
    void deleteCategory_Success() {
        // Given
        when(categoryRepository.existsById(1L)).thenReturn(true);

        // When
        categoryService.deleteCategory(1L);

        // Then
        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent category")
    void deleteCategory_NotFound_ThrowsException() {
        // Given
        when(categoryRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("Category not found with ID: 1");

        verify(categoryRepository).existsById(1L);
        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should search categories by name successfully")
    void searchCategoriesByName_Success() {
        // Given
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.findByNameContainingIgnoreCaseAndIsActiveTrue("cleaning"))
                .thenReturn(categories);

        // When
        List<CategoryResponseDTO> result = categoryService.searchCategoriesByName("cleaning");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Home Cleaning");

        verify(categoryRepository).findByNameContainingIgnoreCaseAndIsActiveTrue("cleaning");
    }

    @Test
    @DisplayName("Should check if category exists and is active")
    void existsAndActive_Success() {
        // Given
        when(categoryRepository.existsByIdAndIsActiveTrue(1L)).thenReturn(true);

        // When
        boolean result = categoryService.existsAndActive(1L);

        // Then
        assertThat(result).isTrue();
        verify(categoryRepository).existsByIdAndIsActiveTrue(1L);
    }
}
