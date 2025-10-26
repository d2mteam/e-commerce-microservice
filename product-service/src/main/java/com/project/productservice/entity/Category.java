package com.project.productservice.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "categories")
public class Category {
    @Id
    private Long categoryId;

    private String categoryName;

    private String categoryDescription;
}
