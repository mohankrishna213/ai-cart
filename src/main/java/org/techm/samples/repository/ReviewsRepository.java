package org.techm.samples.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.Reviews;
import org.techm.samples.entity.User;

public interface ReviewsRepository extends JpaRepository<Reviews, Long> {
	List<Reviews> findAllByProduct(Products product);

	Optional<Reviews> findByProductAndUser(Products product, User user);
}
