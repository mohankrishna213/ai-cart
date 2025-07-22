package org.techm.samples.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.techm.samples.entity.Products;

public interface ProductsRepository extends JpaRepository<Products,Long>{
	List<Products> getProductByName(String name);
}
