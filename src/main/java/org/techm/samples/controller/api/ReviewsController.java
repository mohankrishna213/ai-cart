package org.techm.samples.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.Reviews;
import org.techm.samples.entity.User;
import org.techm.samples.service.reviews.ReviewsService;
import org.techm.samples.repository.UserInfoRepository;
import org.techm.samples.repository.ProductsRepository;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewsController {
    @Autowired
    private ReviewsService reviewsService;
    @Autowired
    private UserInfoRepository userInfoRepository;
    @Autowired
    private ProductsRepository productsRepository;

    // Get all reviews for a product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Reviews>> getReviewsByProduct(@PathVariable Long productId) {
        Optional<Products> productOpt = productsRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<Reviews> reviews = reviewsService.getReviewsByProduct(productOpt.get());
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    // Create a review by customer
    @PostMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> createReview(@PathVariable Long productId, @RequestBody Reviews review, Principal principal) {
        Optional<User> userOpt = userInfoRepository.findByUsername(principal.getName());
        Optional<Products> productOpt = productsRepository.findById(productId);
        if (userOpt.isEmpty() || productOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        review.setUser(userOpt.get());
        review.setProduct(productOpt.get());
        try {
            Reviews saved = reviewsService.addReview(review);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Delete a review by customer or admin
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CUSTOMER')")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId, Principal principal) {
        Optional<Reviews> reviewOpt = reviewsService.getReviewByIdEntity(reviewId);
        if (reviewOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Reviews review = reviewOpt.get();
        Optional<User> userOpt = userInfoRepository.findByUsername(principal.getName());
        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User user = userOpt.get();
        // Only admin or the review owner can delete
        if (user.getRole().name().equals("ADMIN") || review.getUser().getId().equals(user.getId())) {
            reviewsService.deleteReview(reviewId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
} 