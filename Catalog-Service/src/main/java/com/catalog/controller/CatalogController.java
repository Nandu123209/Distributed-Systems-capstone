package com.catalog.controller;


import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catalog.dto.Product;
import com.catalog.service.CatalogServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {
	
	private final CatalogServiceImpl productService;

    @GetMapping("/{uniqId}")
    public ResponseEntity<Product> getProductById(@PathVariable String uniqId) {
        Product product = productService.getProductById(uniqId);
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<List<Product>> getProductsBySku(@PathVariable String sku) {
        List<Product> products = productService.getProductsBySku(sku);
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

}
