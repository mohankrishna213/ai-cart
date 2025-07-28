package org.techm.samples.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.techm.samples.entity.Categories;

@SpringBootTest
@AutoConfigureTestDatabase
public class CategoryRepositoryTest {

  @Autowired
  private CategoriesRepository categoryRepository;

  @Test
  void whenSave_thenFindById() {
    Categories cat = new Categories();
    cat.setName("Electronics");
    cat.setDescription("Gadgets and devices");
    Categories saved = categoryRepository.save(cat);

    Optional<Categories> found = categoryRepository.findById(saved.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Electronics");
  }

  @Test
  void whenFindAll_thenReturnNonEmptyList() {
    Categories c1 = new Categories("Books", "Printed reads");
    categoryRepository.save(c1);

    var all = categoryRepository.findAll();
    assertThat(all).isNotEmpty();
  }
}
