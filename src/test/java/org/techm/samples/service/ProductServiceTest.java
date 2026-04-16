package org.techm.samples.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.techm.samples.entity.Products;
import org.techm.samples.exception.InvalidPromotionalPriceException;
import org.techm.samples.exception.ResourceNotFoundException;
import org.techm.samples.repository.ProductsRepository;
import org.techm.samples.service.products.ProductServiceImpl;


public class ProductServiceTest {

    @Mock
    private ProductsRepository productRepo;

    @InjectMocks
    private ProductServiceImpl service;

    private Products sampleProduct() {
        Products p = new Products();
        p.setId(1L);
        p.setName("Widget");
        p.setDescription("Test widget");
        p.setPrice(9.99);
        return p;
    }
    
    @BeforeEach
    void init() {
      MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddProduct() {
        Products p = sampleProduct();
        when(productRepo.save(p)).thenReturn(p);

        Products result = service.addProduct(p);

        assertEquals("Widget", result.getName());
        verify(productRepo).save(p);
    }

    @Test
    void testGetProductByIdFound() {
        Products p = sampleProduct();
        when(productRepo.findById(1L)).thenReturn(Optional.of(p));

        Products result = service.getProductById(1L);

        assertEquals(1L, result.getId());
        verify(productRepo).findById(1L);
    }

    @Test
    void testGetProductByIdNotFound() {
        when(productRepo.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getProductById(42L));
        verify(productRepo).findById(42L);
    }

    @Test
    void testGetAllProductsWithPageable() {
        Products p1 = sampleProduct();
        Products p2 = sampleProduct();
        p2.setId(2L);
        Page<Products> page = new PageImpl<>(Arrays.asList(p1, p2));
        Pageable pageable = PageRequest.of(0, 2);
        when(productRepo.findAll(pageable)).thenReturn(page);

        Page<Products> result = service.getAllProducts(pageable);

        assertEquals(2, result.getTotalElements());
        verify(productRepo).findAll(pageable);
    }

    @Test
    @WithMockUser
    void testGetAllProductsList() {
        when(productRepo.findAll()).thenReturn(Collections.singletonList(sampleProduct()));

        List<Products> list = service.getAllProducts();

        assertEquals(1, list.size());
        verify(productRepo).findAll();
    }

    @Test
    void testDeleteProduct() {
        Products p = sampleProduct();
        when(productRepo.findById(1L)).thenReturn(Optional.of(p));

        service.deleteProduct(1L);

        verify(productRepo).delete(p);
    }

    @Test
    void testUpdateProduct() {
        Products existing = sampleProduct();
        when(productRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepo.save(existing)).thenReturn(existing);

        Products toUpdate = new Products();
        toUpdate.setName("Updated");
        toUpdate.setDescription("Desc");
        toUpdate.setPrice(19.99);
        toUpdate.setAvailable(false);
        toUpdate.setStockQuantity(5);

        Products result = service.updateProduct(toUpdate, 1L);

        assertEquals("Updated", result.getName());
        assertFalse(result.isAvailable());
        assertEquals(5, result.getStockQuantity());
        verify(productRepo).save(existing);
    }

    @Test
    void testSearchAndFilterMethods() {
        Products p = sampleProduct();
        when(productRepo.findByNameContaining("W")).thenReturn(List.of(p));
        when(productRepo.findByCategoryId(1L)).thenReturn(List.of(p));
        when(productRepo.findByAvailableTrue()).thenReturn(List.of(p));
        when(productRepo.findByNameContainingIgnoreCase("w")).thenReturn(List.of(p));
        when(productRepo.findByPriceBetween(0.0, 10.0)).thenReturn(List.of(p));
        when(productRepo.findTopRatedProducts()).thenReturn(List.of(p));

        assertEquals(1, service.getProductsContaining("W").size());
        assertEquals(1, service.getProductsByCategory(1L).size());
        assertEquals(1, service.getAvailableProducts().size());
        assertEquals(1, service.searchProductsByName("w").size());
        assertEquals(1, service.getProductsByPriceRange(0.0, 10.0).size());
        assertEquals(1, service.getTopRatedProducts().size());
    }

    @Test
    void testGetProductByIdOptional() {
        Products p = sampleProduct();
        when(productRepo.findById(1L)).thenReturn(Optional.of(p));

        Optional<Products> opt = service.getProductByIdOptional(1L);

        assertTrue(opt.isPresent());
        assertEquals(p, opt.get());
    }

    // Promotional Pricing Validation Tests
    
    @Test
    void testUpdateProductWithValidPromotionalPrice() {
        Products existing = sampleProduct();
        existing.setPrice(100.0);
        
        Products updated = sampleProduct();
        updated.setPrice(100.0);
        updated.setPromotionalPrice(79.99);
        updated.setPromoActive(true);
        
        when(productRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepo.save(any(Products.class))).thenReturn(updated);
        
        Products result = service.updateProduct(updated, 1L);
        
        assertEquals(79.99, result.getPromotionalPrice());
        assertTrue(result.isPromoActive());
        verify(productRepo).save(any(Products.class));
    }
    
    @Test
    void testUpdateProductRejectsPromotionalPriceGreaterThanStandardPrice() {
        Products existing = sampleProduct();
        existing.setPrice(100.0);
        
        Products updated = new Products();
        updated.setPrice(100.0);
        updated.setPromotionalPrice(150.0); // Greater than standard price
        updated.setPromoActive(true);
        
        when(productRepo.findById(1L)).thenReturn(Optional.of(existing));
        
        assertThrows(InvalidPromotionalPriceException.class, 
                     () -> service.updateProduct(updated, 1L));
        
        verify(productRepo, never()).save(any(Products.class));
    }
    
    @Test
    void testUpdateProductRejectsNegativePromotionalPrice() {
        Products existing = sampleProduct();
        existing.setPrice(100.0);
        
        Products updated = new Products();
        updated.setPrice(100.0);
        updated.setPromotionalPrice(-10.0); // Negative price
        updated.setPromoActive(true);
        
        when(productRepo.findById(1L)).thenReturn(Optional.of(existing));
        
        assertThrows(InvalidPromotionalPriceException.class,
                     () -> service.updateProduct(updated, 1L));
        
        verify(productRepo, never()).save(any(Products.class));
    }
    
    @Test
    void testUpdateProductAcceptsInvalidPriceWhenPromoNotActive() {
        Products existing = sampleProduct();
        existing.setPrice(100.0);
        
        Products updated = new Products();
        updated.setPrice(100.0);
        updated.setPromotionalPrice(150.0); // Greater than standard price, but promo is inactive
        updated.setPromoActive(false);
        updated.setName("Updated");
        updated.setDescription("Updated");
        updated.setAvailable(true);
        
        when(productRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepo.save(any(Products.class))).thenReturn(updated);
        
        Products result = service.updateProduct(updated, 1L);
        
        assertEquals(150.0, result.getPromotionalPrice());
        assertFalse(result.isPromoActive());
        verify(productRepo).save(any(Products.class));
    }
    
    @Test
    void testUpdateProductWithZeroPromotionalPrice() {
        Products existing = sampleProduct();
        existing.setPrice(100.0);
        
        Products updated = new Products();
        updated.setPrice(100.0);
        updated.setPromotionalPrice(0.0); // Zero is valid (< 100.0 and >= 0)
        updated.setPromoActive(true);
        
        when(productRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepo.save(any(Products.class))).thenReturn(updated);
        
        Products result = service.updateProduct(updated, 1L);
        
        assertEquals(0.0, result.getPromotionalPrice());
        assertTrue(result.isPromoActive());
        verify(productRepo).save(any(Products.class));
    }
}

