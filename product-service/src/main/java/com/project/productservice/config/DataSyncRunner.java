package com.project.productservice.config;

import com.project.productservice.entity.Product;
import com.project.productservice.repository.ProductRepository;
import com.project.productservice.repository.ProductSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j // Dùng để log (Tùy chọn)
public class DataSyncRunner implements CommandLineRunner {

    @Autowired
    private ProductRepository jpaRepository; // SQL

    @Autowired
    private ProductSearchRepository searchRepository; // Elasticsearch

    @Override
    public void run(String... args) throws Exception {
        log.info("Bắt đầu đồng bộ dữ liệu từ PostgreSQL sang Elasticsearch...");

        // Đếm số lượng trong Elasticsearch
        long esCount = searchRepository.count();

        // Đếm số lượng trong SQL
        long sqlCount = jpaRepository.count();

        // Chỉ đồng bộ nếu số lượng không khớp
        if (esCount != sqlCount) {
            log.warn("Phát hiện không đồng bộ! SQL: {}, Elasticsearch: {}. Đang tiến hành đồng bộ...", sqlCount, esCount);

            // Xóa tất cả trong Elasticsearch để làm sạch
            searchRepository.deleteAll();

            // Đọc tất cả từ SQL
            List<Product> allProducts = jpaRepository.findAll();

            // Ghi tất cả vào Elasticsearch
            searchRepository.saveAll(allProducts);

            log.info("Đồng bộ hoàn tất! Đã index {} sản phẩm.", allProducts.size());
        } else {
            log.info("Dữ liệu đã đồng bộ. ({} sản phẩm)", esCount);
        }
    }
}