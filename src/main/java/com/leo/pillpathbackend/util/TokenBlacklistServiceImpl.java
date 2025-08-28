package com.leo.pillpathbackend.util;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    // token -> expiration epoch ms
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    @Override
    public void blacklist(String token) {
        // default to 24h if unknown
        blacklist.put(token, System.currentTimeMillis() + 24 * 60 * 60 * 1000);
    }

    @Override
    public void blacklist(String token, Date expiresAt) {
        blacklist.put(token, expiresAt != null ? expiresAt.getTime() : System.currentTimeMillis() + 24 * 60 * 60 * 1000);
    }

    @Override
    public boolean isBlacklisted(String token) {
        Long exp = blacklist.get(token);
        if (exp == null) return false;
        if (exp < System.currentTimeMillis()) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }
}

