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
                                Comprehensive bidding service for managing task bids in the Mshando platform.
                                
                                ## Features
                                - **Bid Management**: Create, update, accept, reject, and withdraw bids
                                - **Status Tracking**: Complete lifecycle management of bid statuses
                                - **Pagination**: Efficient retrieval of large bid datasets
                                - **Validation**: Business rule enforcement and data validation
                                - **Integration**: Seamless integration with task and user services
                                
                                ## Business Rules
                                - Taskers can only place one bid per task
                                - Only pending bids can be modified
                                - Customers can accept/reject bids for their tasks
                                - Taskers can withdraw their own pending or accepted bids
                                - Auto-acceptance can be configured for old pending bids
                                
                                ## Authentication
                                All endpoints require JWT authentication except health checks.
                                Include the JWT token in the Authorization header as 'Bearer {token}'.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Mshando Development Team")
                                .email("dev@mshando.com")
                                .url("https://github.com/mshando/bidding-service"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token for authenticating API requests")))
                .addSecurityItem(new SecurityRequirement().addList("JWT"));
    }
}
