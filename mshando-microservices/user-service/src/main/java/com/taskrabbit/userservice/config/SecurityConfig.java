package com.taskrabbit.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/users/**").permitAll()  // Allow all user endpoints for now
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api-docs/**").permitAll()  // Allow OpenAPI docs
                .requestMatchers("/swagger-ui/**").permitAll()  // Allow Swagger UI
                .requestMatchers("/swagger-ui.html").permitAll()  // Allow Swagger UI HTML
                .requestMatchers("/v3/api-docs/**").permitAll()  // Allow OpenAPI v3 docs
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}
