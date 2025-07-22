package org.techm.samples.service.products;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.techm.samples.entity.Products;
import org.techm.samples.repository.ProductsRepository;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductsRepository productRepo;

	@Override
	public Products addProduct(Products product) {
		productRepo.save(product);
		return product;
		
	}

	@Override
	public Products getProductById(Long id) {
		Products product=productRepo.findById(id).orElse(null);
		return product;
	}

	@Override
	public List<Products> getAll() {
		List<Products> products=productRepo.findAll();
		return products;
	}

	@Override
	public List<Products> getProductByName(String name) {
		List<Products> products=productRepo.findByName(name);
		return products;
	}

	@Override
	public void deleteProduct(Long id) {
		productRepo.deleteById(id);
		
	}

	@Override
	public Products updateProduct(Products product,Long id) {
		Products updatedProduct=productRepo.findById(id).orElse(product);
		updatedProduct.setAvailable(product.isAvailable());
		updatedProduct.setName(product.getName());
		updatedProduct.setDescription(product.getDescription());
		updatedProduct.setImageUrl(product.getImageUrl());
		updatedProduct.setPrice(product.getPrice());
		updatedProduct.setStockQuantity(product.getStockQuantity());
		updatedProduct.setCategory(product.getCategory());
		return updatedProduct;
		
	}
	
	@Override
	public List<Products> getProductsContaining(String keyword) {
		return productRepo.findByNameContaining(keyword);
	}
	
	
}
