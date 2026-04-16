package com.telusko.springJDBCDemo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.telusko.springJDBCDemo.model.Product;
import com.telusko.springJDBCDemo.repo.ProductRepo;

@Service
public class ProductService {
	@Autowired
	private ProductRepo repo;

	public List<Product> findAll() {
		return repo.findAll();
	}

	public Product updateProduct(int id, Product p) {
		// Implement logic to update the product with the given id
		// You can use the repo to perform the update operation
		Product existingProduct = repo.findById(id).orElse(null);
		if (existingProduct != null) {
			existingProduct.setName(p.getName());
			existingProduct.setBrand(p.getBrand());
			existingProduct.setPrice(p.getPrice());
			existingProduct.setCategory(p.getCategory());
			existingProduct.setAvailable(p.isAvailable());
			existingProduct.setQuantity(p.getQuantity());
			existingProduct.setDescription(p.getDescription());
			return repo.save(existingProduct);
		}
		return null; // or throw an exception if product not found
	}

	public Product getProductById(int id) {
		// TODO Auto-generated method stub
		return repo.findById(id).orElse(null);
	}

	public Product addProduct(Product product, MultipartFile imageFile)throws Exception {
		// TODO Auto-generated method stub
		product.setImageName(imageFile.getOriginalFilename());
		product.setImageType(imageFile.getContentType());
		product.setImageData(imageFile.getBytes());
		return repo.save(product);

	}
}
