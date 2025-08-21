package com.leo.pillpathbackend.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    @Value("${jwt.issuer:pillpath}")
    private String issuer;

    private SecretKey key;

    @PostConstruct
    void init() {
        // Ensure key is at least 32 bytes for HS256
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            // pad to 32 bytes minimal
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, Math.min(bytes.length, 32));
            for (int i = bytes.length; i < 32; i++) padded[i] = (byte) i;
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes (256 bits) for HS256.");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(Long userId, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiration(exp)
                .subject(String.valueOf(userId))
                .claim("uid", userId)
                .claim("role", role)
                .signWith(key)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Object uid = claims.get("uid");
        if (uid instanceof Number) return ((Number) uid).longValue();
        if (uid instanceof String) return Long.parseLong((String) uid);
        // fallback to subject
        return Long.parseLong(claims.getSubject());
    }

    public String getRole(String token) {
        var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Object role = claims.get("role");
        return role != null ? role.toString() : null;
    }

    public Date getExpiration(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getExpiration();
    }
}
