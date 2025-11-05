package com.project.productservice.service;

import com.project.productservice.entity.Product;
import com.project.productservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<UUID> searchProductIdsByName(String keyword) {
        return productRepository.findProductIdsByNameContaining(keyword);
    }

    public Optional<Product> getProductByName(String productName) {
        return productRepository.findByProductName(productName);
    }
}