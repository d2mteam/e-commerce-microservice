package com.project.productservice.service;

import com.project.productservice.entity.Product;
import com.project.productservice.repository.ProductRepository;
import com.project.productservice.repository.ProductSearchRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    private ProductRepository productRepository;
    private ProductSearchRepository searchRepository;

    @Autowired
    public ProductService(ProductRepository productRepository,
                          ProductSearchRepository searchRepository) {
        this.productRepository = productRepository;
        this.searchRepository = searchRepository;
    }

    @Transactional
    public Product createProduct(Product product) {
        // Lưu vào SQL (Nguồn dữ liệu chính)
        Product savedProduct = productRepository.save(product);

        // Index vào Elasticsearch
        searchRepository.save(savedProduct);

        return savedProduct;
    }

    /**
     * CẬP NHẬT:Cập nhật cả hai nơi
     */

    @Transactional
    public Product updateProduct(UUID id, Product productDetails) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Cập nhật các trường (ví dụ)
        existingProduct.setProductName(productDetails.getProductName());
        existingProduct.setProductPrice(productDetails.getProductPrice());
        existingProduct.setProductDescription(productDetails.getProductDescription());
        existingProduct.setProductImages(productDetails.getProductImages());
        existingProduct.setAttributes(productDetails.getAttributes());
        existingProduct.setCategoryName(productDetails.getCategoryName());

        // Lưu cập nhật vào SQL
        Product updatedProduct = productRepository.save(existingProduct);

        // Index lại vào Elasticsearch
        searchRepository.save(updatedProduct);

        return updatedProduct;
    }

    /**
     * XÓA: Xóa ở cả hai nơi
     */
    @Transactional
    public void deleteProduct(UUID id) {
        // Xóa khỏi SQL
        productRepository.deleteById(id);

        // Xóa khỏi Elasticsearch
        searchRepository.deleteById(id);
    }

    /**
     * TÌM KIẾM MỚI: Dùng Elasticsearch (Nhanh, trả về full object)
     */
    public List<Product> searchProductsByName(String keyword) {
        // Dùng Elasticsearch để tìm kiếm
        return searchRepository.findByProductNameContaining(keyword);
    }

    /**
     * LẤY THEO ID: Chỉ dùng SQL (Nguồn chính)
     */
    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public List<UUID> searchProductIdsByName(String keyword) {
        return productRepository.findProductIdsByNameContaining(keyword);
    }

    public Optional<Product> getProductByName(String productName) {
        return productRepository.findByProductName(productName);
    }
}