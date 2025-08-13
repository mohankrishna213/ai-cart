package org.techm.samples.controller.api;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.techm.samples.dto.ReviewsDTO;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.Reviews;
import org.techm.samples.entity.User;
import org.techm.samples.exception.ResourceNotFoundException;
import org.techm.samples.repository.ProductsRepository;
import org.techm.samples.repository.UserInfoRepository;
import org.techm.samples.service.auth.UserInfoDetails;
import org.techm.samples.service.reviews.ReviewsService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewsController {
    @Autowired
    private ReviewsService reviewsService;
    @Autowired
    private UserInfoRepository userInfoRepository;
    @Autowired
    private ProductsRepository productsRepository;

   
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

    
    @PostMapping(path="/product/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> createReview(@PathVariable Long productId, @RequestBody ReviewsDTO reviewDTO) {
    	// 1) Resolve email
    	System.out.println("its here");
        String email = resolveEmail();
        System.out.println(">>> [DEBUG] Resolved email = " + email);
        if (email == null) {
        	System.out.println("here");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2) Lookup user & product
        User user = userInfoRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        Products product = productsRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        // 3) Build and persist review
        Reviews review = new Reviews();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(reviewDTO.getRating());
        review.setTitle(reviewDTO.getTitle());
        review.setContent(reviewDTO.getContent());

        try {
            Reviews saved = reviewsService.addReview(review);
            ReviewsDTO responseDTO = toReviewsDTO(saved);
            System.out.println("saved");
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (IllegalStateException ex) {
            // duplicate‐review case
        	System.out.println(ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    private String resolveEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() 
            || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = auth.getPrincipal();

        // Form-login wrapper
        if (principal instanceof UserInfoDetails) {
            return ((UserInfoDetails) principal).getUsername();
        }

        // OIDC login -> yields an OidcUser
        if (principal instanceof OidcUser) {
            return ((OidcUser) principal).getEmail();      // direct getter
        }

        // OAuth2 login without OIDC -> still implements OAuth2User
        if (principal instanceof OAuth2User) {
            return ((OAuth2User) principal).getAttribute("email");
        }

        // Fallback to name()
        return auth.getName();
    }


   
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