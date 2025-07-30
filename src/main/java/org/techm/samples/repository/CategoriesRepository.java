package org.techm.samples.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.techm.samples.entity.Categories;

public interface CategoriesRepository extends JpaRepository<Categories, Long>{
	List<Categories> getCategoryByName(String name);

	boolean existsByName(String name);

	Optional<Categories> findByName(String name);

}
