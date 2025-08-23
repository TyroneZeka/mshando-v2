package com.mshando.userservice.config;

import com.mshando.userservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (!jwtService.isTokenExpired(jwt)) {
                    // Extract role from JWT
                    String role = jwtService.extractClaim(jwt, claims -> claims.get("role", String.class));
                    Long userId = jwtService.extractClaim(jwt, claims -> claims.get("userId", Long.class));
                    
                    // Create authorities based on role
                    List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + role)
                    );

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                    );
                    
                    // Add userId to authentication details
                    authToken.setDetails(new JwtAuthenticationDetails(userId, role));
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Set authentication for user: {} with role: {}", username, role);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Custom authentication details to store JWT claims
     */
    public static class JwtAuthenticationDetails {
        private final Long userId;
        private final String role;

        public JwtAuthenticationDetails(Long userId, String role) {
            this.userId = userId;
            this.role = role;
        }

        public Long getUserId() {
            return userId;
        }

        public String getRole() {
            return role;
        }
    }
}
