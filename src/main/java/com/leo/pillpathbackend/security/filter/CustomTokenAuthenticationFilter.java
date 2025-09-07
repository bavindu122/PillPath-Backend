package com.leo.pillpathbackend.security.filter;

import com.leo.pillpathbackend.util.AuthenticationHelper;
import com.leo.pillpathbackend.util.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = authHelper.extractAndValidateToken(request);
            if (token != null) {
                // Extract user information from JWT token
                Long userId = jwtService.getUserId(token);
                String role = jwtService.getRole(token);

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                // Map JWT roles to Spring Security roles
                if ("CUSTOMER".equalsIgnoreCase(role)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                } else if ("PHARMACY_ADMIN".equalsIgnoreCase(role)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                } else if ("PHARMACIST".equalsIgnoreCase(role)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_PHARMACIST"));
                } else if ("ADMIN".equalsIgnoreCase(role)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }

                if (userId != null && !authorities.isEmpty()) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception e) {
            // Log and continue without authentication
            System.err.println("Token authentication failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}