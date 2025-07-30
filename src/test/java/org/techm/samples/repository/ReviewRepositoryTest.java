package org.techm.samples.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.Reviews;
import org.techm.samples.entity.Role;
import org.techm.samples.entity.User;

@DataJpaTest
@EntityScan("org.techm.samples.entity")
@EnableJpaRepositories("org.techm.samples.repository")

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class ReviewRepositoryTest{

	@Autowired
    private TestEntityManager em;
	
  @Autowired
  private ReviewsRepository reviewRepository;

  @Test
  void whenSave_thenFindById() {
	  
	  User user = new User();
      user.setUsername("alice");
      user.setEmail("alice@example.com");
      user.setPassword("secret");
      user.setRole(Role.CUSTOMER);
      em.persistAndFlush(user);

     
      Products product = new Products();
      product.setName("Widget");
      product.setDescription("A test widget");
      product.setPrice(9.99);
      em.persistAndFlush(product);
      
    Reviews r = new Reviews();
    r.setProduct(product);
    r.setUser(user);
    r.setRating(5);
    r.setContent("Excellent!");
    Reviews saved = reviewRepository.save(r);

    Optional<Reviews> found = reviewRepository.findById(saved.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getRating()).isEqualTo(5);
  }
}
