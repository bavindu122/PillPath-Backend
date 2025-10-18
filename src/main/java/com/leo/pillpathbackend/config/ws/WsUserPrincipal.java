package com.leo.pillpathbackend.config.ws;

import java.security.Principal;

public class WsUserPrincipal implements Principal {
    private final String name;
    private final String role;
    private final Long userId;

    public WsUserPrincipal(String name, String role, Long userId) {
        this.name = name;
        this.role = role;
        this.userId = userId;
    }

    @Override
    public String getName() { return name; }
    public String getRole() { return role; }
    public Long getUserId() { return userId; }
}
