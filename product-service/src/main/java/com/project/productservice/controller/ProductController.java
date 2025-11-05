package com.project.productservice.controller;

import com.project.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products") // Tiền tố chung cho các API liên quan đến Product
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
}