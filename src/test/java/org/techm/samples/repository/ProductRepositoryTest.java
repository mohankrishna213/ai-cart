package org.techm.samples.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.techm.samples.entity.Products;

@SpringBootTest
@AutoConfigureTestDatabase
public class ProductRepositoryTest {

  @Autowired
  private ProductsRepository productRepository;

  @Test
  void whenSave_thenFindAll() {
    Products p = new Products();
    p.setName("Laptop");
    p.setDescription("Gaming laptop");
    p.setPrice(1500.00);
    productRepository.save(p);

    List<Products> list = productRepository.findAll();
    assertThat(list).hasSizeGreaterThanOrEqualTo(1);
  }
}
