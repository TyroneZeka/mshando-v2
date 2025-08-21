package com.mshando.biddingservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

/**
 * Asynchronous and scheduling configuration for the Bidding Service.
 * 
 * Configures thread pools for async operations and scheduled tasks
 * including auto-acceptance of old bids, performance monitoring,
 * and external service calls.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    /**
     * Thread pool executor for asynchronous bid operations.
     * Used for external service calls, notifications, and 
     * non-blocking operations.
     */
    @Bean(name = "bidTaskExecutor")
    public Executor bidTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("bid-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Thread pool executor for external service integration.
     * Dedicated pool for user service and task service calls
     * to prevent blocking main bid operations.
     */
    @Bean(name = "externalServiceExecutor")
    public Executor externalServiceExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(15);
        executor.setThreadNamePrefix("external-service-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(20);
        executor.initialize();
        return executor;
    }

    /**
     * Task scheduler for scheduled operations like auto-acceptance
     * of old bids, cleanup tasks, and performance monitoring.
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(3);
        scheduler.setThreadNamePrefix("bid-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        return scheduler;
    }

    /**
     * Configuration properties for async operations.
     */
    @ConfigurationProperties(prefix = "mshando.bidding.async")
    public static class AsyncProperties {
        
        /**
         * Core pool size for bid operations
         */
        private int bidCorePoolSize = 5;
        
        /**
         * Maximum pool size for bid operations
         */
        private int bidMaxPoolSize = 10;
        
        /**
         * Queue capacity for bid operations
         */
        private int bidQueueCapacity = 25;
        
        /**
         * Core pool size for external service calls
         */
        private int externalServiceCorePoolSize = 3;
        
        /**
         * Maximum pool size for external service calls
         */
        private int externalServiceMaxPoolSize = 8;
        
        /**
         * Queue capacity for external service calls
         */
        private int externalServiceQueueCapacity = 15;
        
        /**
         * Pool size for scheduled tasks
         */
        private int schedulerPoolSize = 3;
        
        /**
         * Shutdown timeout in seconds
         */
        private int shutdownTimeoutSeconds = 30;

        // Getters and setters
        public int getBidCorePoolSize() {
            return bidCorePoolSize;
        }

        public void setBidCorePoolSize(int bidCorePoolSize) {
            this.bidCorePoolSize = bidCorePoolSize;
        }

        public int getBidMaxPoolSize() {
            return bidMaxPoolSize;
        }

        public void setBidMaxPoolSize(int bidMaxPoolSize) {
            this.bidMaxPoolSize = bidMaxPoolSize;
        }

        public int getBidQueueCapacity() {
            return bidQueueCapacity;
        }

        public void setBidQueueCapacity(int bidQueueCapacity) {
            this.bidQueueCapacity = bidQueueCapacity;
        }

        public int getExternalServiceCorePoolSize() {
            return externalServiceCorePoolSize;
        }

        public void setExternalServiceCorePoolSize(int externalServiceCorePoolSize) {
            this.externalServiceCorePoolSize = externalServiceCorePoolSize;
        }

        public int getExternalServiceMaxPoolSize() {
            return externalServiceMaxPoolSize;
        }

        public void setExternalServiceMaxPoolSize(int externalServiceMaxPoolSize) {
            this.externalServiceMaxPoolSize = externalServiceMaxPoolSize;
        }

        public int getExternalServiceQueueCapacity() {
            return externalServiceQueueCapacity;
        }

        public void setExternalServiceQueueCapacity(int externalServiceQueueCapacity) {
            this.externalServiceQueueCapacity = externalServiceQueueCapacity;
        }

        public int getSchedulerPoolSize() {
            return schedulerPoolSize;
        }

        public void setSchedulerPoolSize(int schedulerPoolSize) {
            this.schedulerPoolSize = schedulerPoolSize;
        }

        public int getShutdownTimeoutSeconds() {
            return shutdownTimeoutSeconds;
        }

        public void setShutdownTimeoutSeconds(int shutdownTimeoutSeconds) {
            this.shutdownTimeoutSeconds = shutdownTimeoutSeconds;
        }
    }
}
