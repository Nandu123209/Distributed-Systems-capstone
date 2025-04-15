package com.Product.application.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.Product.application.dto.Product;

@FeignClient(name = "Catalog-Service")
public interface CatalogClient {
    @GetMapping("/catalog/{uniqId}")
    Product getProductByUniqId(@PathVariable String uniqId);

    @GetMapping("/catalog/sku/{sku}")
    List<Product> getProductsBySku(@PathVariable String sku);

}
