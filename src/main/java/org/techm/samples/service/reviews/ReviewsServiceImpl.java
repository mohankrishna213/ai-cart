package org.techm.samples.service.reviews;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.techm.samples.entity.*;
import org.techm.samples.exception.DuplicateReviewException;
import org.techm.samples.exception.ResourceNotFoundException;
import org.techm.samples.repository.ReviewsRepository;

@Service
public class ReviewsServiceImpl implements ReviewsService {

    @Autowired
    private ReviewsRepository reviewsRepository;

    @Override
    public Reviews addReview(Reviews review) {
        Optional<Reviews> existing =
            reviewsRepository.findByProductAndUser(review.getProduct(), review.getUser());

        if (existing.isPresent()) {
            throw new DuplicateReviewException(review.getProduct().getId());
        }
        return reviewsRepository.save(review);
    }

    @Override
    public List<Reviews> getAllReviews() {
        return reviewsRepository.findAll();
    }

    @Override
    public Reviews getReviewById(Reviews review) {
        return reviewsRepository.findById(review.getId())
            .orElseThrow(() ->
                new ResourceNotFoundException("Review not found with id: " + review.getId()));
    }

    @Override
    public void deleteReview(Long id) {
        if (reviewsRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Review not found with id: " + id);
        }
        reviewsRepository.deleteById(id);
    }

    @Override
    public List<Reviews> getReviewsByProduct(Products product) {
        return reviewsRepository.findAllByProduct(product);
    }

    @Override
    public Optional<Reviews> getReviewByProductAndUser(Products product, User user) {
        return reviewsRepository.findByProductAndUser(product, user);
    }

    @Override
    public Optional<Reviews> getReviewByIdEntity(Long id) {
        return reviewsRepository.findById(id);
    }
}
