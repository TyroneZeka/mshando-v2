package com.mshando.biddingservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) configuration for the Bidding Service.
 * 
 * Configures API documentation with security schemes,
 * contact information, and comprehensive API descriptions
 * for developers and integration partners.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI biddingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mshando Bidding Service API")
                        .description("""
                                ## 🎯 Comprehensive Bidding Service
                                
                                Welcome to the **Mshando Bidding Service API** - your complete solution for managing task bids in the Mshando marketplace platform.
                                
                                ### ✨ Core Features
                                
                                - **📝 Bid Management**: Create, update, accept, reject, and withdraw bids
                                - **📊 Status Tracking**: Complete lifecycle management of bid statuses
                                - **📄 Pagination**: Efficient retrieval of large bid datasets
                                - **✅ Validation**: Business rule enforcement and data validation
                                - **🔗 Integration**: Seamless integration with task and user services
                                - **📈 Statistics**: Comprehensive bidding analytics and metrics
                                
                                ### 🔒 Business Rules
                                
                                | Rule | Description |
                                |------|-------------|
                                | **One Bid Per Task** | Taskers can only place one bid per task |
                                | **Pending Only Updates** | Only pending bids can be modified |
                                | **Owner Permissions** | Customers can accept/reject bids for their tasks |
                                | **Withdrawal Rights** | Taskers can withdraw their own pending or accepted bids |
                                | **Auto-Acceptance** | Configurable auto-acceptance for old pending bids |
                                | **Amount Limits** | Minimum $5.00, Maximum $10,000.00 |
                                | **Time Limits** | 1-720 hours estimated completion time |
                                
                                ### 🔐 Authentication
                                
                                All endpoints require **JWT authentication** except health checks and documentation.
                                Include the JWT token in the Authorization header:
                                
                                ```
                                Authorization: Bearer <your-jwt-token>
                                ```
                                
                                ### 📋 Bid Status Lifecycle
                                
                                ```
                                PENDING → ACCEPTED → COMPLETED
                                    ↓         ↓         ↓
                                REJECTED  CANCELLED  WITHDRAWN
                                ```
                                
                                ### 🚀 Getting Started
                                
                                1. **Authenticate** - Obtain JWT token from auth service
                                2. **Create Bid** - Place bid on a task using POST /api/v1/bids
                                3. **Track Status** - Monitor bid status changes
                                4. **Manage Lifecycle** - Accept, reject, complete, or withdraw bids
                                
                                ### 📞 Support
                                
                                For API support, contact our development team or check our GitHub repository.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Mshando Development Team")
                                .email("dev@mshando.com")
                                .url("https://github.com/mshando/bidding-service"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addServersItem(new io.swagger.v3.oas.models.servers.Server()
                        .url("http://localhost:8083")
                        .description("Local Development Server"))
                .addServersItem(new io.swagger.v3.oas.models.servers.Server()
                        .url("https://api.mshando.com/bidding")
                        .description("Production Server"))
                .components(new Components()
                        // Security Schemes
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token for authenticating API requests. Obtain from the auth service."))
                        
                        // Common Response Schemas
                        .addSchemas("ErrorResponse", new io.swagger.v3.oas.models.media.Schema<Object>()
                                .type("object")
                                .description("Standard error response format")
                                .addProperty("timestamp", new io.swagger.v3.oas.models.media.Schema<>()
                                        .type("string")
                                        .format("date-time")
                                        .description("When the error occurred")
                                        .example("2025-08-21T10:30:00"))
                                .addProperty("status", new io.swagger.v3.oas.models.media.Schema<>()
                                        .type("integer")
                                        .description("HTTP status code")
                                        .example(400))
                                .addProperty("error", new io.swagger.v3.oas.models.media.Schema<>()
                                        .type("string")
                                        .description("Error type")
                                        .example("Validation Failed"))
                                .addProperty("message", new io.swagger.v3.oas.models.media.Schema<>()
                                        .type("string")
                                        .description("Human-readable error message")
                                        .example("Request validation failed"))
                                .addProperty("path", new io.swagger.v3.oas.models.media.Schema<>()
                                        .type("string")
                                        .description("API endpoint that generated the error")
                                        .example("/api/v1/bids"))
                                .addProperty("details", new io.swagger.v3.oas.models.media.Schema<>()
                                        .type("object")
                                        .description("Additional error details (optional)")
                                        .example(java.util.Map.of("amount", "Bid amount is required"))))
                        
                        // Common Response Examples
                        .addExamples("ValidationError", new io.swagger.v3.oas.models.examples.Example()
                                .summary("Validation Error Example")
                                .description("Response when request validation fails")
                                .value("""
                                        {
                                          "timestamp": "2025-08-21T10:30:00",
                                          "status": 400,
                                          "error": "Validation Failed",
                                          "message": "Request validation failed",
                                          "path": "/api/v1/bids",
                                          "details": {
                                            "amount": "Bid amount is required",
                                            "taskId": "Task ID is required"
                                          }
                                        }
                                        """))
                        
                        .addExamples("NotFound", new io.swagger.v3.oas.models.examples.Example()
                                .summary("Not Found Example")
                                .description("Response when requested resource is not found")
                                .value("""
                                        {
                                          "timestamp": "2025-08-21T10:30:00",
                                          "status": 404,
                                          "error": "Bid Not Found",
                                          "message": "Bid with ID 999 not found",
                                          "path": "/api/v1/bids/999"
                                        }
                                        """))
                        
                        .addExamples("BusinessRuleViolation", new io.swagger.v3.oas.models.examples.Example()
                                .summary("Business Rule Violation Example")
                                .description("Response when business rules are violated")
                                .value("""
                                        {
                                          "timestamp": "2025-08-21T10:30:00",
                                          "status": 409,
                                          "error": "Invalid Bid Operation",
                                          "message": "You have already placed a bid on this task",
                                          "path": "/api/v1/bids"
                                        }
                                        """))
                        
                        .addExamples("Unauthorized", new io.swagger.v3.oas.models.examples.Example()
                                .summary("Unauthorized Example")
                                .description("Response when authentication is required")
                                .value("""
                                        {
                                          "timestamp": "2025-08-21T10:30:00",
                                          "status": 401,
                                          "error": "Unauthorized",
                                          "message": "JWT token is required",
                                          "path": "/api/v1/bids"
                                        }
                                        """))
                        
                        .addExamples("Forbidden", new io.swagger.v3.oas.models.examples.Example()
                                .summary("Forbidden Example")
                                .description("Response when access is denied")
                                .value("""
                                        {
                                          "timestamp": "2025-08-21T10:30:00",
                                          "status": 403,
                                          "error": "Forbidden",
                                          "message": "You can only modify your own bids",
                                          "path": "/api/v1/bids/123"
                                        }
                                        """)))
                .addSecurityItem(new SecurityRequirement().addList("JWT"));
    }
}
