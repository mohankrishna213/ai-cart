package org.techm.samples.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.techm.samples.entity.Wishlist_items;
import org.techm.samples.entity.User;
import org.techm.samples.entity.Products;
import java.util.List;
import java.util.Optional;

public interface WishlistItemsRepository extends JpaRepository<Wishlist_items, Long> {
    List<Wishlist_items> findByUser(User user);
    Optional<Wishlist_items> findByUserAndProduct(User user, Products product);
    void deleteByUserAndProduct(User user, Products product);
	List<Wishlist_items> findByUser_Id(Long id);
}
