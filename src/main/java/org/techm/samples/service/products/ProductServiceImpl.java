package org.techm.samples.service.products;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.techm.samples.entity.Products;
import org.techm.samples.exception.ResourceNotFoundException;
import org.techm.samples.repository.ProductsRepository;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductsRepository productRepo;

    @Override
    public Products addProduct(Products product) {
        return productRepo.save(product);
    }

    @Override
    public Products getProductById(Long id) {
        return productRepo.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    public Page<Products> getAllProducts(Pageable pageable) {
        return productRepo.findAll(pageable);
    }

    @Override
    public List<Products> getAllProducts() {
        return productRepo.findAll();
    }

    @Override
    public void deleteProduct(Long id) {
        Products prod = getProductById(id);
        productRepo.delete(prod);
    }

    @Override
    public Products updateProduct(Products product, Long id) {
        Products existing = getProductById(id);
        existing.setAvailable(product.isAvailable());
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setImageUrl(product.getImageUrl());
        existing.setPrice(product.getPrice());
        existing.setStockQuantity(product.getStockQuantity());
        existing.setCategory(product.getCategory());
        return productRepo.save(existing);
    }

    @Override
    public List<Products> getProductsContaining(String keyword) {
        return productRepo.findByNameContaining(keyword);
    }

    @Override
    public List<Products> getProductsByCategory(Long categoryId) {
        return productRepo.findByCategoryId(categoryId);
    }

    @Override
    public List<Products> getAvailableProducts() {
        return productRepo.findByAvailableTrue();
    }

    @Override
    public List<Products> searchProductsByName(String keyword) {
        return productRepo.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public List<Products> getProductsByPriceRange(double minPrice, double maxPrice) {
        return productRepo.findByPriceBetween(minPrice, maxPrice);
    }

    @Override
    public List<Products> getTopRatedProducts() {
        return productRepo.findTopRatedProducts();
    }

	@Override
	public Optional<Products> getProductByIdOptional(Long id) {
		return productRepo.findById(id);
	}
}
