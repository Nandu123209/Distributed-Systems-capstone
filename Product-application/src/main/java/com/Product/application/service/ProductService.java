package com.Product.application.service;

import com.Product.application.client.CatalogClient;
import com.Product.application.client.InventoryClient;
import com.Product.application.dto.Product;
import com.Product.application.dto.ProductAvailability;
import com.Product.application.dto.ProductResponse;
import com.Product.application.exception.ProductNotFoundException;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;

    public ProductService(CatalogClient catalogClient, InventoryClient inventoryClient) {
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
    }

    // 🔹 Product by ID
    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallbackProductByUniqId")
    public ProductResponse getProductByUniqId(String uniqId) {
        if (uniqId == null || uniqId.trim().isEmpty()) {
            log.warn("uniqId is null or empty");
            throw new ProductNotFoundException("Invalid or missing uniqId");
        }

        log.info("Fetching product details for ID: {}", uniqId);

        Product product = catalogClient.getProductByUniqId(uniqId);

        if (product == null || product.getUniqId() == null || product.getSku() == null) {
            log.warn("Invalid product data returned for ID: {}", uniqId);
            throw new ProductNotFoundException("Incomplete product data for ID: " + uniqId);
        }

        ProductAvailability availability = getAvailabilityWithFallback(uniqId);
        return new ProductResponse(product, availability);
    }

    // 🔹 Products by SKU
    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallbackProductsBySku")
    public List<ProductResponse> getProductsBySku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            log.warn("SKU is null or empty");
            throw new ProductNotFoundException("Invalid or missing SKU");
        }

        log.info("Fetching products for SKU: {}", sku);
        List<Product> products = catalogClient.getProductsBySku(sku);

        if (products == null || products.isEmpty()) {
            log.info("No products found for SKU: {}", sku);
            return Collections.emptyList();
        }

        return products.stream()
                .filter(product -> product != null && product.getUniqId() != null && product.getSku() != null)
                .map(product -> {
                    ProductAvailability availability = getAvailabilityWithFallback(product.getUniqId());
                    return new ProductResponse(product, availability);
                })
                .collect(Collectors.toList());
    }
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackAvailability")
    private ProductAvailability getAvailabilityWithFallback(String productId) {
        log.debug("Fetching availability for product ID: {}", productId);
        return inventoryClient.getAvailability(productId);
    }

    public ProductAvailability fallbackAvailability(String productId, Throwable t) {
        log.warn("Inventory fallback for ID: {}. Reason: {}", productId, t.getMessage());
        return new ProductAvailability(productId, "AVAILABILITY_UNKNOWN");
    }
    public ProductResponse fallbackProductByUniqId(String uniqId, Throwable throwable) {
        if (throwable instanceof FeignException.NotFound) {
            log.warn("Catalog fallback triggered - product not found for ID: {}", uniqId);
            return new ProductResponse(
                    Product.empty(),
                    new ProductAvailability(uniqId, "PRODUCT_NOT_FOUND_IN_FALLBACK")
            );
        }

        log.error("Catalog fallback triggered due to error for ID: {}. Reason: {}", uniqId, throwable.getMessage());
        return new ProductResponse(
                Product.empty(),
                new ProductAvailability(uniqId, "PRODUCT_SERVICE_UNAVAILABLE")
        );
    }

    // 🔹 Fallback for SKU-based lookup
    public List<ProductResponse> fallbackProductsBySku(String sku, Throwable throwable) {
        log.warn("Catalog fallback triggered for SKU [{}]. Reason: {}", sku, throwable.getMessage());
        return Collections.emptyList();
    }
}
