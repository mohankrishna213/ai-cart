package org.techm.samples.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.techm.samples.entity.Categories;
import org.techm.samples.exception.ResourceNotFoundException;
import org.techm.samples.repository.CategoriesRepository;
import org.techm.samples.service.categories.CategoriesServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

  @Mock
  private CategoriesRepository categoryRepository;

  @InjectMocks
  private CategoriesServiceImpl categoryService;

  @Test
  void getAllCategories_returnsList() {
    when(categoryRepository.findAll())
      .thenReturn(List.of(new Categories("A","foo")));

    var list = categoryService.getAllCategories();
    assertThat(list).hasSize(1);
    verify(categoryRepository).findAll();
  }

  @Test
  void getCategoryById_exists() {
    Categories c = new Categories("Toys","Fun");
    c.setId(5L);
    when(categoryRepository.findById(5L))
      .thenReturn(Optional.of(c));

    var result = categoryService.getCategoryById(5L);
    assertThat(result.getName()).isEqualTo("Toys");
  }

  @Test
  void getCategoryById_notFound_throws() {
    when(categoryRepository.findById(99L))
      .thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.getCategoryById(99L))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessageContaining("Category not found with id: 99");
  }

  @Test
  void createCategory_savesAndReturns() {
    Categories input = new Categories("New","desc");
    when(categoryRepository.save(input))
      .thenReturn(input);

    var created = categoryService.addCategory(input);
    assertThat(created).isSameAs(input);
    verify(categoryRepository).save(input);
  }

  @Test
  void deleteCategory_callsRepository() {
    
    Categories toDelete = new Categories("X","desc");
    toDelete.setId(7L);
    when(categoryRepository.findById(7L))
      .thenReturn(Optional.of(toDelete));

    
    doNothing().when(categoryRepository).deleteById(7L);

   
    categoryService.deleteCategory(7L);

    
    verify(categoryRepository).deleteById(7L);
  }

  @Test
  void deleteCategory_notFound_throws() {
    when(categoryRepository.findById(7L))
      .thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.deleteCategory(7L))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessageContaining("Category not found with id: 7");
  }
}
