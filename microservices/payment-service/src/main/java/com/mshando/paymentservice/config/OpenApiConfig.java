package com.mshando.paymentservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 configuration for Payment Service.
 * 
 * Provides comprehensive API documentation with security schemes,
 * detailed endpoint descriptions, and interactive testing capabilities.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:Mshando Payment Service - Comprehensive payment processing for the Mshando platform}")
    private String appDescription;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    /**
     * Configures OpenAPI documentation for Payment Service.
     */
    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("💳 Mshando Payment Service API")
                        .description("""
                                ## 🚀 Comprehensive Payment Processing Service
                                
                                The **Mshando Payment Service** provides robust, secure payment processing capabilities for the Mshando platform.
                                
                                ### 🎯 Key Features
                                - **💰 Multi-Payment Support**: Credit cards, digital wallets, bank transfers
                                - **🔄 Automated Processing**: Asynchronous payment handling with retry logic
                                - **💸 Refund Management**: Full and partial refund processing
                                - **📊 Financial Analytics**: Revenue tracking and financial reporting
                                - **🔒 Security First**: JWT authentication and encrypted data handling
                                - **⚡ High Performance**: Optimized for scale with caching and async processing
                                
                                ### 🏗️ Architecture
                                - **Microservice Design**: Independent, scalable service
                                - **Event-Driven**: Real-time status updates and notifications
                                - **External Integration**: Stripe payment provider support
                                - **Database Optimization**: PostgreSQL with performance tuning
                                
                                ### 📋 Business Rules
                                - Minimum payment: **$0.01**
                                - Maximum payment: **$100,000**
                                - Service fee: **10%** of payment amount
                                - Maximum retry attempts: **3**
                                - Refund window: **90 days**
                                
                                ### 🔗 Integration Guide
                                1. **Authentication**: Include JWT token in Authorization header
                                2. **Create Payment**: POST to `/api/v1/payments`
                                3. **Monitor Status**: Use webhooks or polling for status updates
                                4. **Handle Responses**: Process success/error responses appropriately
                                
                                ### 🆘 Support
                                - **Documentation**: Comprehensive API docs with examples
                                - **Testing**: Interactive swagger UI for API testing
                                - **Monitoring**: Health checks and performance metrics
                                """)
                        .version(appVersion)
                        .contact(new Contact()
                                .name("Mshando Development Team")
                                .email("dev@mshando.com")
                                .url("https://mshando.com/contact"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8083" + contextPath)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api-dev.mshando.com/payment-service" + contextPath)
                                .description("Development Environment"),
                        new Server()
                                .url("https://api-staging.mshando.com/payment-service" + contextPath)
                                .description("Staging Environment"),
                        new Server()
                                .url("https://api.mshando.com/payment-service" + contextPath)
                                .description("Production Environment")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer token authentication. Include your JWT token in the Authorization header.")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"));
    }
}
