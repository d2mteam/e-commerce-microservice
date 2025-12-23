package com.project.productservice.dto;

import com.project.productservice.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ProductSearchResponse {
    private List<Product> products;           // Danh sách sản phẩm
    private long totalElements;               // Tổng số kết quả
    private int totalPages;                   // Tổng số trang

    // Facet: Ví dụ {"Category": {"Laptop": 10, "Phone": 5}, "RAM": {"8GB": 20}}
    private Map<String, Map<String, Long>> facets;
}