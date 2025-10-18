package com.leo.pillpathbackend.config.ws;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class CustomPrincipalHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(@NonNull ServerHttpRequest request, @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
        String role = (String) attributes.get("role");
        String userIdStr = (String) attributes.get("userId");
        Long userId = null;
        try { if (userIdStr != null) userId = Long.valueOf(userIdStr); } catch (NumberFormatException ignored) {}
        String name = (role != null ? role.toLowerCase() : "anon") + ":" + (userId != null ? userId : UUID.randomUUID());
        return new WsUserPrincipal(name, role != null ? role.toLowerCase() : null, userId);
    }
}
