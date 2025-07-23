package org.techm.samples.controller.api;

import java.util.List;

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
import org.techm.samples.entity.Products;
import org.techm.samples.service.products.ProductService;

@Controller
@RequestMapping("/api/products")
public class ProductsController {
	@Autowired
    private ProductService productService;
	
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

}
