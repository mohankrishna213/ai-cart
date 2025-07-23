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
import org.techm.samples.dto.WishlistItemsDTO;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.Wishlist_items;
import org.techm.samples.entity.User;
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
    public ResponseEntity<Page<Products>> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size,
                                 @RequestParam(defaultValue = "name") String sortBy,
                                 @RequestParam(defaultValue = "asc") String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
	    Pageable pageable = PageRequest.of(page, size, sort);

	    Page<Products> products = productService.getAllProducts(pageable);
	    
	    return new ResponseEntity<>(products,HttpStatus.OK);
	}
	
	@GetMapping("/search")
    public ResponseEntity<List<Products>> searchProducts(@RequestParam String q,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        List<Products> products = productService.searchProductsByName(q);
        return new ResponseEntity<>(products,HttpStatus.OK);
	}
	
	@GetMapping("/{id}")
    public ResponseEntity<Products> getProductById(@PathVariable Long id) {
        Products product = productService.getProductById(id);
        return new ResponseEntity<>(product,HttpStatus.OK);
    }
	
	@PostMapping("/admin")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<Products> createProduct(@RequestBody Products products){
		productService.addProduct(products);
		return new ResponseEntity<>(products,HttpStatus.CREATED);
	}
	
	@PostMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Products> updateProduct(@PathVariable Long id, @RequestBody Products product) {
		return new ResponseEntity<>(productService.updateProduct(product, id),HttpStatus.CREATED);
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
	    Optional<User> userOpt = userInfoRepository.findByUsername(principal.getName());
	    Optional<Products> productOpt = productService.getProductByIdOptional(productId);
	    if (userOpt.isEmpty() || productOpt.isEmpty()) {
	        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	    }
	    Wishlist_items item = wishlistService.addToWishlist(userOpt.get(), productOpt.get());
	    WishlistItemsDTO dto = new WishlistItemsDTO();
	    dto.setId(item.getId());
	    dto.setUserId(item.getUser().getId());
	    dto.setProductId(item.getProduct().getId());
	    dto.setProductName(item.getProduct().getName());
	    // If product has image field, set it here
	    // dto.setProductImage(item.getProduct().getImage());
	    return new ResponseEntity<>(dto, HttpStatus.CREATED);
	}

	@DeleteMapping("/wishlist/remove/{productId}")
	@PreAuthorize("hasAuthority('CUSTOMER')")
	public ResponseEntity<?> removeProductFromWishlist(@PathVariable Long productId, Principal principal) {
	    Optional<User> userOpt = userInfoRepository.findByUsername(principal.getName());
	    Optional<Products> productOpt = productService.getProductByIdOptional(productId);
	    if (userOpt.isEmpty() || productOpt.isEmpty()) {
	        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	    }
	    wishlistService.removeFromWishlist(userOpt.get(), productOpt.get());
	    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
