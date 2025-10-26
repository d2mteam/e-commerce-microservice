package com.project.productservice.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Document(collection = "products")
public class Product {
    @Id
    private Long productId;

    private String productName;

    private double productPrice;

    private String productDescription;

    private String categoryName;

    private List<String> productImages;

    private Map<String, Object> attributes;
}
