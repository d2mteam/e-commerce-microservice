package com.project.productservice.repository;

import com.project.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//public interface ProductRepository extends MongoRepository<Product, Long> {
//}
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p.productId FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<UUID> findProductIdsByNameContaining(@Param("name") String name);

    Optional<Product> findByProductName(String productName);
}

