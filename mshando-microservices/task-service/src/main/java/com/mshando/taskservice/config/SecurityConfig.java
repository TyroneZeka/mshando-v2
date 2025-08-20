package com.mshando.taskservice.config;

import com.mshando.taskservice.security.JwtAuthenticationEntryPoint;
import com.mshando.taskservice.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for Task Service
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/active").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tasks/search/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tasks/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tasks/{taskId}/images").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tasks/{taskId}/images/primary").permitAll()
                        
                        // Admin only endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/tasks/due-soon").hasRole("ADMIN")
                        
                        // Customer endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/tasks").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/tasks/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/tasks/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/tasks/**/publish").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/tasks/**/assign").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/tasks/**/cancel").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/tasks/my-tasks").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/tasks/**/images/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/tasks/**/images/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/tasks/**/images/**").hasRole("CUSTOMER")
                        
                        // Tasker endpoints
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/tasks/**/start").hasRole("TASKER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/tasks/**/complete").hasRole("TASKER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/tasks/my-assignments").hasRole("TASKER")
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Total-Count"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
