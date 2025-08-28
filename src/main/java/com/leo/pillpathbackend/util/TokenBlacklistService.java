package com.leo.pillpathbackend.util;

import java.util.Date;

public interface TokenBlacklistService {
    void blacklist(String token);
    void blacklist(String token, Date expiresAt);
    boolean isBlacklisted(String token);
}

