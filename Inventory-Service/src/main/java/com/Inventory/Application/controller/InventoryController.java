package com.Inventory.Application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Inventory.Application.dto.ProductAvailability;
import com.Inventory.Application.service.InventoryService;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping("/inventory")
@AllArgsConstructor
public class InventoryController {
	
	
    private final InventoryService inventoryService;

  
    @GetMapping("/{uniqId}")
    public ResponseEntity<ProductAvailability> getAvailability(@PathVariable String uniqId) {
        ProductAvailability availability = inventoryService.getAvailability(uniqId);
        return ResponseEntity.status(HttpStatus.OK).body(availability);
}
}


