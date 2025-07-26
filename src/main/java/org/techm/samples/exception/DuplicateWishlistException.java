package org.techm.samples.exception;

public class DuplicateWishlistException extends RuntimeException {
    public DuplicateWishlistException(Long productId) {
        super("Product with ID " + productId + " is already in your wishlist.");
    }
}
