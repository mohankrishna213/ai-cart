package org.techm.samples.service.reviews;

import java.util.List;
import java.util.Optional;

import org.techm.samples.entity.Products;
import org.techm.samples.entity.Reviews;
import org.techm.samples.entity.User;

public interface ReviewsService {
	Reviews addReview(Reviews review);
	
	List<Reviews> getAllReviews();
	
	Reviews getReviewById(Reviews review);
	
	void deleteReview(Long id);

	List<Reviews> getReviewsByProduct(Products product);

	Optional<Reviews> getReviewByProductAndUser(Products product, User user);

}
