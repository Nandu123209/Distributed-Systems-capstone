package com.Product.application.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

	 @ExceptionHandler(ProductNotFoundException.class)
	    public ResponseEntity<Void> handleProductNotFound(ProductNotFoundException ex) {
	              return ResponseEntity.notFound().build(); 
	    }
}
