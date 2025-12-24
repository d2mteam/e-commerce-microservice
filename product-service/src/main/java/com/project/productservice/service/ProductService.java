package com.project.productservice.service;

// 1. IMPORT DTO & ENTITY
import com.project.productservice.dto.ProductSearchRequest;
import com.project.productservice.dto.ProductSearchResponse;
import com.project.productservice.entity.Product;
import com.project.productservice.repository.ProductRepository;
import com.project.productservice.repository.ProductSearchRepository;

// 2. IMPORT SPRING CORE
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

// 3. IMPORT ELASTICSEARCH CLIENT (co.elastic.clients.*)
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery;

// 4. IMPORT SPRING DATA ELASTICSEARCH (org.springframework.data.elasticsearch.*)
// Lưu ý: Import chính xác các class này
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import co.elastic.clients.json.JsonData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    // ==================== CRUD (Giữ nguyên) ====================

    @Transactional
    public Product createProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        searchRepository.save(savedProduct);
        return savedProduct;
    }

    @Transactional
    public Product updateProduct(UUID id, Product productDetails) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        existingProduct.setProductName(productDetails.getProductName());
        existingProduct.setProductPrice(productDetails.getProductPrice());
        existingProduct.setProductDescription(productDetails.getProductDescription());
        existingProduct.setProductImages(productDetails.getProductImages());
        existingProduct.setAttributes(productDetails.getAttributes());
        existingProduct.setCategoryName(productDetails.getCategoryName());

        Product updatedProduct = productRepository.save(existingProduct);
        searchRepository.save(updatedProduct);
        return updatedProduct;
    }

    @Transactional
    public void deleteProduct(UUID id) {
        productRepository.deleteById(id);
        searchRepository.deleteById(id);
    }

    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public List<Product> searchProductsByName(String keyword) {
        return searchRepository.findByProductNameContaining(keyword);
    }

    public List<UUID> searchProductIdsByName(String keyword) {
        return productRepository.findProductIdsByNameContaining(keyword);
    }

    public Optional<Product> getProductByName(String productName) {
        return productRepository.findByProductName(productName);
    }

    /**
     * TÌM KIẾM NÂNG CAO VỚI FILTERS + FACETS
     * - Filter: keyword, category, price range, attributes
     * - Facet: aggregation counts cho category
     */
    public ProductSearchResponse searchWithFiltersAndFacets(ProductSearchRequest request) {

        // 1. Build BoolQuery cho filters
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 1.1 Filter theo keyword (full-text search trên productName và
        // productDescription)
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            boolQueryBuilder.must(m -> m
                    .multiMatch(mm -> mm
                            .query(request.getKeyword())
                            .fields("productName", "productDescription")));
        }

        // 1.2 Filter theo category (exact match)
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            boolQueryBuilder.filter(f -> f
                    .term(t -> t
                            .field("categoryName")
                            .value(request.getCategory())));
        }

        // 1.3 Filter theo price range
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            NumberRangeQuery.Builder rangeBuilder = new NumberRangeQuery.Builder()
                    .field("productPrice");
            if (request.getMinPrice() != null) {
                rangeBuilder.gte(request.getMinPrice());
            }
            if (request.getMaxPrice() != null) {
                rangeBuilder.lte(request.getMaxPrice());
            }
            boolQueryBuilder.filter(f -> f.range(r -> r.number(rangeBuilder.build())));
        }

        // 1.4 Filter theo dynamic attributes
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            for (Map.Entry<String, String> attr : request.getAttributes().entrySet()) {
                boolQueryBuilder.filter(f -> f
                        .term(t -> t
                                .field("attributes." + attr.getKey() + ".keyword")
                                .value(attr.getValue())));
            }
        }

        // 2. Build NativeQuery với pagination, sorting, và aggregation
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQueryBuilder.build()))
                .withPageable(PageRequest.of(request.getPage(), request.getSize()))
                .withAggregation("category_facet", Aggregation.of(a -> a
                        .terms(t -> t.field("categoryName").size(50))))
                .build();

        // 3. Execute query
        SearchHits<Product> searchHits = elasticsearchOperations.search(query, Product.class);

        // 4. Extract products
        List<Product> products = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        // 5. Extract facets (aggregations)
        Map<String, Map<String, Long>> facets = new HashMap<>();

        if (searchHits.getAggregations() != null) {
            ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();

            ElasticsearchAggregation categoryAggregation = aggregations.aggregationsAsMap().get("category_facet");
            if (categoryAggregation != null) {
                Map<String, Long> categoryFacet = new HashMap<>();
                List<StringTermsBucket> buckets = categoryAggregation.aggregation()
                        .getAggregate()
                        .sterms()
                        .buckets()
                        .array();

                for (StringTermsBucket bucket : buckets) {
                    categoryFacet.put(bucket.key().stringValue(), bucket.docCount());
                }
                facets.put("category", categoryFacet);
            }
        }

        // 6. Build response
        long totalElements = searchHits.getTotalHits();
        int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

        return ProductSearchResponse.builder()
                .products(products)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .facets(facets)
                .build();
    }

    /**
     * AUTOCOMPLETE: Gợi ý tên sản phẩm theo prefix
     * @param prefix Ký tự người dùng đã gõ
     * @param limit Số gợi ý tối đa (mặc định 10)
     * @return Danh sách tên sản phẩm gợi ý
     */
    public List<String> getAutocompleteSuggestions(String prefix, int limit) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .prefix(p -> p
                                .field("productName")
                                .value(prefix.toLowerCase())
                        )
                )
                .withPageable(PageRequest.of(0, limit))
                .build();
        SearchHits<Product> searchHits = elasticsearchOperations.search(query, Product.class);
        return searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getProductName())
                .distinct()
                .collect(Collectors.toList());
    }

}