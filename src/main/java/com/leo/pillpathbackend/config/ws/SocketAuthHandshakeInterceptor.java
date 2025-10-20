package com.leo.pillpathbackend.config.ws;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public class SocketAuthHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletReq) {
            HttpServletRequest http = servletReq.getServletRequest();
            attributes.put("role", firstNonEmpty(http.getParameter("role"), http.getHeader("X-WS-Role")));
            attributes.put("userId", firstNonEmpty(http.getParameter("userId"), http.getHeader("X-WS-UserId")));
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }

    private String firstNonEmpty(String a, String b) {
        if (a != null && !a.isBlank()) return a; if (b != null && !b.isBlank()) return b; return null;
    }
}
