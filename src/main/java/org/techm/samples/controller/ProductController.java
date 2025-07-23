package org.techm.samples.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.techm.samples.entity.Products;
import org.techm.samples.service.products.ProductService;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Public: List all products with pagination and sorting
    @GetMapping
    public String getAllProducts(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size,
                                 @RequestParam(defaultValue = "name") String sortBy,
                                 @RequestParam(defaultValue = "asc") String sortDir,
                                 Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Products> products = productService.getAllProducts(pageable);

        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "products/list";
    }

    // Public: View product details
    @GetMapping("/{id}")
    public String getProductById(@PathVariable Long id, Model model) {
        Products product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "products/detail";
    }

    // Public: Search products by name
    @GetMapping("/search")
    public String searchProducts(@RequestParam String q,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size,
                                 Model model) {

        Pageable pageable = PageRequest.of(page, size);
        List<Products> products = productService.searchProductsByName(q);

        model.addAttribute("products", products);
        model.addAttribute("searchTerm", q);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 1); // assuming no pagination in search
        model.addAttribute("totalItems", products.size());

        return "products/search";
    }

    // Public: Filter by category
    @GetMapping("/category/{categoryId}")
    public String getProductsByCategory(@PathVariable Long categoryId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "12") int size,
                                        Model model) {

        Pageable pageable = PageRequest.of(page, size);
        List<Products> products = productService.getProductsByCategory(categoryId);

        model.addAttribute("products", products);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 1); // assuming no pagination
        model.addAttribute("totalItems", products.size());

        return "products/category";
    }

    // Admin: View all products in admin panel
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adminProducts(Model model) {
        List<Products> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin/products/list";
    }

    // Admin: Show form to create new product
    @GetMapping("/admin/new")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Products());
        return "admin/products/form";
    }

    // Admin: Handle product creation
    @PostMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String createProduct(@ModelAttribute("product") Products product,
                                BindingResult result,
                                Model model) {

        if (result.hasErrors()) {
            return "admin/products/form";
        }

        try {
            productService.addProduct(product);
            return "redirect:/products/admin";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/products/form";
        }
    }

    // Admin: Show form to edit product
    @GetMapping("/admin/{id}/edit")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String editProductForm(@PathVariable Long id, Model model) {
        Products product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "admin/products/form";
    }

    // Admin: Handle product update
    @PostMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute("product") Products product,
                                BindingResult result,
                                Model model) {

        if (result.hasErrors()) {
            return "admin/products/form";
        }

        try {
            productService.updateProduct(product, id);
            return "redirect:/products/admin";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/products/form";
        }
    }

    // Admin: Delete product
    @PostMapping("/admin/{id}/delete")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products/admin";
    }
}
