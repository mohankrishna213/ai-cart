package org.techm.samples.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.techm.samples.entity.Products;

@Repository
public interface ProductsRepository extends JpaRepository<Products,Long>{
	List<Products> findByName(String name);
	List<Products> findByNameContaining(String keyword);
}
