package com.project.productservice.controller;

import com.project.productservice.entity.Product;
import com.project.productservice.service.ProductService;
import com.project.productservice.utils.RequireRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Create product - ADMIN only
     */
    @PostMapping
    @RequireRole("ADMIN")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product newProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }

    /**
     * Update product - ADMIN only
     */
    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<Product> updateProduct(@PathVariable UUID id, @RequestBody Product productDetails) {
        Product updatedProduct = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Delete product - ADMIN only
     */
    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get product by ID - Any authenticated user (admin or user)
     */
    @GetMapping("/{id}")
    @RequireRole({"ADMIN", "USER"})
    public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Search product IDs by name - Any authenticated user
     */
    @GetMapping("/search/ids")
    @RequireRole({"ADMIN", "USER"})
    public List<UUID> searchProductIdsByName(@RequestParam String name) {
        return productService.searchProductIdsByName(name);
    }

    /**
     * Search products using Elasticsearch - Any authenticated user
     */
    @GetMapping("/search")
    // @RequireRole({"ADMIN", "USER"}) // Tạm comment để test tải
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        List<Product> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }

    /**
     * Get product by name - Any authenticated user
     */
    @GetMapping("/by-name")
    @RequireRole({"ADMIN", "USER"})
    public ResponseEntity<?> getProductByName(@RequestParam String name) {
        Optional<Product> productOptional = productService.getProductByName(name);

        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Product not found with name: " + name);
        }
    }
}