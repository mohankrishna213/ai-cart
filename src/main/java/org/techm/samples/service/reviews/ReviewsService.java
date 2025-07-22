package org.techm.samples.service.reviews;

import java.util.List;

import org.techm.samples.entity.Reviews;

public interface ReviewsService {
	Reviews addReview(Reviews review);
	
	List<Reviews> getAll();
	
	Reviews getReviewById(Reviews review);
	
	 void deleteReview(Long id);

}
