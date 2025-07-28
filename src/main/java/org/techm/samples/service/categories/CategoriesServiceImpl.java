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
public class CategoriesServiceImpl implements CategoriesService {
    @Override
    public ProductsDTO updateProductInCategory(Long categoryId, ProductsDTO dto) {
        Products product = productsRepository.findById(dto.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getId()));
        Categories category = getCategoryById(categoryId);
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setImageUrl(dto.getImageUrl());
        product.setAvailable(dto.isAvailable());
        product.setCategory(category);
        Products saved = productsRepository.save(product);
        dto.setId(saved.getId());
        dto.setCategoryId(categoryId);
        return dto;
    }

    @Autowired
    private CategoriesRepository categoryRepo;

    @Autowired
    private ProductsRepository productsRepository;

    @Override
    public Categories addCategory(Categories category) {
        return categoryRepo.save(category);
    }

    @Override
    public List<Categories> getAllCategories() {
        return categoryRepo.findAll();
    }

    @Override
    public Categories getCategoryById(Long id) {
        return categoryRepo.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Override
    public List<Categories> getCategoryByName(String name) {
        return categoryRepo.getCategoryByName(name);
    }

    @Override
    public void deleteCategory(Long id) {
        Categories cat = getCategoryById(id);
        categoryRepo.deleteById(id);
    }

    @Override
    public Categories updateCategory(Categories category, Long id) {
        Categories existing = getCategoryById(id);
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        return categoryRepo.save(existing);
    }

    @Override
    public ProductsDTO addProductToCategory(Long categoryId, ProductsDTO dto) {
        Categories category = getCategoryById(categoryId);

        Products product = new Products();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setImageUrl(dto.getImageUrl());
        product.setAvailable(dto.isAvailable());
        product.setCategory(category);

        Products saved = productsRepository.save(product);

        dto.setId(saved.getId());
        dto.setCategoryId(categoryId);
        return dto;
    }
}
