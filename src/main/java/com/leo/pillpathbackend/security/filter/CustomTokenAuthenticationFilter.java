package com.leo.pillpathbackend.security.filter;

import com.leo.pillpathbackend.util.AuthenticationHelper;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = authHelper.extractAndValidateToken(request);
            if (token != null) {
                Long userId = null;
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                if (token.startsWith("customer-token-")) {
                    userId = authHelper.extractCustomerIdFromToken(token);
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                } else if (token.startsWith("temp-token-customer-")) {
                    userId = extractFromTempToken(token);
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                } else if (token.startsWith("pharmacy-admin-token-")) {
                    userId = authHelper.extractPharmacyAdminIdFromToken(token);
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }

                if (userId != null) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception e) {
            // Log and continue without authentication
        }

        filterChain.doFilter(request, response);
    }

    private Long extractFromTempToken(String token) {
        String[] parts = token.split("-");
        return Long.parseLong(parts[parts.length - 1]);
    }
}