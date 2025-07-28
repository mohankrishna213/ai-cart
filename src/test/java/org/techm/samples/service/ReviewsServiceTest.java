package org.techm.samples.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.techm.samples.entity.*;
import org.techm.samples.exception.*;
import org.techm.samples.repository.ReviewsRepository;
import org.techm.samples.service.reviews.ReviewsService;
import org.techm.samples.service.reviews.ReviewsServiceImpl;


public class ReviewsServiceTest {

    @Mock 
    private ReviewsRepository repo;
    @InjectMocks 
    private ReviewsServiceImpl service;

    @BeforeEach
    void init() {
      MockitoAnnotations.openMocks(this);
    }
    
    private Products product() {
        Products p = new Products();
        p.setId(1L);
        return p;
    }

    private User user() {
        User u = new User();
        u.setId(2L);
        return u;
    }

    private Reviews review() {
        Reviews r = new Reviews();
        r.setId(3L);
        r.setProduct(product());
        r.setUser(user());
        r.setRating(4.5);
        return r;
    }

    @Test
    void testAddReviewSuccess() {
        Reviews r = review();
        when(repo.findByProductAndUser(product(), user())).thenReturn(Optional.empty());
        when(repo.save(r)).thenReturn(r);

        Reviews result = service.addReview(r);

        assertEquals(4.5, result.getRating());
        verify(repo).save(r);
    }

    @Test
    void testAddReviewDuplicate() {
        Reviews r = review();
        Products  p = r.getProduct();
        User      u = r.getUser();
        when(repo.findByProductAndUser(p, u)).thenReturn(Optional.of(r));

        assertThrows(DuplicateReviewException.class, () -> service.addReview(r));
    }

    @Test
    void testGetAllReviews() {
        Reviews r = review();
        when(repo.findAll()).thenReturn(List.of(r));

        List<Reviews> list = service.getAllReviews();

        assertEquals(1, list.size());
    }

    @Test
    void testDeleteReviewSuccess() {
        when(repo.findById(3L)).thenReturn(Optional.of(review()));

        service.deleteReview(3L);

        verify(repo).deleteById(3L);
    }

    @Test
    void testDeleteReviewNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteReview(99L));
    }

	/*
	 * @Test void testGetReviewsByProduct() { Reviews r = review();
	 * when(repo.findAllByProduct(product())).thenReturn(List.of(r));
	 * 
	 * List<Reviews> list = service.getReviewsByProduct(product());
	 * 
	 * assertEquals(1, list.size()); }
	 */
    @Test
    void testGetReviewsByProduct() {
      Products prod = product();
      Reviews r = review();

      // stub the exact method
      when(repo.findAllByProduct(prod))
        .thenReturn(List.of(r));

      List<Reviews> list = service.getReviewsByProduct(prod);

      assertEquals(1, list.size());
      // verify the right method was invoked
      verify(repo).findAllByProduct(prod);
    }


    @Test
    void testGetReviewByProductAndUser() {
    	Products prod = product();
        Reviews r = review();
        User u = user();
        when(repo.findByProductAndUser(prod, u)).thenReturn(Optional.of(r));

        Optional<Reviews> opt = service.getReviewByProductAndUser(prod, u);

        assertTrue(opt.isPresent());
        
        verify(repo).findByProductAndUser(prod,u);
        
    }

    @Test
    void testGetReviewByIdEntity() {
        Reviews r = review();
        when(repo.findById(3L)).thenReturn(Optional.of(r));

        Optional<Reviews> opt = service.getReviewByIdEntity(3L);

        assertTrue(opt.isPresent());
    }
}
