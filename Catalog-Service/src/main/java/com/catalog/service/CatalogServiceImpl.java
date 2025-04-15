package com.catalog.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.catalog.dto.AppConstants;
import com.catalog.dto.Product;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CatalogServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(CatalogServiceImpl.class);

    private final Map<String, Product> productMap = new HashMap<>();

    @PostConstruct
    public void loadProductData() {
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("jcpenney_com-ecommerce_sample.csv")),
                StandardCharsets.UTF_8)) {

            CSVParser csvParser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withTrim()
                    .parse(reader);

            for (CSVRecord record : csvParser) {
                String uniqId = record.get(AppConstants.UNIQ_ID).trim();
                if (uniqId.isEmpty()) continue;

                Product product = Product.builder()
                        .uniqId(uniqId)
                        .sku(record.get(AppConstants.SKU))
                        .name(record.get(AppConstants.NAME))
                        .description(record.get(AppConstants.DESCRIPTION))
                        .listPrice(record.get(AppConstants.LIST_PRICE))
                        .salePrice(record.get(AppConstants.SALE_PRICE))
                        .category(record.get(AppConstants.CATEGORY))
                        .categoryTree(record.get(AppConstants.CATEGORY_TREE))
                        .averageProductRating(record.get(AppConstants.AVERAGE_PRODUCT_RATING))
                        .productUrl(record.get(AppConstants.PRODUCT_URL))
                        .productImageUrls(record.get(AppConstants.PRODUCT_IMAGE_URLS))
                        .brand(record.get(AppConstants.BRAND))
                        .totalNumberReviews(record.get(AppConstants.TOTAL_NUMBER_REVIEWS))
                        .build();

                productMap.put(uniqId, product);
            }

            log.info(" Loaded {} products into catalog.", productMap.size());

        } catch (Exception e) {
            log.error(" Failed to load product data: {}", e.getMessage(), e);
        }
    }

    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallbackGetProductById")
    public Product getProductById(String uniqId) {
        log.info(" Fetching product by ID: {}", uniqId);
        return productMap.get(uniqId.trim());
    }

    public Product fallbackGetProductById(String uniqId, Throwable ex) {
        log.warn(" Fallback for getProductById [{}]: {}", uniqId, ex.getMessage());
        return Product.builder()
                .uniqId(uniqId)
                .name("Fallback Product")
                .description("Service unavailable")
                .build();
    }

    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallbackGetProductsBySku")
    public List<Product> getProductsBySku(String sku) {
        log.info(" Searching for products by SKU: {}", sku);
        return productMap.values().stream()
                .filter(product -> sku.equalsIgnoreCase(product.getSku()))
                .collect(Collectors.toList());
    }

    public List<Product> fallbackGetProductsBySku(String sku, Throwable ex) {
        log.warn("️ Fallback for getProductsBySku [{}]: {}", sku, ex.getMessage());
        return Collections.emptyList();
    }
}
