package com.project.productservice.repository;

import com.project.productservice.entity.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
//               ElasticsearchRepository<Product, UUID>
public interface ProductSearchRepository extends ElasticsearchRepository<Product, UUID> {

    /**
     * Tự động tạo truy vấn tìm kiếm full-text trên trường productName
     */
    List<Product> findByProductNameContaining(String name);

    /**
     * Tìm cả trong tên và mô tả
     */
    List<Product> findByProductNameOrProductDescriptionContaining(String name, String description);

    /**
     * Tìm theo danh mục (lọc chính xác)
     */
    List<Product> findByCategoryName(String categoryName);
}