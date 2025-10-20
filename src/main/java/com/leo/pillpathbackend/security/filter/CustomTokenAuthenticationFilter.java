package com.leo.pillpathbackend.security.filter;

import com.leo.pillpathbackend.util.AuthenticationHelper;
import com.leo.pillpathbackend.util.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CustomTokenAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private AuthenticationHelper authHelper;

    @Autowired
    private JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Skip filter for public endpoints
        return path.startsWith("/api/v1/users/") ||
                path.startsWith("/api/v1/customers/register") ||
                path.startsWith("/api/v1/customers/check-email/") ||
                path.startsWith("/api/v1/pharmacies/register") ||
                path.startsWith("/api/v1/pharmacy-admins/register") ||
                path.startsWith("/api/v1/admin/") ||
                path.startsWith("/api/v1/pharmacies/") ||
                path.startsWith("/api/v1/pharmacy-admin/") ||
                path.startsWith("/api/pharmacy-admin/") ||
                path.startsWith("/api/v1/prescriptions/") ||
                path.startsWith("/api/v1/medicines/") ||
                path.startsWith("/api/v1/orders/") ||
                // OTC endpoints are public
                path.equals("/api/otc") ||
                path.startsWith("/api/otc/") ||
                path.startsWith("/api/otc-orders/") ||
                // Websocket/STOMP endpoints
                path.startsWith("/ws/") ||
                path.startsWith("/app/") ||
                path.startsWith("/topic/") ||
                "/ws/health".equals(path) ||
                "/error".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            System.out.println("CustomTokenAuthenticationFilter processing: " + request.getMethod() + " " + path);

            // If another filter already authenticated, don't override
            Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
            if (existingAuth != null && existingAuth.isAuthenticated()) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHelper.extractAndValidateToken(request);
            if (token != null) {
                System.out.println("Valid token found");
                // Extract user information from JWT token
                Long userId = jwtService.getUserId(token);
                String role = jwtService.getRole(token);
                
                System.out.println("Token - UserId: " + userId + ", Role: " + role);

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                if (role != null) {
                    String roleUpper = role.toUpperCase();
                    // Use authorities consistent with SecurityConfig.hasAuthority checks
                    switch (roleUpper) {
                        case "CUSTOMER" -> authorities.add(new SimpleGrantedAuthority("CUSTOMER"));
                        case "PHARMACY_ADMIN" -> authorities.add(new SimpleGrantedAuthority("PHARMACY_ADMIN"));
                        case "PHARMACIST" -> authorities.add(new SimpleGrantedAuthority("PHARMACIST"));
                        case "ADMIN" -> authorities.add(new SimpleGrantedAuthority("ADMIN"));
                        default -> { /* ignore unknown roles */ }
                    }
                }

                if (userId != null && !authorities.isEmpty()) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.println("Authentication set successfully");
                } else {
                    System.out.println("Failed to set authentication - userId: " + userId + ", authorities: " + authorities.size());
                }
            } else {
                System.out.println("No valid token found");
            }
        } catch (Exception e) {
            // Log and continue without authentication
            System.err.println("Token authentication failed: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}