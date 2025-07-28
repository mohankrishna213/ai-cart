package org.techm.samples.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.techm.samples.config.SecurityConfig;
import org.techm.samples.controller.api.ProductsController;
import org.techm.samples.dto.ProductsDTO;
import org.techm.samples.entity.Products;
import org.techm.samples.exception.ResourceNotFoundException;
import org.techm.samples.repository.UserInfoRepository;
import org.techm.samples.repository.WishlistItemsRepository;
import org.techm.samples.service.auth.JwtService;
import org.techm.samples.service.products.ProductService;
import org.techm.samples.service.products.WishlistService;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private WishlistService wishlistService;

    @MockBean
    private WishlistItemsRepository wishlistItemsRepository;

    @MockBean
    private UserInfoRepository userInfoRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService uds;

    @Test
    @WithMockUser
    void getProductByIdFound() throws Exception {
        Products product = new Products();
        product.setId(10L);
        product.setName("Laptop");
        product.setDescription("Gaming Laptop");
        product.setPrice(1200.0);
        product.setAvailable(true);
        product.setStockQuantity(5);
        product.setImageUrl("laptop.jpg");

        Mockito.when(productService.getProductById(10L)).thenReturn(product);

        mockMvc.perform(get("/api/products/10"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(10)))
               .andExpect(jsonPath("$.name", is("Laptop")))
               .andExpect(jsonPath("$.price", is(1200.0)));
    }

    @Test
    @WithMockUser
    void getProductByIdNotFound() throws Exception {
        Mockito.when(productService.getProductById(99L))
               .thenThrow(new ResourceNotFoundException("Product not found with id: 99"));

        mockMvc.perform(get("/api/products/99"))
               .andExpect(status().isNotFound())
               .andExpect(content().string(containsString("Product not found with id: 99")));
    }

    @Test
    @WithMockUser(username = "admin",roles = "ADMIN")
    void createProductReturnsCreated() throws Exception {
        ProductsDTO dto = new ProductsDTO();
        dto.setName("Smartphone");
        dto.setDescription("Latest model");
        dto.setPrice(999.99);
        dto.setAvailable(true);
        dto.setStockQuantity(10);
        dto.setImageUrl("smartphone.jpg");

        Products saved = new Products();
        saved.setId(1L);
        saved.setName(dto.getName());
        saved.setDescription(dto.getDescription());
        saved.setPrice(dto.getPrice());
        saved.setAvailable(dto.isAvailable());
        saved.setStockQuantity(dto.getStockQuantity());
        saved.setImageUrl(dto.getImageUrl());

        Mockito.when(productService.addProduct(Mockito.any(Products.class))).thenReturn(saved);

        mockMvc.perform(post("/api/products/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id", is(1)))
               .andExpect(jsonPath("$.name", is("Smartphone")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProductReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/admin/5"))
               .andExpect(status().isNoContent());

        Mockito.verify(productService).deleteProduct(5L);
    }

    @Test
    @WithMockUser
    void searchProductsReturnsList() throws Exception {
        Products p = new Products();
        p.setId(2L);
        p.setName("Headphones");
        p.setDescription("Noise cancelling");
        p.setPrice(199.99);
        p.setAvailable(true);
        p.setStockQuantity(15);
        p.setImageUrl("headphones.jpg");

        Mockito.when(productService.searchProductsByName("Headphones"))
               .thenReturn(List.of(p));

        mockMvc.perform(get("/api/products/search?q=Headphones"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].name", is("Headphones")))
               .andExpect(jsonPath("$[0].price", is(199.99)));
    }
}
