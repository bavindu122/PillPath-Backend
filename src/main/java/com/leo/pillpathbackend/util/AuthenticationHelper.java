package com.leo.pillpathbackend.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationHelper {

    /**
     * Helper method to extract and validate the Bearer token from the Authorization header.
     * Returns the token string if valid, or null if missing/invalid.
     */
    public String extractAndValidateToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    /**
     * Helper method to extract customer ID from customer token.
     * Returns the customer ID or throws IllegalArgumentException if invalid.
     */
    public Long extractCustomerIdFromToken(String token) {
        if (token == null || !token.startsWith("customer-token-")) {
            throw new IllegalArgumentException("Invalid customer token format");
        }

        try {
            return Long.parseLong(token.replace("customer-token-", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid customer token format");
        }
    }

    /**
     * Helper method to extract pharmacy admin ID from pharmacy admin token.
     * Returns the admin ID or throws IllegalArgumentException if invalid.
     */
    public Long extractPharmacyAdminIdFromToken(String token) {
        if (token == null || !token.startsWith("pharmacy-admin-token-")) {
            throw new IllegalArgumentException("Invalid pharmacy admin token format");
        }

        try {
            return Long.parseLong(token.replace("pharmacy-admin-token-", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid pharmacy admin token format");
        }
    }

    /**
     * Helper method to extract customer ID directly from request.
     * Returns the customer ID or throws IllegalArgumentException if invalid.
     */
    public Long extractCustomerIdFromRequest(HttpServletRequest request) {
        String token = extractAndValidateToken(request);
        if (token == null) {
            throw new IllegalArgumentException("Missing or invalid authorization header");
        }
        return extractCustomerIdFromToken(token);
    }

    /**
     * Helper method to extract pharmacy admin ID directly from request.
     * Returns the admin ID or throws IllegalArgumentException if invalid.
     */
    public Long extractPharmacyAdminIdFromRequest(HttpServletRequest request) {
        String token = extractAndValidateToken(request);
        if (token == null) {
            throw new IllegalArgumentException("Missing or invalid authorization header");
        }
        return extractPharmacyAdminIdFromToken(token);
    }
}