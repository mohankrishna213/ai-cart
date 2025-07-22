package org.techm.samples.service.categories;

import java.util.List;

import org.techm.samples.entity.Categories;

public interface CategoriesService {
	Categories addCategory(Categories category);
	
	List<Categories> getAllCategories();
	
	
	
	List<Categories> getCategoryByName(String name);
	
	void deleteCategory(Long id);
	
	Categories updateCategory(Categories category, Long id);

	Categories getCategoryById(Long id);
	

}
