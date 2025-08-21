package com.mshando.biddingservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Bidding Service.
 * 
 * Configures JWT-based authentication, CORS settings,
 * and endpoint security rules for the bidding API.
 * 
 * NOTE: This is a placeholder configuration. In a full implementation,
 * you would need to add the JWT authentication filter and proper
 * JWT token validation logic.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Main security filter chain configuration.
     * Configures endpoint access rules and authentication requirements.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API
            .csrf(csrf -> csrf.disable())
            
            // Configure session management as stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure endpoint authorization
            .authorizeHttpRequests(authz -> authz
                // Public endpoints (health checks, documentation)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // Protected API endpoints
                .requestMatchers("/api/v1/bids/**").authenticated()
                
                // All other endpoints require authentication
                .anyRequest().authenticated())
            
            // Configure headers
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentTypeOptions -> {})
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)));

        // TODO: Add JWT authentication filter
        // http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Security properties for configuring authentication and authorization.
     */
    @ConfigurationProperties(prefix = "mshando.bidding.security")
    public static class SecurityProperties {
        
        /**
         * JWT secret key for token validation
         */
        private String jwtSecret = "your-secret-key-here";
        
        /**
         * JWT token expiration time in milliseconds
         */
        private long jwtExpirationMs = 86400000; // 24 hours
        
        /**
         * Whether to enable JWT authentication
         */
        private boolean jwtEnabled = true;
        
        /**
         * Allowed origins for CORS
         */
        private String[] allowedOrigins = {"http://localhost:3000", "http://localhost:8080"};
        
        /**
         * Whether to allow credentials in CORS requests
         */
        private boolean allowCredentials = true;
        
        /**
         * Maximum age for CORS preflight requests
         */
        private long corsMaxAge = 3600;

        // Getters and setters
        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public long getJwtExpirationMs() {
            return jwtExpirationMs;
        }

        public void setJwtExpirationMs(long jwtExpirationMs) {
            this.jwtExpirationMs = jwtExpirationMs;
        }

        public boolean isJwtEnabled() {
            return jwtEnabled;
        }

        public void setJwtEnabled(boolean jwtEnabled) {
            this.jwtEnabled = jwtEnabled;
        }

        public String[] getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String[] allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public long getCorsMaxAge() {
            return corsMaxAge;
        }

        public void setCorsMaxAge(long corsMaxAge) {
            this.corsMaxAge = corsMaxAge;
        }
    }
}
