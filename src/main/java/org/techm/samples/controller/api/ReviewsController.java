package org.techm.samples.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.Reviews;
import org.techm.samples.entity.User;
import org.techm.samples.service.reviews.ReviewsService;
import org.techm.samples.repository.UserInfoRepository;
import org.techm.samples.repository.ProductsRepository;
import org.techm.samples.dto.ReviewsDTO;
import org.techm.samples.exception.ResourceNotFoundException;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
    public ResponseEntity<List<ReviewsDTO>> getReviewsByProduct(@PathVariable Long productId) {
        Optional<Products> productOpt = productsRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        List<Reviews> reviews = reviewsService.getReviewsByProduct(productOpt.get());
        List<ReviewsDTO> dtoList = reviews.stream().map(this::toReviewsDTO).toList();
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    // Create a review by customer
    @PostMapping(path="/product/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> createReview(@PathVariable Long productId, @RequestBody ReviewsDTO reviewDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<User> userOpt = userInfoRepository.findByEmail(email);
        Optional<Products> productOpt = productsRepository.findById(productId);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found: " + email);
        }
        if (productOpt.isEmpty()) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        Reviews review = new Reviews();
        review.setUser(userOpt.get());
        review.setProduct(productOpt.get());
        review.setRating(reviewDTO.getRating());
        review.setTitle(reviewDTO.getTitle());
        review.setContent(reviewDTO.getContent());
        try {
            Reviews saved = reviewsService.addReview(review);
            ReviewsDTO responseDTO = toReviewsDTO(saved);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
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
            throw new ResourceNotFoundException("Review not found with id: " + reviewId);
        }
        Reviews review = reviewOpt.get();
        Optional<User> userOpt = userInfoRepository.findByEmail(principal.getName());
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

    private ReviewsDTO toReviewsDTO(Reviews review) {
        ReviewsDTO dto = new ReviewsDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setTitle(review.getTitle());
        dto.setContent(review.getContent());
        dto.setUserId(review.getUser().getId());
        dto.setProductId(review.getProduct().getId());
        return dto;
    }
}