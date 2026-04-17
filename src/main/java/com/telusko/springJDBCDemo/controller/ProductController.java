package com.telusko.springJDBCDemo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.telusko.springJDBCDemo.model.Product;
import com.telusko.springJDBCDemo.service.ProductService;

@RestController
@CrossOrigin(origins = "http://localhost:4200") // Allow requests from the React frontend
@RequestMapping("/api")
public class ProductController {
	@Autowired
	private ProductService service;
	
	
	
	@GetMapping("/products")
	public ResponseEntity<List<Product>> getAllProducts() {
	    List<Product> products = service.findAll();
	    return ResponseEntity.ok(products);
	}

	@GetMapping("/products/search")
	public ResponseEntity<List<Product>> searchProductsByCategory(@RequestParam(required = false) String category) {
	    return ResponseEntity.ok(service.searchByCategory(category));
	}
	
	@PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addProduct(
			@RequestPart("p") Product p,
			@RequestPart("imageFile") MultipartFile imageFile) {
	    try {
	        Product createdProduct = service.addProduct(p, imageFile);
	        return ResponseEntity.ok(createdProduct);
	    } catch (Exception e) {
	        return ResponseEntity.status(500).body("Error adding product: " + e.getMessage());
	    }
	}
	
	@PutMapping("/products/{id}")
	public Product updateProduct(@PathVariable int id, @RequestBody Product p) {
	    // Implement logic to update the product with the given id
	    // You can use the service layer to perform the update operation
	    return service.updateProduct(id, p);
	}
	
	@GetMapping("/products/{id:\\d+}")
	public Product getProduct(@PathVariable int id) {
	    // Implement logic to retrieve a product by its id
	    // You can use the service layer to perform the retrieval operation
	    return service.getProductById(id);
	}
	
	
	
	
}
