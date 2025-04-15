package com.Product.application.client;



import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.Product.application.dto.ProductAvailability;

import java.util.List;
import java.util.Map;

@FeignClient(name = "Inventory-Service")
public interface InventoryClient {
    @GetMapping("/inventory/{uniqId}")
    ProductAvailability getAvailability(@PathVariable String uniqId);
}
