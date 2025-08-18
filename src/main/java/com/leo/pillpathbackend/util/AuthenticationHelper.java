package com.leo.pillpathbackend.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationHelper {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Extracts Bearer token and validates JWT + blacklist.
     * Returns the token if valid, otherwise null.
     */
    public String extractAndValidateToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        if (token == null || token.isBlank()) return null;
        if (tokenBlacklistService.isBlacklisted(token)) return null;
        return jwtService.isTokenValid(token) ? token : null;
    }

    /**
     * Extract customer ID from JWT; requires role CUSTOMER
     */
    public Long extractCustomerIdFromToken(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Missing token");
        }
        if (!jwtService.isTokenValid(token)) {
            throw new IllegalArgumentException("Invalid token");
        }
        String role = jwtService.getRole(token);
        if (role == null || !role.equalsIgnoreCase("CUSTOMER")) {
            throw new IllegalArgumentException("Invalid role for this resource");
        }
        return jwtService.getUserId(token);
    }

    /**
     * Extract pharmacy admin ID from JWT; requires role PHARMACY_ADMIN
     */
    public Long extractPharmacyAdminIdFromToken(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Missing token");
        }
        if (!jwtService.isTokenValid(token)) {
            throw new IllegalArgumentException("Invalid token");
        }
        String role = jwtService.getRole(token);
        if (role == null || !role.equalsIgnoreCase("PHARMACY_ADMIN")) {
            throw new IllegalArgumentException("Invalid role for this resource");
        }
        return jwtService.getUserId(token);
    }

    /**
     * Extract customer ID from request header via JWT
     */
    public Long extractCustomerIdFromRequest(HttpServletRequest request) {
        String token = extractAndValidateToken(request);
        if (token == null) {
            throw new IllegalArgumentException("Missing or invalid authorization header");
        }
        return extractCustomerIdFromToken(token);
    }

    /**
     * Extract pharmacy admin ID from request header via JWT
     */
    public Long extractPharmacyAdminIdFromRequest(HttpServletRequest request) {
        String token = extractAndValidateToken(request);
        if (token == null) {
            throw new IllegalArgumentException("Missing or invalid authorization header");
        }
        return extractPharmacyAdminIdFromToken(token);
    }
}