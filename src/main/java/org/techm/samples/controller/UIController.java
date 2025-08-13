package org.techm.samples.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.techm.samples.controller.api.ProductsController;
import org.techm.samples.dto.WishlistItemsDTO;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.User;
import org.techm.samples.entity.Wishlist_items;
import org.techm.samples.exception.DuplicateWishlistException;
import org.techm.samples.exception.ResourceNotFoundException;
import org.techm.samples.repository.UserInfoRepository;
import org.techm.samples.service.auth.UserInfoDetails;
import org.techm.samples.service.categories.CategoriesService;
import org.techm.samples.service.products.ProductService;
import org.techm.samples.service.products.WishlistService;
import org.techm.samples.service.reviews.ReviewsService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("")
public class UIController {
    @Autowired
    private CategoriesService categoriesService;
    @Autowired
    private ProductService productService;
    @Autowired
    private WishlistService wishlistService;
    @Autowired
    private ReviewsService reviewsService;
    @Autowired
    private UserInfoRepository userInfoRepository;
    @Autowired
    private ProductsController apiController;

   
    @GetMapping({"/", "/home"})
    public String home(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "name") String sortBy,
                       @RequestParam(defaultValue = "asc") String sortDir,
                       @RequestParam(required = false) String filter,
                       @RequestParam(required = false) Double minPrice,
                       @RequestParam(required = false) Double maxPrice,
                       Model model,
                       HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String remoteUser = (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
        model.addAttribute("remoteUser", remoteUser);
        model.addAttribute("categories", categoriesService.getAllCategories());

        List<Products> products;

        if ("available".equalsIgnoreCase(filter)) {
            products = productService.getAvailableProducts();
        } else if ("top-rated".equalsIgnoreCase(filter)) {
            products = productService.getTopRatedProducts();
        } else if ("price-range".equalsIgnoreCase(filter) && minPrice != null && maxPrice != null) {
            products = productService.getProductsByPriceRange(minPrice, maxPrice);
        } else {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Products> productPage = productService.getAllProducts(pageable);
            model.addAttribute("products", productPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productPage.getTotalPages());
            model.addAttribute("size", size);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("filter", null);
            boolean isAdmin = false;
            if (auth != null && auth.isAuthenticated() && auth.getAuthorities() != null) {
                isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
            }
            model.addAttribute("isAdmin", Boolean.valueOf(isAdmin));
            return "home";
        }

        // For filtered results, no pagination
        model.addAttribute("products", products);
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1);
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("filter", filter);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        boolean isAdmin = false;
        if (auth != null && auth.isAuthenticated() && auth.getAuthorities() != null) {
            isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
        }
        model.addAttribute("isAdmin", Boolean.valueOf(isAdmin));

        return "home";
    }



   
    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "redirect:/auth/loginPage";
    }

    
    @GetMapping("/wishlist")
    public String wishlist(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = resolveEmail();
        User user = userInfoRepository.findByEmail(email).orElse(null);
        if (user != null) {
            model.addAttribute("wishlistItems", wishlistService.getWishlistByUser(user));
        }
        boolean isAdmin = false;
        if (auth != null && auth.isAuthenticated() && auth.getAuthorities() != null) {
            isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
        }
        model.addAttribute("isAdmin", isAdmin);
        return "wishlist";
    }

   
    @GetMapping("/category/{categoryId}")
    public String productsByCategory(@org.springframework.web.bind.annotation.PathVariable Long categoryId, Model model) {
        model.addAttribute("categories", categoriesService.getAllCategories());
        model.addAttribute("products", productService.getProductsByCategory(categoryId));
        model.addAttribute("categoryId", categoryId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = false;
        if (auth != null && auth.isAuthenticated() && auth.getAuthorities() != null) {
            isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
        }
        model.addAttribute("isAdmin", isAdmin);

        return "products/category";
    }

 
    @GetMapping("/product/{productId}")
    public String productDetail(@PathVariable Long productId, Model model) {
        
        // Fetch product and its reviews
        var product = productService.getProductById(productId);
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewsService.getReviewsByProduct(product));
        
        // Determine authenticated user’s email
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = null;
        
        if (auth != null && auth.isAuthenticated()
            && !(auth instanceof AnonymousAuthenticationToken)) {
            
            Object principal = auth.getPrincipal();
            if (principal instanceof UserInfoDetails) {
                email = ((UserInfoDetails) principal).getUsername();
            }
            else if (principal instanceof OidcUser) {
                email = ((OidcUser) principal).getAttribute("email");
            }
            else {
                email = auth.getName();
            }
        }
        
        User user = (email != null)
            ? userInfoRepository.findByEmail(email).orElse(null)
            : null;
        
        boolean inWishlist = false;
        if (user != null) {
            inWishlist = wishlistService
                .getWishlistByUser(user)
                .stream()
                .anyMatch(w -> w.getProduct().getId().equals(productId));
        }
        model.addAttribute("inWishlist", inWishlist);
        

        boolean isAdmin = false;
        if (auth != null && auth.isAuthenticated()) {
            isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        }
        model.addAttribute("isAdmin", isAdmin);
        

        if (user != null) {
            model.addAttribute("currentUserId", user.getId());
        }
        
        return "products/detail";
    }


    
    @GetMapping("/products")
    public String allProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products/list";
    }


    @GetMapping("/products/price-range")
    public String productsByPriceRange(@org.springframework.web.bind.annotation.RequestParam double minPrice,
                                       @org.springframework.web.bind.annotation.RequestParam double maxPrice,
                                       Model model) {
        model.addAttribute("products", productService.getProductsByPriceRange(minPrice, maxPrice));
        return "products/list";
    }

    
    @GetMapping("/products/top-rated")
    public String topRatedProducts(Model model) {
        model.addAttribute("products", productService.getTopRatedProducts());
        return "products/list";
    }

    
    @GetMapping("/products/search")
    public String searchProducts(@org.springframework.web.bind.annotation.RequestParam String q, Model model) {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	boolean isAdmin = false;
        if (auth != null && auth.isAuthenticated() && auth.getAuthorities() != null) {
            isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
        }
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("products", productService.searchProductsByName(q));
        return "products/search";
    }
    
    @PostMapping("products/wishlist/add/{productId}")
    public ResponseEntity<?> addProductToWishlist(@PathVariable Long productId, Model model) {
    	String email = resolveEmail();
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2) Lookup user and product
        User user = userInfoRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        Products product = productService.getProductByIdOptional(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        // 3) Add to wishlist
        try {
            Wishlist_items item = wishlistService.addToWishlist(user, product);

            // 4) Map to DTO
            WishlistItemsDTO dto = new WishlistItemsDTO();
            dto.setId(item.getId());
            dto.setUserId(user.getId());
            dto.setProductId(product.getId());
            dto.setProductName(product.getName());
            dto.setProductImage(product.getImageUrl());

            return ResponseEntity.ok(dto);
        }
        catch (DuplicateWishlistException ex) {
            // Already in wishlist
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED)
                                 .body(ex.getMessage());
        }
    }

    private String resolveEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
            || !auth.isAuthenticated()
            || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserInfoDetails) {
            return ((UserInfoDetails) principal).getUsername();
        }
        if (principal instanceof OidcUser) {
            return ((OidcUser) principal).getAttribute("email");
        }
        if (principal instanceof OAuth2User) {
            return ((OAuth2User) principal).getAttribute("email");
        }
        // fallback to name()
        return auth.getName();
    }
    
    @PostMapping("/wishlist/remove/{productId}")
    public String removeFromWishlistUI(
        @PathVariable Long productId,
        Principal principal
    ) {
        
        apiController.removeProductFromWishlist(productId, principal);
        return "redirect:/wishlist";
    }
    
    @PostMapping("products/{productId}/reviews/{reviewId}/remove")
    public String removeReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserInfoDetails userDetails,
            RedirectAttributes redirectAttrs) {


        reviewsService.deleteReview(reviewId);

        return "redirect:/product/" + productId;
    }
   
    @PostMapping("/admin/category/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addCategory(@org.springframework.web.bind.annotation.RequestBody org.techm.samples.entity.Categories category) {
        var saved = categoriesService.addCategory(category);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/admin/category/edit")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> editCategory(@org.springframework.web.bind.annotation.RequestBody org.techm.samples.entity.Categories category) {
        var updated = categoriesService.updateCategory(category, category.getId());
        return ResponseEntity.ok(updated);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/admin/category/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoriesService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    
    @PostMapping("/admin/product/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addProduct(@org.springframework.web.bind.annotation.RequestBody org.techm.samples.dto.ProductsDTO dto) {
        if (dto.getCategoryId() == null) {
            return ResponseEntity.badRequest().body("Category ID is required");
        }
        var savedDto = categoriesService.addProductToCategory(dto.getCategoryId(), dto);
        return ResponseEntity.ok(savedDto);
    }

    @PostMapping("/admin/product/edit")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> editProduct(@org.springframework.web.bind.annotation.RequestBody org.techm.samples.dto.ProductsDTO dto) {
        if (dto.getCategoryId() == null || dto.getId() == null) {
            return ResponseEntity.badRequest().body("Category ID and Product ID are required");
        }
        var updatedDto = categoriesService.updateProductInCategory(dto.getCategoryId(), dto);
        return ResponseEntity.ok(updatedDto);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/admin/product/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}
