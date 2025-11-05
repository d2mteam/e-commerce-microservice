package com.project.productservice.repository;

import com.project.productservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

//public interface CategoryRepository extends MongoRepository<Category, Long> {
//}
public interface CategoryRepository extends JpaRepository<Category, Long> { }

