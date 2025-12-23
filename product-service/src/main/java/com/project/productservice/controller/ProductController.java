package com.project.productservice.controller;

import com.project.productservice.dto.ProductSearchRequest;
import com.project.productservice.dto.ProductSearchResponse;

import com.project.productservice.entity.Product;
import com.project.productservice.service.ProductService;
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
     * TẠO MỚI (POST /api/products)
     * Hàm này gọi service, service sẽ lưu vào CẢ SQL và Elasticsearch.
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product newProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }

    /**
     * CẬP NHẬT (PUT /api/products/{id})
     * Hàm này gọi service, service sẽ cập nhật CẢ SQL và Elasticsearch.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable UUID id, @RequestBody Product productDetails) {
        Product updatedProduct = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * XÓA (DELETE /api/products/{id})
     * Hàm này gọi service, service sẽ xóa ở CẢ SQL và Elasticsearch.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * LẤY THEO ID (GET /api/products/{id})
     * Chỉ dùng SQL (nguồn dữ liệu chính)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Gọi nó bằng URL: /api/products/search/ids?name=keyword
     */
    @GetMapping("/search/ids")
    public List<UUID> searchProductIdsByName(@RequestParam String name) {
        return productService.searchProductIdsByName(name);
    }

    /**
     * TÌM KIẾM DÙNG ELASTICSEARCH
     * Gọi nó bằng URL: /api/products/search?name=keyword
     * Trả về danh sách Product đầy đủ.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        List<Product> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/by-name")
    public ResponseEntity<?> getProductByName(@RequestParam String name) {


        Optional<Product> productOptional = productService.getProductByName(name);


        if (productOptional.isPresent()) {

            Product product = productOptional.get();
            return ResponseEntity.ok(product);
        } else {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy sản phẩm với tên: " + name);
        }
    }

    /**
     * TÌM KIẾM NÂNG CAO VỚI FILTER + FACET
     * POST /api/products/search/advanced
     */
    @PostMapping("/search/advanced")
    public ResponseEntity<ProductSearchResponse> advancedSearch(@RequestBody ProductSearchRequest request) {
        ProductSearchResponse response = productService.searchWithFiltersAndFacets(request);
        return ResponseEntity.ok(response);
    }

    /**
     * AUTOCOMPLETE - Gợi ý tìm kiếm
     * GET /api/products/search/autocomplete?q=lap&limit=10
     */
    @GetMapping("/search/autocomplete")
    public ResponseEntity<List<String>> autocomplete(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        List<String> suggestions = productService.getAutocompleteSuggestions(q, limit);
        return ResponseEntity.ok(suggestions);
    }

}