package org.techm.samples.service.products;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.techm.samples.entity.Products;
import org.techm.samples.exception.InvalidPromotionalPriceException;
import org.techm.samples.exception.ResourceNotFoundException;
import org.techm.samples.repository.ProductsRepository;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductsRepository productRepo;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "searchResults", allEntries = true)
    })
    public Products addProduct(Products product) {
        return productRepo.save(product);
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public Products getProductById(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    @Cacheable(value = "products", key = "'all_pageable_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort")
    public Page<Products> getAllProducts(Pageable pageable) {
        return productRepo.findAll(pageable);
    }

    @Override
    @Cacheable(value = "products", key = "'all_products'")
    public List<Products> getAllProducts() {
        return productRepo.findAll();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "searchResults", allEntries = true)
    })
    public void deleteProduct(Long id) {
        Products prod = getProductById(id);
        productRepo.delete(prod);
    }

    @Override
    @Caching(
            put = @CachePut(value = "products", key = "#id"),
            evict = {
                    @CacheEvict(value = "products", allEntries = true),
                    @CacheEvict(value = "searchResults", allEntries = true)
            }
    )
    public Products updateProduct(Products product, Long id) {
        Products existing = getProductById(id);
        
        // Validate promotional pricing if it's being updated
        if (product.getPromotionalPrice() != null || product.isPromoActive()) {
            validatePromotionalPrice(product.getPrice(), product.getPromotionalPrice(), product.isPromoActive());
            existing.setPromotionalPrice(product.getPromotionalPrice());
            existing.setPromoActive(product.isPromoActive());
        }
        
        existing.setAvailable(product.isAvailable());
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setImageUrl(product.getImageUrl());
        existing.setPrice(product.getPrice());
        existing.setStockQuantity(product.getStockQuantity());
        existing.setCategory(product.getCategory());
        return productRepo.save(existing);
    }
    
    /**
     * Validates promotional price constraints.
     * Rules:
     * - If isPromoActive is false, promotional price is not validated
     * - If isPromoActive is true, promotionalPrice must be less than standard price
     * - Promotional price must not be negative
     * 
     * @param standardPrice the product's standard price
     * @param promotionalPrice the promotional price to validate
     * @param isPromoActive whether the promotional price is active
     * @throws InvalidPromotionalPriceException if validation fails
     */
    private void validatePromotionalPrice(double standardPrice, Double promotionalPrice, boolean isPromoActive) throws InvalidPromotionalPriceException {
        // If promo is not active, don't validate the price value
        if (!isPromoActive) {
            return;
        }
        
        // If promo is active, price must be validated
        if (promotionalPrice == null) {
            throw new InvalidPromotionalPriceException("Promotional price cannot be null when promotional pricing is active");
        }
        
        if (promotionalPrice < 0) {
            throw new InvalidPromotionalPriceException("Promotional price must not be negative");
        }
        
        if (promotionalPrice >= standardPrice) {
            throw new InvalidPromotionalPriceException("Promotional price must be less than the standard price");
        }
    }

    @Override
    @Cacheable(value = "searchResults", key = "'containing_' + #keyword")
    public List<Products> getProductsContaining(String keyword) {
        return productRepo.findByNameContaining(keyword);
    }

    @Override
    @Cacheable(value = "products", key = "'category_' + #categoryId")
    public List<Products> getProductsByCategory(Long categoryId) {
        return productRepo.findByCategoryId(categoryId);
    }

    @Override
    @Cacheable(value = "products", key = "'available_products'")
    public List<Products> getAvailableProducts() {
        return productRepo.findByAvailableTrue();
    }

    @Override
    @Cacheable(value = "searchResults", key = "'search_' + #keyword")
    public List<Products> searchProductsByName(String keyword) {
        return productRepo.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    @Cacheable(value = "searchResults", key = "'price_range_' + #minPrice + '_' + #maxPrice")
    public List<Products> getProductsByPriceRange(double minPrice, double maxPrice) {
        return productRepo.findByPriceBetween(minPrice, maxPrice);
    }

    @Override
    @Cacheable(value = "products", key = "'top_rated'")
    public List<Products> getTopRatedProducts() {
        return productRepo.findTopRatedProducts();
    }

    @Override
    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public Optional<Products> getProductByIdOptional(Long id) {
        return productRepo.findById(id);
    }
}
