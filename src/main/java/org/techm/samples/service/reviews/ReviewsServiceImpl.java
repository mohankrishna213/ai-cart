package org.techm.samples.service.reviews;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.techm.samples.entity.Reviews;
import org.techm.samples.repository.ReviewsRepository;

@Service
public class ReviewsServiceImpl implements ReviewsService {
@Autowired
private ReviewsRepository reviewsRepository;
	@Override
	public Reviews addReview(Reviews review) {
		
		return reviewsRepository.save(review);
	}

	@Override
	public List<Reviews> getAll() {
		return reviewsRepository.findAll();			}

	@Override
	public Reviews getReviewById(Reviews review) {
		Reviews result=reviewsRepository.findById(review.getId()).orElse(null);
		return result;
	}

	@Override
	public void deleteReview(Long id) {
	
		reviewsRepository.deleteById(id);
		
		
	}

} 