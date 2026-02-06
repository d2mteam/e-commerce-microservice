package com.project.productservice.config;

import com.project.productservice.entity.Product;
import com.project.productservice.repository.ProductRepository;
import com.project.productservice.repository.ProductSearchRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataSyncRunner implements CommandLineRunner {

    @Autowired
    private ProductRepository jpaRepository; // SQL

    @Autowired
    private ProductSearchRepository searchRepository; // Elasticsearch

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional // Giữ transaction để lazy load hoạt động, nhưng phải clear cache thủ công
    public void run(String... args) throws Exception {
        log.info("Bắt đầu đồng bộ dữ liệu từ PostgreSQL sang Elasticsearch...");

        long sqlCount = jpaRepository.count();
        long esCount = searchRepository.count();

        if (esCount != sqlCount) {
            log.warn("Phát hiện không đồng bộ! SQL: {}, Elasticsearch: {}. Đang tiến hành đồng bộ...", sqlCount, esCount);
            
            searchRepository.deleteAll();
            
            int batchSize = 1000; // Xử lý 1000 bản ghi mỗi lần
            int pageNo = 0;
            Page<Product> page;
            
            do {
                // Đọc từng trang
                page = jpaRepository.findAll(PageRequest.of(pageNo, batchSize));
                
                if (page.hasContent()) {
                    // Ghi vào Elastic
                    searchRepository.saveAll(page.getContent());
                    log.info("Đã đồng bộ batch {}/{} ({} bản ghi)", pageNo + 1, page.getTotalPages(), page.getNumberOfElements());
                    
                    // QUAN TRỌNG: Giải phóng bộ nhớ Hibernate sau mỗi batch
                    entityManager.flush();
                    entityManager.clear();
                }
                pageNo++;
            } while (page.hasNext());

            log.info("Đồng bộ hoàn tất! Tổng cộng {} sản phẩm.", sqlCount);
        } else {
            log.info("Dữ liệu đã đồng bộ. ({} sản phẩm)", esCount);
        }
    }
}