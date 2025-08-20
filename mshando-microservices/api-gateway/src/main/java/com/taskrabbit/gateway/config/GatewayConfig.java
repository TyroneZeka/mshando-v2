package com.taskrabbit.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway routing configuration
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Configuration
public class GatewayConfig {

    /**
     * Configure routes for microservices
     * 
     * @param builder RouteLocatorBuilder
     * @return RouteLocator with configured routes
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // User Service routes
            .route("user-service", r -> r.path("/api/users/**", "/api/auth/**")
                .uri("lb://user-service"))
            
            // Task Service routes
            .route("task-service", r -> r.path("/api/tasks/**", "/api/categories/**")
                .uri("lb://task-service"))
            
            // Bidding Service routes
            .route("bidding-service", r -> r.path("/api/bids/**")
                .uri("lb://bidding-service"))
            
            // Payment Service routes
            .route("payment-service", r -> r.path("/api/payments/**", "/api/wallet/**")
                .uri("lb://payment-service"))
            
            // Notification Service routes
            .route("notification-service", r -> r.path("/api/notifications/**")
                .uri("lb://notification-service"))
            
            // Review Service routes
            .route("review-service", r -> r.path("/api/reviews/**", "/api/ratings/**")
                .uri("lb://review-service"))
            
            .build();
    }
}
