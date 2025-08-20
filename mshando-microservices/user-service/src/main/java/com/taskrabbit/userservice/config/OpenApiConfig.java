package com.taskrabbit.userservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI Configuration for User Service
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Mshando User Service API",
        version = "1.0.0",
        description = """
            ## Mshando User Service API Documentation
            
            The User Service handles user management, authentication, and profile operations for the Mshando platform.
            
            ### Features:
            - User registration and authentication
            - JWT token-based security
            - Email verification system
            - User profile management
            - Role-based access control
            
            ### Authentication:
            This API uses JWT (JSON Web Token) for authentication. Include the token in the Authorization header:
            ```
            Authorization: Bearer <your-jwt-token>
            ```
            
            ### Getting Started:
            1. Register a new user account
            2. Login to receive a JWT token
            3. Use the token for authenticated requests
            4. Verify your email for full account access
            """,
        contact = @Contact(
            name = "Mshando Team",
            email = "support@mshando.com",
            url = "https://mshando.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8081",
            description = "Local Development Server"
        ),
        @Server(
            url = "http://localhost:8080",
            description = "API Gateway (Production Route)"
        ),
        @Server(
            url = "https://api.mshando.com",
            description = "Production Server"
        )
    }
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT Authentication",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
