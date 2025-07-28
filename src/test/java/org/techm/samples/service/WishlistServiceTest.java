package org.techm.samples.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.techm.samples.entity.User;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.Wishlist_items;
import org.techm.samples.exception.DuplicateWishlistException;
import org.techm.samples.exception.ResourceNotFoundException;
import org.techm.samples.repository.WishlistItemsRepository;
import org.techm.samples.service.products.WishlistService;

@ExtendWith(MockitoExtension.class)
public class WishlistServiceTest {

    @Mock
    private WishlistItemsRepository repo;

    @InjectMocks
    private WishlistService service;

    private User buildUser() {
        User u = new User();
        u.setId(1L);
        return u;
    }

    private Products buildProduct() {
        Products p = new Products();
        p.setId(2L);
        return p;
    }

    @Test
    void testAddToWishlistSuccess() {
        User u = buildUser();
        Products p = buildProduct();
        // No existing item
        when(repo.findByUserAndProduct(u, p)).thenReturn(Optional.empty());

        Wishlist_items saved = new Wishlist_items(u, p);
        when(repo.save(any(Wishlist_items.class))).thenReturn(saved);

        Wishlist_items result = service.addToWishlist(u, p);

        assertEquals(u, result.getUser());
        assertEquals(p, result.getProduct());
        verify(repo).save(any(Wishlist_items.class));
    }

    @Test
    void testAddToWishlistDuplicate() {
        User u = buildUser();
        Products p = buildProduct();

        Wishlist_items existing = new Wishlist_items(u, p);
        when(repo.findByUserAndProduct(u, p)).thenReturn(Optional.of(existing));

        assertThrows(DuplicateWishlistException.class, () -> service.addToWishlist(u, p));
        verify(repo, never()).save(any());
    }

    @Test
    void testRemoveFromWishlistSuccess() {
        User u = buildUser();
        Products p = buildProduct();

        Wishlist_items existing = new Wishlist_items(u, p);
        when(repo.findByUserAndProduct(u, p)).thenReturn(Optional.of(existing));

        service.removeFromWishlist(u, p);

        verify(repo).delete(existing);
    }

    @Test
    void testRemoveFromWishlistNotFound() {
        User u = buildUser();
        Products p = buildProduct();
        when(repo.findByUserAndProduct(u, p)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> service.removeFromWishlist(u, p)
        );
        assertTrue(ex.getMessage().contains("Wishlist item not found"));
    }

    @Test
    void testGetWishlistByUser() {
        User u = buildUser();
        Products p = buildProduct();

        Wishlist_items item = new Wishlist_items(u, p);
        when(repo.findByUser(u)).thenReturn(List.of(item));

        List<Wishlist_items> list = service.getWishlistByUser(u);

        assertEquals(1, list.size());
        assertSame(item, list.get(0));
    }
}
