package org.techm.samples.exception;

/**
 * Exception thrown when promotional pricing validation fails.
 * This occurs when promotional price constraints are violated:
 * - Promotional price must be less than standard price
 * - Promotional price must not be negative
 */
public class InvalidPromotionalPriceException extends RuntimeException {
    
    public InvalidPromotionalPriceException(String message) {
        super(message);
    }
    
    public InvalidPromotionalPriceException(String message, Throwable cause) {
        super(message, cause);
    }
}
