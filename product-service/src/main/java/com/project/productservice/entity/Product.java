package com.project.productservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.annotation.Id;


import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "products")
public class Product {

    @Id
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "product_id", updatable = false, nullable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false)
    @Field(type = FieldType.Text, name = "productName")
    private String productName;

    @Column(name = "product_price", nullable = false)
    @Field(type = FieldType.Double, name = "productPrice")
    private double productPrice;

    @Column(name = "product_description")
    @Field(type = FieldType.Text, name = "productDescription")
    private String productDescription;

    @Column(name = "category_name")
    @Field(type = FieldType.Keyword, name = "categoryName")
    private String categoryName;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    @Field(type = FieldType.Keyword)
    private List<String> productImages;


    @Column(name = "attributes", columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Field(type = FieldType.Object)
    private Map<String, Object> attributes;
}
