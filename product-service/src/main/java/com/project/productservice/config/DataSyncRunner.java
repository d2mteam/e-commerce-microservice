package com.project.productservice.config;

import com.project.productservice.entity.Product;
import com.project.productservice.repository.ProductRepository;
import com.project.productservice.repository.ProductSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
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

        long esCount = searchRepository.count();
        long sqlCount = jpaRepository.count();

        if (esCount == sqlCount) {
            log.info("Dữ liệu đã đồng bộ. ({} sản phẩm)", esCount);
            return;
        }

        log.warn("Phát hiện không đồng bộ! SQL: {}, Elasticsearch: {}. Đang tiến hành đồng bộ...", sqlCount, esCount);

        // Xóa chỉ số cũ rồi ghi lại từ DB
        searchRepository.deleteAll();

        List<Product> allProducts;
        try {
            allProducts = jpaRepository.findAll();
        } catch (Exception e) {
            log.error("Không thể đọc dữ liệu từ PostgreSQL để đồng bộ (có thể do dữ liệu attributes không đúng định dạng JSON Map<String,String>). Bỏ qua đồng bộ, hãy làm sạch dữ liệu và chạy lại.", e);
            return;
        }

        searchRepository.saveAll(allProducts);

        log.info("Đồng bộ hoàn tất! Đã index {} sản phẩm.", allProducts.size());
    }
}
