package org.techm.samples.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.User;
import org.techm.samples.repository.UserInfoRepository;
import org.techm.samples.service.categories.CategoriesService;
import org.techm.samples.service.products.ProductService;
import org.techm.samples.service.products.WishlistService;
import org.techm.samples.service.reviews.ReviewsService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("")
public class CustomerViewController {
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

    // Home page after login
    @GetMapping({"/", "/home"})
    public String home(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
                       Model model,
                       HttpServletRequest request) {
        model.addAttribute("remoteUser", request.getRemoteUser());
        model.addAttribute("categories", categoriesService.getAllCategories());
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Products> productPage = productService.getAllProducts(pageable);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("remoteUser", request.getRemoteUser());

        return "home";
    }



    // Logout (Spring Security will handle actual logout, this is just a redirect)
    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "redirect:/auth/loginPage";
    }

    // Show wishlist page for customer
    @GetMapping("/wishlist")
    public String wishlist(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userInfoRepository.findByEmail(email).orElse(null);
        if (user != null) {
            model.addAttribute("wishlistItems", wishlistService.getWishlistByUser(user));
        }
        return "wishlist";
    }

    // Show products by category
    @GetMapping("/category/{categoryId}")
    public String productsByCategory(@org.springframework.web.bind.annotation.PathVariable Long categoryId, Model model) {
        model.addAttribute("categories", categoriesService.getAllCategories());
        model.addAttribute("products", productService.getProductsByCategory(categoryId));
        return "products/category";
    }

    // Show product details page
    @GetMapping("/product/{productId}")
    public String productDetail(@org.springframework.web.bind.annotation.PathVariable Long productId, Model model) {
        var product = productService.getProductById(productId);
        model.addAttribute("product", product);
        // Add reviews for this product
        model.addAttribute("reviews", reviewsService.getReviewsByProduct(product));
        // Add wishlist status for this user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userInfoRepository.findByEmail(email).orElse(null);
        boolean inWishlist = false;
        if (user != null) {
            inWishlist = wishlistService.getWishlistByUser(user).stream().anyMatch(w -> w.getProduct().getId().equals(product.getId()));
        }
        model.addAttribute("inWishlist", inWishlist);
        return "products/detail";
    }

    // Show all products (with filters)
    @GetMapping("/products")
    public String allProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products/list";
    }

    // Filter products by price range
    @GetMapping("/products/price-range")
    public String productsByPriceRange(@org.springframework.web.bind.annotation.RequestParam double minPrice,
                                       @org.springframework.web.bind.annotation.RequestParam double maxPrice,
                                       Model model) {
        model.addAttribute("products", productService.getProductsByPriceRange(minPrice, maxPrice));
        return "products/list";
    }

    // Filter top rated products
    @GetMapping("/products/top-rated")
    public String topRatedProducts(Model model) {
        model.addAttribute("products", productService.getTopRatedProducts());
        return "products/list";
    }

    // Search products by name
    @GetMapping("/products/search")
    public String searchProducts(@org.springframework.web.bind.annotation.RequestParam String q, Model model) {
        model.addAttribute("products", productService.searchProductsByName(q));
        return "products/search";
    }
}
