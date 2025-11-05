package com.project.productservice.controller;

import com.project.productservice.entity.Product;
import com.project.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Gọi nó bằng URL: /api/products/search/ids?name=keyword
     */
    @GetMapping("/search/ids")
    public List<UUID> searchProductIdsByName(@RequestParam String name) {
        return productService.searchProductIdsByName(name);
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

}