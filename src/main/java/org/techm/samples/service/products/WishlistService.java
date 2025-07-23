package org.techm.samples.service.products;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.User;
import org.techm.samples.entity.Wishlist_items;
import org.techm.samples.repository.WishlistItemsRepository;

import jakarta.transaction.Transactional;

@Service
public class WishlistService {
    @Autowired
    private WishlistItemsRepository wishlistItemsRepository;

    public Wishlist_items addToWishlist(User user, Products product) {
        Optional<Wishlist_items> existing = wishlistItemsRepository.findByUserAndProduct(user, product);
        if (existing.isPresent()) {
            return existing.get(); // Already in wishlist
        }
        Wishlist_items item = new Wishlist_items(user, product);
        return wishlistItemsRepository.save(item);
    }
    
    @Transactional
    public void removeFromWishlist(User user, Products product) {
        wishlistItemsRepository.deleteByUserAndProduct(user, product);
    }

    public List<Wishlist_items> getWishlistByUser(User user) {
        return wishlistItemsRepository.findByUser(user);
    }
}
