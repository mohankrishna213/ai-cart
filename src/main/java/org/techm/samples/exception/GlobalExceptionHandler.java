package org.techm.samples.exception;

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

    // 400 – Validation failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    // 404 – Resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ex.getMessage());
    }

    // 409 – Duplicate operations
    @ExceptionHandler({
        DuplicateWishlistException.class,
        DuplicateReviewException.class,
        DuplicateUserException.class
    })
    public ResponseEntity<String> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ex.getMessage());
    }

    // 401 – Bad credentials (UI or API can catch this)
    @ExceptionHandler({InvalidCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<String> handleAuthErrors(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(ex.getMessage());
    }

    // 500 – Fallback for all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body("Internal Server Error: " + ex.getMessage());
    }
}
