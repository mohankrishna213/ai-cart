package org.techm.samples.service.categories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.techm.samples.entity.Categories;
import org.techm.samples.repository.CategoriesRepository;

@Service
public class CategoriesServiceImpl implements CategoriesService{
	
	@Autowired
	private CategoriesRepository categoryRepo;
	
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
	

}
