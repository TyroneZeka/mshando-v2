package com.mshando.biddingservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Main configuration class for the Bidding Service.
 * 
 * Configures essential beans and application-wide settings
 * for the bidding service including REST templates and
 * business rule configurations.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Configuration
public class BiddingServiceConfig {

    /**
     * Configure RestTemplate for external service calls
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Configuration properties for bidding business rules
     */
    @Bean
    @ConfigurationProperties(prefix = "bidding")
    public BiddingProperties biddingProperties() {
        return new BiddingProperties();
    }

    /**
     * Configuration properties for external services
     */
    @Bean
    @ConfigurationProperties(prefix = "services")
    public ServiceUrlProperties serviceUrlProperties() {
        return new ServiceUrlProperties();
    }

    /**
     * Bidding business rule properties
     */
    public static class BiddingProperties {
        private int maxBidsPerTask = 10;
        private String minBidAmount = "5.00";
        private AutoAccept autoAccept = new AutoAccept();

        // Getters and setters
        public int getMaxBidsPerTask() {
            return maxBidsPerTask;
        }

        public void setMaxBidsPerTask(int maxBidsPerTask) {
            this.maxBidsPerTask = maxBidsPerTask;
        }

        public String getMinBidAmount() {
            return minBidAmount;
        }

        public void setMinBidAmount(String minBidAmount) {
            this.minBidAmount = minBidAmount;
        }

        public AutoAccept getAutoAccept() {
            return autoAccept;
        }

        public void setAutoAccept(AutoAccept autoAccept) {
            this.autoAccept = autoAccept;
        }

        public static class AutoAccept {
            private boolean enabled = false;
            private int thresholdHours = 24;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public int getThresholdHours() {
                return thresholdHours;
            }

            public void setThresholdHours(int thresholdHours) {
                this.thresholdHours = thresholdHours;
            }
        }
    }

    /**
     * External service URL properties
     */
    public static class ServiceUrlProperties {
        private Service userService = new Service();
        private Service taskService = new Service();

        public Service getUserService() {
            return userService;
        }

        public void setUserService(Service userService) {
            this.userService = userService;
        }

        public Service getTaskService() {
            return taskService;
        }

        public void setTaskService(Service taskService) {
            this.taskService = taskService;
        }

        public static class Service {
            private String url;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }
    }
}
