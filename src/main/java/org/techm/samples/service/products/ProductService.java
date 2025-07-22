package org.techm.samples.service.products;

import java.util.List;

import org.techm.samples.entity.Products;

public interface ProductService {

	Products addProduct(Products product);
	
	Products getProductById(Long id);
	
	List<Products> getAll();
	
	List<Products> getProductByName(String name);
	
	void deleteProduct(Long id);
	
	Products updateProduct(Products product,Long id);
	
	List<Products> getProductsContaining(String keyword);
}
