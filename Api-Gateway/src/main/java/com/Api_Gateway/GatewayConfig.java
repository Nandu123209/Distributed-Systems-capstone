package com.Api_Gateway;


import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("Catalog-Service-Application", r -> r.path("/catalog/**")
                        .uri("lb://Catalog-Service-Application"))
                .route("Inventory-Application", r -> r.path("/inventory/**")
                        .uri("lb://Inventory-Application"))
                .route("Product-application", r -> r.path("/products/**")
                        .uri("lb://Product-application"))
                .build();
    }
    
    @Bean
    public RouteLocator fallbackRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("fallback-route", r -> r.path("/fallback")
                        .filters(f -> f.rewritePath("/fallback", "/error"))
                        .uri("no://op"))
                .build();
    }
}

