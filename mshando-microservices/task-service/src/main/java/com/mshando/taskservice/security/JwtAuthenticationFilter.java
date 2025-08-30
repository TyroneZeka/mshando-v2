package com.mshando.taskservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter for processing JWT tokens in requests
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Skip JWT processing for public endpoints
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        if (isPublicEndpoint(requestURI, method)) {
            log.debug("Skipping JWT authentication for public endpoint: {} {}", method, requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && jwtTokenUtil.validateToken(jwt)) {
                String username = jwtTokenUtil.getUsernameFromToken(jwt);
                Long userId = jwtTokenUtil.getUserIdFromToken(jwt);
                List<String> roles = jwtTokenUtil.getRolesFromToken(jwt);
                
                // Convert roles to Spring Security authorities
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList());
                
                // Create UserDetails with user ID as username for easy access
                UserDetails userDetails = User.builder()
                        .username(userId.toString()) // Using userId as username for easy extraction
                        .password("") // Not needed for JWT
                        .authorities(authorities)
                        .build();
                
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("Set authentication for user: {} with roles: {}", username, roles);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            // Don't throw exception, just log and continue
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Check if the endpoint is public and should skip JWT processing
     */
    private boolean isPublicEndpoint(String requestURI, String method) {
        // Actuator endpoints
        if (requestURI.startsWith("/actuator/")) {
            return "GET".equals(method);
        }
        
        // Swagger endpoints
        if (requestURI.startsWith("/swagger-ui/") || requestURI.startsWith("/v3/api-docs/")) {
            return "GET".equals(method);
        }
        
        // Public API endpoints
        if ("GET".equals(method)) {
            return requestURI.equals("/api/categories/active") ||
                   requestURI.equals("/api/categories/search") ||
                   requestURI.startsWith("/api/tasks/search/") ||
                   requestURI.matches("/api/tasks/\\d+") ||
                   requestURI.matches("/api/tasks/\\d+/images") ||
                   requestURI.matches("/api/tasks/\\d+/images/primary");
        }
        
        return false;
    }

    /**
     * Extract JWT token from request header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
