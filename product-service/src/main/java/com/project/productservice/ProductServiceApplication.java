package com.project.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
// Chỉ định JPA chỉ scan các class kế thừa JpaRepository
@EnableJpaRepositories(
        basePackages = "com.project.productservice.repository",
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaRepository.class)
)
// Chỉ định Elasticsearch chỉ scan các class kế thừa ElasticsearchRepository
@EnableElasticsearchRepositories(
        basePackages = "com.project.productservice.repository",
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ElasticsearchRepository.class)
)
public class ProductServiceApplication {

    //hello
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
