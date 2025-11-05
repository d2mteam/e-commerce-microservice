package com.project.productservice.service;

import com.project.productservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<UUID> searchProductIdsByName(String keyword) {
        return productRepository.findProductIdsByNameContaining(keyword);
    }
}