package com.leo.pillpathbackend.security;

import com.leo.pillpathbackend.util.JwtService;
import com.leo.pillpathbackend.util.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    private List<SimpleGrantedAuthority> mapAuthorities(String role) {
        if (role == null) return List.of();
        String r = role.toUpperCase();
        List<SimpleGrantedAuthority> list = new ArrayList<>();
        switch (r) {
            case "CUSTOMER" -> {
                list.add(new SimpleGrantedAuthority("CUSTOMER"));
                list.add(new SimpleGrantedAuthority("ROLE_USER"));
                list.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
            }
            case "ADMIN" -> {
                list.add(new SimpleGrantedAuthority("ADMIN"));
                list.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
            case "PHARMACY_ADMIN" -> {
                list.add(new SimpleGrantedAuthority("PHARMACY_ADMIN"));
                list.add(new SimpleGrantedAuthority("ROLE_PHARMACY_ADMIN"));
            }
            case "PHARMACIST" -> {
                list.add(new SimpleGrantedAuthority("PHARMACIST"));
                list.add(new SimpleGrantedAuthority("ROLE_PHARMACIST"));
            }
            default -> list.add(new SimpleGrantedAuthority(r));
        }
        return list;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (StringUtils.hasText(token) && !tokenBlacklistService.isBlacklisted(token) && jwtService.isTokenValid(token)) {
                    Long userId = jwtService.getUserId(token);
                    String role = jwtService.getRole(token);
                    if (role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        List<SimpleGrantedAuthority> authorities = mapAuthorities(role);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                authorities
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception ignored) {
            // On any parsing/validation error, proceed without setting auth
        }
        filterChain.doFilter(request, response);
    }
}
