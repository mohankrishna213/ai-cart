package org.techm.samples.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.techm.samples.entity.User;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.Wishlist_items;
import org.techm.samples.entity.Role;

@DataJpaTest
@EntityScan("org.techm.samples.entity")
@EnableJpaRepositories("org.techm.samples.repository")
// optional if you want to override the default embedded test DB
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class WishlistItemsRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private WishlistItemsRepository repository;

    @Test
    void whenSave_thenFindByUser() {
        // 1) create & persist a user
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword("secret");
        user.setRole(Role.CUSTOMER);
        em.persistAndFlush(user);

        // 2) create & persist a product
        Products product = new Products();
        product.setName("Widget");
        product.setDescription("A test widget");
        product.setPrice(9.99);
        em.persistAndFlush(product);

        // 3) create & persist a wishlist item
        Wishlist_items wish = new Wishlist_items();
        wish.setUser(user);
        wish.setProduct(product);
        em.persistAndFlush(wish);

        // 4a) query by user
        List<Wishlist_items> byUser = repository.findByUser(user);
        assertThat(byUser)
            .hasSize(1)
            .first()
            .isEqualTo(wish);

        // 4b) query by user.id
        List<Wishlist_items> byUserId = repository.findByUser_Id(user.getId());
        assertThat(byUserId)
            .hasSize(1)
            .first()
            .isEqualTo(wish);
    }
}
