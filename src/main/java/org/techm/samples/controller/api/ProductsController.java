package org.techm.samples.controller.api;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.techm.samples.dto.ProductsDTO;
import org.techm.samples.dto.WishlistItemsDTO;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.Wishlist_items;
import org.techm.samples.entity.User;
import org.techm.samples.exception.ResourceNotFoundException;
import org.techm.samples.repository.UserInfoRepository;
import org.techm.samples.repository.WishlistItemsRepository;
import org.techm.samples.service.products.ProductService;
import org.techm.samples.service.products.WishlistService;

@Controller
@RequestMapping("/api/products")
public class ProductsController {
	@Autowired
    private ProductService productService;
	
	@Autowired
	private WishlistService wishlistService;
	
	@Autowired
	private WishlistItemsRepository wishlistItemsRepository;
	
	@Autowired
	private UserInfoRepository userInfoRepository;
	
	@GetMapping
    public ResponseEntity<Page<ProductsDTO>> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size,
                                 @RequestParam(defaultValue = "name") String sortBy,
                                 @RequestParam(defaultValue = "asc") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<Products> products = productService.getAllProducts(pageable);
    Page<ProductsDTO> dtoPage = products.map(this::toProductsDTO);
    return new ResponseEntity<>(dtoPage, HttpStatus.OK);
}

	@GetMapping("/search")
    public ResponseEntity<List<ProductsDTO>> searchProducts(@RequestParam String q,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size) {
    List<Products> products = productService.searchProductsByName(q);
    List<ProductsDTO> dtoList = products.stream().map(this::toProductsDTO).toList();
    return new ResponseEntity<>(dtoList, HttpStatus.OK);
}

	@GetMapping("/{id}")
    public ResponseEntity<ProductsDTO> getProductById(@PathVariable Long id) {
    Products product = productService.getProductById(id);
    if (product == null) {
        throw new ResourceNotFoundException("Product not found with id: " + id);
    }
    return new ResponseEntity<>(toProductsDTO(product), HttpStatus.OK);
}

	@PostMapping("/admin")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<ProductsDTO> createProduct(@RequestBody ProductsDTO productsDTO){
    Products product = toProductsEntity(productsDTO);
    Products saved = productService.addProduct(product);
    return new ResponseEntity<>(toProductsDTO(saved), HttpStatus.CREATED);
}

	@PostMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductsDTO> updateProduct(@PathVariable Long id, @RequestBody ProductsDTO productsDTO) {
    Products product = toProductsEntity(productsDTO);
    Products updated = productService.updateProduct(product, id);
    return new ResponseEntity<>(toProductsDTO(updated), HttpStatus.CREATED);
}

	@DeleteMapping("/admin/{id}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
	    productService.deleteProduct(id);
	    return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/wishlist/add/{productId}")
	@PreAuthorize("hasAuthority('CUSTOMER')")
	public ResponseEntity<?> addProductToWishlist(@PathVariable Long productId, Principal principal) {
	    Optional<User> userOpt = userInfoRepository.findByEmail(principal.getName());
	    Optional<Products> productOpt = productService.getProductByIdOptional(productId);
	    if (userOpt.isEmpty()) {
	        throw new ResourceNotFoundException("User not found: " + principal.getName());
	    }
	    if (productOpt.isEmpty()) {
	        throw new ResourceNotFoundException("Product not found with id: " + productId);
	    }
	    Wishlist_items item = wishlistService.addToWishlist(userOpt.get(), productOpt.get());
	    WishlistItemsDTO dto = new WishlistItemsDTO();
	    dto.setId(item.getId());
	    dto.setUserId(item.getUser().getId());
	    dto.setProductId(item.getProduct().getId());
	    dto.setProductName(item.getProduct().getName());
	    dto.setProductImage(item.getProduct().getImageUrl());
	    return new ResponseEntity<>(dto, HttpStatus.CREATED);
	}

	@DeleteMapping("/wishlist/remove/{productId}")
	@PreAuthorize("hasAuthority('CUSTOMER')")
	public ResponseEntity<?> removeProductFromWishlist(@PathVariable Long productId, Principal principal) {
	    Optional<User> userOpt = userInfoRepository.findByEmail(principal.getName());
	    Optional<Products> productOpt = productService.getProductByIdOptional(productId);
	    if (userOpt.isEmpty()) {
	        throw new ResourceNotFoundException("User not found: " + principal.getName());
	    }
	    if (productOpt.isEmpty()) {
	        throw new ResourceNotFoundException("Product not found with id: " + productId);
	    }
	    wishlistService.removeFromWishlist(userOpt.get(), productOpt.get());
	    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@GetMapping("/category/{categoryId}")
	public ResponseEntity<List<ProductsDTO>> getProductsByCategory(@PathVariable Long categoryId) {
    List<Products> products = productService.getProductsByCategory(categoryId);
    List<ProductsDTO> dtoList = products.stream().map(this::toProductsDTO).toList();
    return new ResponseEntity<>(dtoList, HttpStatus.OK);
}

	@GetMapping("/available")
	public ResponseEntity<List<ProductsDTO>> getAvailableProducts() {
    List<Products> products = productService.getAvailableProducts();
    List<ProductsDTO> dtoList = products.stream().map(this::toProductsDTO).toList();
    return new ResponseEntity<>(dtoList, HttpStatus.OK);
}

	@GetMapping("/price-range")
	public ResponseEntity<List<ProductsDTO>> getProductsByPriceRange(@RequestParam double minPrice, @RequestParam double maxPrice) {
    List<Products> products = productService.getProductsByPriceRange(minPrice, maxPrice);
    List<ProductsDTO> dtoList = products.stream().map(this::toProductsDTO).toList();
    return new ResponseEntity<>(dtoList, HttpStatus.OK);
}

	@GetMapping("/top-rated")
	public ResponseEntity<List<ProductsDTO>> getTopRatedProducts() {
    List<Products> products = productService.getTopRatedProducts();
    List<ProductsDTO> dtoList = products.stream().map(this::toProductsDTO).toList();
    return new ResponseEntity<>(dtoList, HttpStatus.OK);
}

	// DTO conversion helpers
	private ProductsDTO toProductsDTO(Products product) {
    ProductsDTO dto = new ProductsDTO();
    dto.setId(product.getId());
    dto.setName(product.getName());
    dto.setDescription(product.getDescription());
    dto.setPrice(product.getPrice());
    dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
    dto.setAvailable(product.isAvailable());
    dto.setStockQuantity(product.getStockQuantity());
    dto.setImageUrl(product.getImageUrl());
    return dto;
}

	private Products toProductsEntity(ProductsDTO dto) {
    Products product = new Products();
    product.setId(dto.getId());
    product.setName(dto.getName());
    product.setDescription(dto.getDescription());
    product.setPrice(dto.getPrice());
    product.setStockQuantity(dto.getStockQuantity());
    product.setImageUrl(dto.getImageUrl());
    product.setAvailable(dto.isAvailable());
    
    return product;
}
}
