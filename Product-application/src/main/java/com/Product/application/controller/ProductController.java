package com.Product.application.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Product.application.dto.ProductResponse;
import com.Product.application.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

   

    @GetMapping("/{uniqId}")
    public ResponseEntity<ProductResponse> getProductByUniqId(@PathVariable String uniqId) {
        ProductResponse product = productService.getProductByUniqId(uniqId);
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<List<ProductResponse>> getProductsBySku(@PathVariable String sku) {
        List<ProductResponse> products = productService.getProductsBySku(sku);
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }
}


