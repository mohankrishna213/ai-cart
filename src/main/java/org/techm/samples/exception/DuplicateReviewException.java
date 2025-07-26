package org.techm.samples.exception;

public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException(Long productId) {
        super("You have already reviewed product with ID " + productId + ".");
    }
}
