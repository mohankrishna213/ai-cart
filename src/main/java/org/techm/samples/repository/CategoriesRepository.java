package org.techm.samples.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.techm.samples.entity.Categories;

public interface CategoriesRepository extends JpaRepository<Categories, Long>{
	List<Categories> getCategoryByName(String name);

}
