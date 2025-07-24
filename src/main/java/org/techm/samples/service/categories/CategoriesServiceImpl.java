package org.techm.samples.service.categories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.techm.samples.dto.ProductsDTO;
import org.techm.samples.entity.Categories;
import org.techm.samples.entity.Products;
import org.techm.samples.exception.ResourceNotFoundException;
import org.techm.samples.repository.CategoriesRepository;
import org.techm.samples.repository.ProductsRepository;

@Service
public class CategoriesServiceImpl implements CategoriesService{
	
	@Autowired
	private CategoriesRepository categoryRepo;
	
	@Autowired
	private ProductsRepository productsRepository;
	
	@Override
	public Categories addCategory(Categories category) {
		categoryRepo.save(category);
		return category;
		
	}

	@Override
	public List<Categories> getAllCategories() {
		List<Categories> categories = categoryRepo.findAll();
		return categories;
	}

	@Override
	public Categories getCategoryById(Long id) {
		Categories category = categoryRepo.findById(id).orElse(null);
		return category;
	}

	@Override
	public List<Categories> getCategoryByName(String name) {
		List<Categories> categories = categoryRepo.getCategoryByName(name);
		return categories;
	}

	@Override
	public void deleteCategory(Long id) {
		categoryRepo.deleteById(id);
		
	}

	@Override
	public Categories updateCategory(Categories category, Long id) {
		Categories updatedCategory = categoryRepo.findById(id).orElse(category);
		updatedCategory.setName(category.getName());
		updatedCategory.setDescription(category.getDescription());
		return categoryRepo.save(updatedCategory);
	}

	@Override
	public ProductsDTO addProductToCategory(Long categoryId, ProductsDTO dto) {
	    Categories category = categoryRepo.findById(categoryId)
	        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

	    Products product = new Products();
	    product.setName(dto.getName());
	    product.setDescription(dto.getDescription());
	    product.setPrice(dto.getPrice());
	    product.setStockQuantity(dto.getStockQuantity());
	    product.setImageUrl(dto.getImageUrl());
	    product.setAvailable(dto.isAvailable());
	    product.setCategory(category); // Link to category

	    Products saved = productsRepository.save(product);

	    // Convert back to DTO
	    ProductsDTO result = new ProductsDTO();
	    result.setId(saved.getId());
	    result.setName(saved.getName());
	    result.setDescription(saved.getDescription());
	    result.setPrice(saved.getPrice());
	    result.setStockQuantity(saved.getStockQuantity());
	    result.setImageUrl(saved.getImageUrl());
	    result.setAvailable(saved.isAvailable());
	    result.setCategoryId(categoryId);

	    return result;
	}

}
