package com.mshando.biddingservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Cache configuration for the Bidding Service.
 * 
 * Configures caching for frequently accessed data like
 * user information, task details, and bid statistics.
 * Uses in-memory caching by default with configurable TTL.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "mshando.bidding.cache.enabled", havingValue = "true", matchIfMissing = true)
public class CacheConfig {

    /**
     * Configure the cache manager for bid-related operations.
     * Uses simple in-memory caching for development and testing.
     * 
     * In production, consider Redis or Hazelcast for distributed caching.
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Define cache names for different data types
        cacheManager.setCacheNames(Arrays.asList(
            "userCache",           // User information cache
            "taskCache",           // Task details cache
            "bidStatsCache",       // Bid statistics cache
            "configCache"          // Configuration cache
        ));
        
        // Allow dynamic cache creation
        cacheManager.setAllowNullValues(false);
        
        return cacheManager;
    }

    /**
     * Cache configuration properties.
     */
    @ConfigurationProperties(prefix = "mshando.bidding.cache")
    public static class CacheProperties {
        
        /**
         * Whether caching is enabled
         */
        private boolean enabled = true;
        
        /**
         * Default TTL for cache entries in seconds
         */
        private long defaultTtl = 3600; // 1 hour
        
        /**
         * TTL for user information cache in seconds
         */
        private long userCacheTtl = 1800; // 30 minutes
        
        /**
         * TTL for task details cache in seconds
         */
        private long taskCacheTtl = 900; // 15 minutes
        
        /**
         * TTL for bid statistics cache in seconds
         */
        private long bidStatsCacheTtl = 300; // 5 minutes
        
        /**
         * Maximum cache size for each cache
         */
        private int maxCacheSize = 1000;
        
        /**
         * Cache type (simple, redis, hazelcast)
         */
        private String cacheType = "simple";

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getDefaultTtl() {
            return defaultTtl;
        }

        public void setDefaultTtl(long defaultTtl) {
            this.defaultTtl = defaultTtl;
        }

        public long getUserCacheTtl() {
            return userCacheTtl;
        }

        public void setUserCacheTtl(long userCacheTtl) {
            this.userCacheTtl = userCacheTtl;
        }

        public long getTaskCacheTtl() {
            return taskCacheTtl;
        }

        public void setTaskCacheTtl(long taskCacheTtl) {
            this.taskCacheTtl = taskCacheTtl;
        }

        public long getBidStatsCacheTtl() {
            return bidStatsCacheTtl;
        }

        public void setBidStatsCacheTtl(long bidStatsCacheTtl) {
            this.bidStatsCacheTtl = bidStatsCacheTtl;
        }

        public int getMaxCacheSize() {
            return maxCacheSize;
        }

        public void setMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
        }

        public String getCacheType() {
            return cacheType;
        }

        public void setCacheType(String cacheType) {
            this.cacheType = cacheType;
        }
    }
}
