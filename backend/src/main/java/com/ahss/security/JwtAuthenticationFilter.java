package com.ahss.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.debug("Processing request: {} {}", method, requestURI);
        
        String header = request.getHeader("Authorization");
        log.debug("Authorization header present: {}", header != null);
        
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            log.debug("Extracted JWT token (first 20 chars): {}...", token.length() > 20 ? token.substring(0, 20) : token);
            
            try {
                Claims claims = JwtTokenProvider.parse(token);
                String username = claims.getSubject();
                log.debug("JWT token parsed successfully. Username: {}", username);

                // Extract userId from token claims
                Long userId = claims.get("userId") != null ?
                    ((Number) claims.get("userId")).longValue() : null;
                log.debug("Extracted userId from token: {}", userId);

                // Extract roles from token claims
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles");
                log.debug("Extracted roles from token: {}", roles);

                // Convert roles to authorities
                List<SimpleGrantedAuthority> authorities = roles != null ?
                    roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase().replace(" ", "_")))
                        .collect(Collectors.toList()) :
                    List.of(new SimpleGrantedAuthority("ROLE_USER"));

                log.debug("Converted authorities: {}", authorities);

                // Create UserPrincipal with userId and username
                UserPrincipal userPrincipal = new UserPrincipal(userId, username);
                var auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("Authentication set in SecurityContext for user: {} (userId: {})", username, userId);

            } catch (Exception e) {
                log.warn("JWT token validation failed: {}", e.getMessage());
                log.debug("JWT token validation exception details", e);
                // invalid token, proceed unauthenticated
            }
        } else {
            log.debug("No valid Authorization header found");
        }
        
        log.debug("Proceeding with filter chain for request: {} {}", method, requestURI);
        chain.doFilter(request, response);
    }
}