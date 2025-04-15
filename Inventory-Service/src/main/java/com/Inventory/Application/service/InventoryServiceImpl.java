package com.Inventory.Application.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Inventory.Application.dto.AppConstants;
import com.Inventory.Application.dto.Product;
import com.Inventory.Application.dto.ProductAvailability;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private static final String[] AVAILABILITY_STATUSES = {"IN_STOCK", "LIMITED_STOCK", "OUT_OF_STOCK"};

    private final Map<String, Product> productMap = new HashMap<>();
    private final Map<String, ProductAvailability> availabilityMap = new HashMap<>();

    @PostConstruct
    public void loadProductData() {
        try (
            Reader reader = new InputStreamReader(
                    Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("jcpenney_com-ecommerce_sample.csv"))
            );
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())
        ) {
            for (CSVRecord record : csvParser) {
                String uniqId = record.get("uniq_id").trim();
                if (!uniqId.isEmpty()) {
                    Product product = new Product(
                            uniqId,
                            record.get(AppConstants.UNIQ_ID),
                            record.get(AppConstants.NAME),
                            record.get(AppConstants.DESCRIPTION),
                            record.get(AppConstants.LIST_PRICE),
                            record.get(AppConstants.SALE_PRICE),
                            record.get(AppConstants.CATEGORY),
                            record.get(AppConstants.CATEGORY_TREE),
                            record.get(AppConstants.AVERAGE_PRODUCT_RATING),
                            record.get(AppConstants.PRODUCT_URL),
                            record.get(AppConstants.PRODUCT_IMAGE_URLS),
                            record.get(AppConstants.BRAND),
                            record.get(AppConstants.TOTAL_NUMBER_REVIEWS)
                    );

                    productMap.put(uniqId, product);

                    String randomAvailability = AVAILABILITY_STATUSES[new Random().nextInt(AVAILABILITY_STATUSES.length)];
                    availabilityMap.put(uniqId, new ProductAvailability(uniqId, randomAvailability));
                }
            }

            log.info(" Product inventory loaded successfully. Total products: {}", productMap.size());

        } catch (Exception e) {
            log.error(" Error loading product data from CSV: {}", e.getMessage(), e);
        }
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackAvailability")
    public ProductAvailability getAvailability(String uniqId) {
        if (uniqId == null || uniqId.trim().isEmpty()) {
            log.warn("Received empty uniqId for availability check");
            return new ProductAvailability("UNKNOWN_ID", "INVALID_ID");
        }

        uniqId = uniqId.trim();
        ProductAvailability availability = availabilityMap.get(uniqId);

        if (availability != null) {
            log.info(" Product availability found: {} → {}", uniqId, availability.getStatus());
            return availability;
        } else {
            log.warn(" Product not found in inventory: {}", uniqId);
            return new ProductAvailability(uniqId, "PRODUCT_NOT_FOUND");
        }
    }

    public ProductAvailability fallbackAvailability(String uniqId, Throwable throwable) {
        log.error(" Circuit Breaker triggered for getAvailability [{}]. Reason: {}", uniqId, throwable.getMessage());
        return new ProductAvailability(uniqId, "INVENTORY_SERVICE_UNAVAILABLE");
    }
}
