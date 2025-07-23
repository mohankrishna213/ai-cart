package org.techm.samples.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.techm.samples.entity.Products;
import org.techm.samples.service.products.ProductService;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Home page for all users (guests, customers, admins) with pagination.
     * Admins see create/edit/delete options. Guests cannot add to wishlist.
     */
    @GetMapping({"/", "", "/list"})
    public String viewAllProducts(
            Model model,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Products> productPage = productService.getAllProducts(pageable);
        model.addAttribute("productPage", productPage);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        boolean isAdmin = authentication != null &&
                authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
        boolean isCustomer = authentication != null &&
                authentication.getAuthorities().contains(new SimpleGrantedAuthority("CUSTOMER"));
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated();
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isCustomer", isCustomer);
        model.addAttribute("isLoggedIn", isLoggedIn);
        return "product-list";
    }

    /**
     * Map root '/' to product list for all users (guests, customers, admins).
     */
    @GetMapping("/../")
    public String homeRedirect(Model model, Authentication authentication, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return viewAllProducts(model, authentication, page, size);
    }

    /**
     * Show form to add a new product (admin only).
     */
    @GetMapping("/add")
    public String showAddProductForm(Model model, Authentication authentication) {
        if (!isAdmin(authentication)) return "redirect:/products/";
        model.addAttribute("product", new Products());
        return "product-form";
    }

    /**
     * Handle product creation (admin only).
     */
    @PostMapping("/add")
    public String addProduct(@ModelAttribute Products product, Authentication authentication) {
        if (!isAdmin(authentication)) return "redirect:/products/";
        productService.addProduct(product);
        return "redirect:/products/";
    }

    /**
     * Show form to update a product (admin only).
     */
    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model, Authentication authentication) {
        if (!isAdmin(authentication)) return "redirect:/products/";
        Products product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product-form";
    }

    /**
     * Handle product update (admin only).
     */
    @PostMapping("/update/{id}")
    public String updateProduct(@ModelAttribute Products product, @PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) return "redirect:/products/";
        productService.updateProduct(product, id);
        return "redirect:/products/";
    }

    /**
     * Delete a product (admin only).
     */
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) return "redirect:/products/";
        productService.deleteProduct(id);
        return "redirect:/products/";
    }

    /**
     * View product details (accessible to all).
     */
    @GetMapping("/view/{id}")
    public String viewProductDetails(@PathVariable Long id, Model model) {
        Products product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product-details";
    }

    /**
     * Utility method to check if the user is an admin.
     */
    private boolean isAdmin(Authentication authentication) {
        return authentication != null &&
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
    }
}
