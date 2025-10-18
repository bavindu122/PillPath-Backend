package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.config.ws.WsUserPrincipal;
import com.leo.pillpathbackend.ws.WatchRegistry;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class ChatWsController {
    private final SimpMessagingTemplate messagingTemplate;
    private final WatchRegistry watchRegistry;

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        Principal principal = sha.getUser();
        if (principal instanceof WsUserPrincipal p) {
            watchRegistry.registerSession(sessionId, p.getRole(), p.getUserId());
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        watchRegistry.unregisterSession(event.getSessionId());
    }

    // Admin watch control: { type:'watch', customerId }
    @MessageMapping("/chat.watch")
    public void watch(@Payload Map<String, Object> body, SimpMessageHeaderAccessor headers) {
        Principal principal = headers.getUser();
        if (!(principal instanceof WsUserPrincipal p)) { sendError(headers, "unauthorized"); return; }
        if (!("admin".equalsIgnoreCase(p.getRole()) || "pharmacy_admin".equalsIgnoreCase(p.getRole()) || "pharmacist".equalsIgnoreCase(p.getRole()))) {
            sendError(headers, "not allowed to watch");
            return;
        }
        Long customerId = toLong(body.get("customerId"));
        if (customerId == null) { sendError(headers, "customerId required"); return; }
    watchRegistry.watch(p.getRole(), p.getUserId(), customerId);
        messagingTemplate.convertAndSendToUser(p.getName(), "/queue/ack", Map.of("ok", true, "type", "watch", "customerId", customerId));
    }

    // Message: { customerId, sender:'customer'|'admin', text, time }
    @MessageMapping("/chat.message")
    public void chat(@Payload Map<String, Object> body, SimpMessageHeaderAccessor headers) {
        Principal principal = headers.getUser();
        if (!(principal instanceof WsUserPrincipal)) { sendError(headers, "unauthorized"); return; }

        Long customerId = toLong(body.get("customerId"));
        String sender = str(body.get("sender"));
        String text = str(body.get("text"));
        Long time = toLong(body.get("time"));
        if (customerId == null || sender == null || text == null || time == null) { sendError(headers, "missing fields"); return; }

        sender = sender.toLowerCase();
        if (!Objects.equals(sender, "customer") && !Objects.equals(sender, "admin")) { sendError(headers, "invalid sender"); return; }

        ChatPayload payload = new ChatPayload();
        payload.setCustomerId(customerId);
        payload.setSender(sender);
        payload.setText(text);
        payload.setTime(time);

        // Deliver to the customer
        String customerUser = "customer:" + customerId;
        messagingTemplate.convertAndSendToUser(customerUser, "/queue/chat", payload);

        // Deliver to watching admins
        var admins = watchRegistry.getWatchers(customerId);
        for (var w : admins) {
            // deliver to any admin-like watcher by using their actual role prefix
            if ("admin".equalsIgnoreCase(w.role()) || "pharmacy_admin".equalsIgnoreCase(w.role()) || "pharmacist".equalsIgnoreCase(w.role())) {
                String targetUser = w.role().toLowerCase() + ":" + w.userId();
                messagingTemplate.convertAndSendToUser(targetUser, "/queue/chat", payload);
            }
        }
    }

    @MessageExceptionHandler
    public void onError(Exception ex, SimpMessageHeaderAccessor headers) { sendError(headers, "error: " + ex.getMessage()); }

    private void sendError(SimpMessageHeaderAccessor headers, String message) {
        Principal principal = headers.getUser();
        if (principal != null) {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/error", Map.of("error", message));
        }
    }

    private Long toLong(Object v) {
        if (v == null) return null; if (v instanceof Number n) return n.longValue();
        try { return Long.valueOf(String.valueOf(v)); } catch (Exception e) { return null; }
    }
    private String str(Object v) { return v == null ? null : String.valueOf(v); }

    @Data
    public static class ChatPayload { private Long customerId; private String sender; private String text; private Long time; }
}
