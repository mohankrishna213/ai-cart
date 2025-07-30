package org.techm.samples.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.techm.samples.entity.Categories;
import org.techm.samples.entity.Products;
import org.techm.samples.service.categories.CategoriesService;
import org.techm.samples.service.products.ProductService;
import org.techm.samples.service.reviews.ReviewsService;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminViewController {
    @Autowired
    private CategoriesService categoriesService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ReviewsService reviewsService;

   
    @PostMapping("/category")
    public ResponseEntity<?> addCategory(@RequestBody Categories category) {
        var saved = categoriesService.addCategory(category);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/category/{id}")
    public ResponseEntity<?> editCategory(@PathVariable Long id, @RequestBody Categories category) {
        category.setId(id);
        var updated = categoriesService.updateCategory(category, id);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/category/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoriesService.deleteCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

   
    @PutMapping("/product/{id}")
    public ResponseEntity<?> editProduct(@PathVariable Long id, @RequestBody Products product) {
        product.setId(id);
        var updated = productService.updateProduct(product, id);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    
    @DeleteMapping("/review/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        reviewsService.deleteReview(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
