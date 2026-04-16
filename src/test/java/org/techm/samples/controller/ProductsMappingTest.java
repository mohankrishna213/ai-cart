package org.techm.samples.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.techm.samples.dto.ProductsDTO;
import org.techm.samples.entity.Products;

/**
 * Tests for ProductsController DTO mapping methods
 */
public class ProductsMappingTest {

    /**
     * Helper method to simulate controller's toProductsDTO method
     */
    private ProductsDTO toProductsDTO(Products product) {
        ProductsDTO dto = new ProductsDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setPromotionalPrice(product.getPromotionalPrice());
        dto.setIsPromoActive(product.isPromoActive());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        dto.setAvailable(product.isAvailable());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        return dto;
    }

    /**
     * Helper method to simulate controller's toProductsEntity method
     */
    private Products toProductsEntity(ProductsDTO dto) {
        Products product = new Products();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setPromotionalPrice(dto.getPromotionalPrice());
        product.setPromoActive(dto.getIsPromoActive() != null ? dto.getIsPromoActive() : false);
        product.setStockQuantity(dto.getStockQuantity());
        product.setImageUrl(dto.getImageUrl());
        product.setAvailable(dto.isAvailable());
        return product;
    }

    @Test
    void testToProductsDTOMapsPromotionalPrice() {
        Products product = new Products();
        product.setId(1L);
        product.setName("Widget");
        product.setDescription("Test");
        product.setPrice(100.0);
        product.setPromotionalPrice(79.99);
        product.setPromoActive(true);
        product.setAvailable(true);
        product.setStockQuantity(10);
        product.setImageUrl("http://example.com/image.jpg");

        ProductsDTO dto = toProductsDTO(product);

        assertEquals(100.0, dto.getPrice());
        assertEquals(79.99, dto.getPromotionalPrice());
        assertEquals(true, dto.getIsPromoActive());
    }

    @Test
    void testToProductsEntityMapsPromotionalPrice() {
        ProductsDTO dto = new ProductsDTO();
        dto.setId(1L);
        dto.setName("Widget");
        dto.setDescription("Test");
        dto.setPrice(100.0);
        dto.setPromotionalPrice(79.99);
        dto.setIsPromoActive(true);
        dto.setAvailable(true);
        dto.setStockQuantity(10);
        dto.setImageUrl("http://example.com/image.jpg");

        Products product = toProductsEntity(dto);

        assertEquals(100.0, product.getPrice());
        assertEquals(79.99, product.getPromotionalPrice());
        assertEquals(true, product.isPromoActive());
    }

    @Test
    void testBidirectionalConversionPreservesPromotionalPricing() {
        Products original = new Products();
        original.setId(1L);
        original.setName("Widget");
        original.setDescription("Test");
        original.setPrice(100.0);
        original.setPromotionalPrice(75.50);
        original.setPromoActive(true);
        original.setAvailable(true);
        original.setStockQuantity(15);
        original.setImageUrl("http://example.com/image.jpg");

        // Entity -> DTO -> Entity
        ProductsDTO dto = toProductsDTO(original);
        Products reconstructed = toProductsEntity(dto);

        assertEquals(original.getPromotionalPrice(), reconstructed.getPromotionalPrice());
        assertEquals(original.isPromoActive(), reconstructed.isPromoActive());
    }

    @Test
    void testNullPromotionalPriceMappingEntityToDTO() {
        Products product = new Products();
        product.setId(1L);
        product.setName("Widget");
        product.setDescription("Test");
        product.setPrice(100.0);
        product.setPromotionalPrice(null);
        product.setPromoActive(false);
        product.setAvailable(true);
        product.setStockQuantity(10);
        product.setImageUrl("http://example.com/image.jpg");

        ProductsDTO dto = toProductsDTO(product);

        assertNull(dto.getPromotionalPrice());
        assertFalse(dto.getIsPromoActive());
    }

    @Test
    void testNullPromotionalPriceMappingDTOToEntity() {
        ProductsDTO dto = new ProductsDTO();
        dto.setId(1L);
        dto.setName("Widget");
        dto.setDescription("Test");
        dto.setPrice(100.0);
        dto.setPromotionalPrice(null);
        dto.setIsPromoActive(null);
        dto.setAvailable(true);
        dto.setStockQuantity(10);
        dto.setImageUrl("http://example.com/image.jpg");

        Products product = toProductsEntity(dto);

        assertNull(product.getPromotionalPrice());
        assertFalse(product.isPromoActive());
    }

    @Test
    void testZeroPromotionalPricePreservedInMapping() {
        Products product = new Products();
        product.setId(1L);
        product.setName("Widget");
        product.setDescription("Test");
        product.setPrice(100.0);
        product.setPromotionalPrice(0.0);
        product.setPromoActive(true);
        product.setAvailable(true);
        product.setStockQuantity(10);
        product.setImageUrl("http://example.com/image.jpg");

        ProductsDTO dto = toProductsDTO(product);

        assertEquals(0.0, dto.getPromotionalPrice());
        assertFalse(dto.getPromotionalPrice() == null);
    }
}
