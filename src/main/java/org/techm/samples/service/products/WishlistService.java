package org.techm.samples.service.products;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.User;
import org.techm.samples.entity.Wishlist_items;
import org.techm.samples.exception.DuplicateWishlistException;
import org.techm.samples.exception.ResourceNotFoundException;
import org.techm.samples.repository.WishlistItemsRepository;

@Service
public class WishlistService {

    @Autowired
    private WishlistItemsRepository wishlistItemsRepository;

    public Wishlist_items addToWishlist(User user, Products product) {
        Optional<Wishlist_items> existing =
            wishlistItemsRepository.findByUserAndProduct(user, product);

        if (existing.isPresent()) {
            throw new DuplicateWishlistException(product.getId());
        }
        return wishlistItemsRepository.save(new Wishlist_items(user, product));
    }

    public void removeFromWishlist(User user, Products product) {
        Optional<Wishlist_items> existing =
            wishlistItemsRepository.findByUserAndProduct(user, product);

        if (existing.isEmpty()) {
            throw new ResourceNotFoundException(
                "Wishlist item not found for user " + user.getId());
        }
        wishlistItemsRepository.delete(existing.get());
    }

    public List<Wishlist_items> getWishlistByUser(User user) {
        return wishlistItemsRepository.findByUser(user);
    }
}
