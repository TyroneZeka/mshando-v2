package com.mshando.biddingservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for the Bidding Service.
 * 
 * NOTE: CORS configuration is disabled here as it's handled by the API Gateway.
 * This prevents duplicate CORS headers that would cause browser errors.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * CORS configuration disabled - handled by API Gateway
     * This prevents duplicate Access-Control-Allow-Origin headers
     */
    /*
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
                
        registry.addMapping("/actuator/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET")
                .allowedHeaders("*")
                .maxAge(3600);
    }
    */
}
