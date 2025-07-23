package org.techm.samples.service.products;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.techm.samples.entity.Products;

public interface ProductService {

	Products addProduct(Products product);
	Products updateProduct(Products product,Long id);
	Products getProductById(Long id);
	
	List<Products> getAllProducts();
	Page<Products> getAllProducts(Pageable pageable);
	List<Products> getProductsContaining(String keyword);
	List<Products> getProductsByCategory(Long categoryId);
	List<Products> getAvailableProducts();
	List<Products> searchProductsByName(String keyword);
	List<Products> getProductsByPriceRange(double minPrice, double maxPrice);
	List<Products> getTopRatedProducts();

	void deleteProduct(Long id);
}
