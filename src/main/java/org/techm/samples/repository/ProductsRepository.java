package org.techm.samples.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.techm.samples.entity.Products;

@Repository
public interface ProductsRepository extends JpaRepository<Products,Long>{
	List<Products> findByNameContaining(String keyword);
	List<Products> findByCategoryId(Long categoryId);
    List<Products> findByAvailableTrue();
    List<Products> findByNameContainingIgnoreCase(String keyword);
    List<Products> findByPriceBetween(double minPrice, double maxPrice);
    @Query("SELECT p FROM Products p JOIN p.reviews r GROUP BY p.id ORDER BY AVG(r.rating) DESC")
    List<Products> findTopRatedProducts(); // assuming Reviews has a 'rating' field
    Page<Products> findAll(Pageable pageable);
}
