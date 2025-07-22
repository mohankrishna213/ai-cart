package org.techm.samples.service.products;

import java.util.List;

import org.techm.samples.entity.Products;

public interface ProductService {

	Products addProduct(Products product);
	Products updateProduct(Products product,Long id);
	Products getProductById(Long id);
	
	List<Products> getAllProducts();
	List<Products> getProductsContaining(String keyword);
	List<Products> getProductsByCategory(Long categoryId);
	List<Products> getAvailableProducts();
	List<Products> searchProductsByName(String keyword);
	List<Products> getProductsByPriceRange(double minPrice, double maxPrice);
	List<Products> getTopRatedProducts();

	void deleteProduct(Long id);
}
