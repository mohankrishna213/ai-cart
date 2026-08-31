package org.techm.samples.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }

   
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ex.getMessage());
    }

    
    @ExceptionHandler({
        DuplicateWishlistException.class,
        DuplicateReviewException.class,
        DuplicateUserException.class
    })
    public ResponseEntity<String> handleConflict(RuntimeException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ex.getMessage());
    }

    
    @ExceptionHandler({InvalidCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<String> handleAuthErrors(RuntimeException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(ex.getMessage());
    }

    
    @ExceptionHandler(InvalidPromotionalPriceException.class)
    public ResponseEntity<String> handleInvalidPromotionalPrice(InvalidPromotionalPriceException ex) {
        log.warn("Invalid promotional price: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ex.getMessage());
    }

    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest req) {
        log.error("Unhandled exception on {}: {}", req.getDescription(false), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body("Internal Server Error: " + ex.getMessage());
    }
}
