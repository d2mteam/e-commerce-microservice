package com.project.productservice.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ProductSearchRequest {
    private String keyword;             // Từ khóa tìm kiếm
    private Double minPrice;            // Giá thấp nhất
    private Double maxPrice;            // Giá cao nhất
    private String category;            // Lọc theo danh mục
    private Map<String, String> attributes; // Lọc động (VD: {"RAM": "16GB", "Color": "Red"})

    private int page = 0;
    private int size = 10;
    private String sortBy = "price";    // price, name
    private String sortOrder = "asc";   // asc, desc
}