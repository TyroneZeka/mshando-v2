package com.mshando.paymentservice.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration for Payment Service.
 * 
 * Provides caching configuration for frequently accessed payment data
 * to improve performance and reduce database load.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache manager for payment service caching.
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Define cache names for different payment data
        cacheManager.setCacheNames(java.util.Arrays.asList(
            "payments",           // Individual payment cache
            "customerPayments",   // Customer payment summaries
            "taskerEarnings",     // Tasker earning summaries
            "serviceFees",        // Service fee calculations
            "paymentStats"        // Payment statistics
        ));
        
        // Allow creation of additional caches at runtime
        cacheManager.setAllowNullValues(false);
        
        return cacheManager;
    }
}
