package com.mshando.paymentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Main configuration class for the Payment Service.
 * 
 * Configures essential beans and application-wide settings
 * for the payment service including REST templates and
 * business rule configurations.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Configuration
public class HttpClientConfig {

    /**
     * Configure RestTemplate for external service calls
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Configuration properties for payment business rules
     */
    @Bean
    @ConfigurationProperties(prefix = "payment")
    public PaymentProperties paymentProperties() {
        return new PaymentProperties();
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
     * Payment business rule properties
     */
    public static class PaymentProperties {
        private Providers providers = new Providers();
        private Business business = new Business();
        private Processing processing = new Processing();

        // Getters and setters
        public Providers getProviders() {
            return providers;
        }

        public void setProviders(Providers providers) {
            this.providers = providers;
        }

        public Business getBusiness() {
            return business;
        }

        public void setBusiness(Business business) {
            this.business = business;
        }

        public Processing getProcessing() {
            return processing;
        }

        public void setProcessing(Processing processing) {
            this.processing = processing;
        }

        public static class Providers {
            private Stripe stripe = new Stripe();

            public Stripe getStripe() {
                return stripe;
            }

            public void setStripe(Stripe stripe) {
                this.stripe = stripe;
            }

            public static class Stripe {
                private String secretKey;
                private String publicKey;
                private String webhookSecret;
                private String apiVersion = "2023-10-16";
                private int timeout = 30000;
                private int maxRetries = 3;

                // Getters and setters
                public String getSecretKey() {
                    return secretKey;
                }

                public void setSecretKey(String secretKey) {
                    this.secretKey = secretKey;
                }

                public String getPublicKey() {
                    return publicKey;
                }

                public void setPublicKey(String publicKey) {
                    this.publicKey = publicKey;
                }

                public String getWebhookSecret() {
                    return webhookSecret;
                }

                public void setWebhookSecret(String webhookSecret) {
                    this.webhookSecret = webhookSecret;
                }

                public String getApiVersion() {
                    return apiVersion;
                }

                public void setApiVersion(String apiVersion) {
                    this.apiVersion = apiVersion;
                }

                public int getTimeout() {
                    return timeout;
                }

                public void setTimeout(int timeout) {
                    this.timeout = timeout;
                }

                public int getMaxRetries() {
                    return maxRetries;
                }

                public void setMaxRetries(int maxRetries) {
                    this.maxRetries = maxRetries;
                }
            }
        }

        public static class Business {
            private double serviceFeePercentage = 10.0;
            private double minimumPayment = 0.01;
            private double maximumPayment = 100000.00;
            private String defaultCurrency = "USD";

            // Getters and setters
            public double getServiceFeePercentage() {
                return serviceFeePercentage;
            }

            public void setServiceFeePercentage(double serviceFeePercentage) {
                this.serviceFeePercentage = serviceFeePercentage;
            }

            public double getMinimumPayment() {
                return minimumPayment;
            }

            public void setMinimumPayment(double minimumPayment) {
                this.minimumPayment = minimumPayment;
            }

            public double getMaximumPayment() {
                return maximumPayment;
            }

            public void setMaximumPayment(double maximumPayment) {
                this.maximumPayment = maximumPayment;
            }

            public String getDefaultCurrency() {
                return defaultCurrency;
            }

            public void setDefaultCurrency(String defaultCurrency) {
                this.defaultCurrency = defaultCurrency;
            }
        }

        public static class Processing {
            private boolean asyncEnabled = true;
            private int batchSize = 50;
            private boolean retryEnabled = true;
            private boolean webhookEnabled = true;

            // Getters and setters
            public boolean isAsyncEnabled() {
                return asyncEnabled;
            }

            public void setAsyncEnabled(boolean asyncEnabled) {
                this.asyncEnabled = asyncEnabled;
            }

            public int getBatchSize() {
                return batchSize;
            }

            public void setBatchSize(int batchSize) {
                this.batchSize = batchSize;
            }

            public boolean isRetryEnabled() {
                return retryEnabled;
            }

            public void setRetryEnabled(boolean retryEnabled) {
                this.retryEnabled = retryEnabled;
            }

            public boolean isWebhookEnabled() {
                return webhookEnabled;
            }

            public void setWebhookEnabled(boolean webhookEnabled) {
                this.webhookEnabled = webhookEnabled;
            }
        }
    }

    /**
     * External service URL properties
     */
    public static class ServiceUrlProperties {
        private Service userService = new Service();
        private Service taskService = new Service();
        private Service notificationService = new Service();

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

        public Service getNotificationService() {
            return notificationService;
        }

        public void setNotificationService(Service notificationService) {
            this.notificationService = notificationService;
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
