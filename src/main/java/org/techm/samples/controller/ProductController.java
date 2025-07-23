package org.techm.samples.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.techm.samples.entity.Products;
import org.techm.samples.service.products.ProductService;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Home page for all authenticated users.
     * Admins see create/edit/delete options.
     */
    @GetMapping("/")
    public String viewAllProducts(Model model, Authentication authentication) {
        List<Products> products = productService.getAllProducts();
        model.addAttribute("products", products);

        boolean isAdmin = authentication != null &&
                          authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        return "product-list";
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
