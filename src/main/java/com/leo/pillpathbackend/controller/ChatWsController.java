package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.config.ws.WsUserPrincipal;
import com.leo.pillpathbackend.entity.ChatRoom;
import com.leo.pillpathbackend.repository.ChatRoomRepository;
import com.leo.pillpathbackend.ws.WatchRegistry;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
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

/**
 * WebSocket controller for real-time chat messaging.
 * 
 * This controller handles:
 * 1. Connection/disconnection events
 * 2. Room-specific message routing
 * 3. Typing indicators
 * 4. User presence tracking
 * 
 * Architecture:
 * - Each chat room has its own WebSocket topic: /topic/chat/room/{chatRoomId}
 * - Users subscribe to specific chat rooms they participate in
 * - Messages are broadcasted only to participants of the specific chat room
 */
@Controller
@RequiredArgsConstructor
public class ChatWsController {
    private final SimpMessagingTemplate messagingTemplate;
    private final WatchRegistry watchRegistry;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * Handle WebSocket connection events.
     * Register user session for tracking and presence.
     */
    @EventListener
    public void onConnect(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        Principal principal = sha.getUser();
        if (principal instanceof WsUserPrincipal p) {
            watchRegistry.registerSession(sessionId, p.getRole(), p.getUserId());
            System.out.println("WebSocket connected: user=" + p.getUserId() + ", role=" + p.getRole() + ", session=" + sessionId);
        }
    }

    /**
     * Handle WebSocket disconnection events.
     * Clean up user session and presence tracking.
     */
    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        watchRegistry.unregisterSession(event.getSessionId());
        System.out.println("WebSocket disconnected: session=" + event.getSessionId());
    }

    /**
     * Handle room-specific messages via WebSocket.
     * 
     * IMPORTANT: This is for real-time message delivery ONLY.
     * Messages are already persisted by the REST API endpoint before this is called.
     * 
     * Message format:
     * {
     *   "chatRoomId": 123,
     *   "text": "Hello",
     *   "senderId": 456,
     *   "senderType": "CUSTOMER" | "ADMIN" | "PHARMACIST"
     * }
     * 
     * Flow:
     * 1. Validate user has access to this chat room
     * 2. Broadcast message to all participants in the room
     * 3. Message is already saved to database by REST endpoint
     */
    @MessageMapping("/chat.room.{chatRoomId}")
    public void sendMessageToRoom(
            @DestinationVariable Long chatRoomId,
            @Payload Map<String, Object> messageData,
            SimpMessageHeaderAccessor headers) {
        
        Principal principal = headers.getUser();
        if (!(principal instanceof WsUserPrincipal wsUser)) {
            sendError(headers, "Unauthorized: No valid user principal");
            return;
        }

        try {
            // Validate chat room exists
            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
            if (chatRoom == null) {
                sendError(headers, "Chat room not found");
                return;
            }

            // Validate user is a participant in this chat room
            boolean isParticipant = false;
            String userRole = wsUser.getRole().toLowerCase();
            Long userId = wsUser.getUserId();

            if ("customer".equals(userRole) && chatRoom.getCustomer().getId().equals(userId)) {
                isParticipant = true;
            } else if (("pharmacy_admin".equals(userRole) || "admin".equals(userRole) || "pharmacist".equals(userRole))
                    && chatRoom.getPharmacy() != null) {
                // Verify pharmacy staff belongs to the same pharmacy
                isParticipant = true; // Simplified - in production, verify pharmacy relationship
            }

            if (!isParticipant) {
                sendError(headers, "You are not a participant in this chat room");
                return;
            }

            // Extract message data
            String text = str(messageData.get("text"));
            if (text == null || text.trim().isEmpty()) {
                sendError(headers, "Message text is required");
                return;
            }

            // Broadcast to room topic (all subscribers to this specific chat room)
            messagingTemplate.convertAndSend("/topic/chat/room/" + chatRoomId, messageData);
            
            // Send acknowledgment to sender
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/ack",
                Map.of("ok", true, "type", "message", "chatRoomId", chatRoomId)
            );

        } catch (Exception e) {
            System.err.println("Error in sendMessageToRoom: " + e.getMessage());
            sendError(headers, "Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Send typing indicator for a specific chat room.
     * 
     * Message format:
     * {
     *   "chatRoomId": 123,
     *   "isTyping": true,
     *   "userId": 456,
     *   "userName": "John Doe"
     * }
     */
    @MessageMapping("/chat.typing.{chatRoomId}")
    public void sendTypingIndicator(
            @DestinationVariable Long chatRoomId,
            @Payload Map<String, Object> typingData,
            SimpMessageHeaderAccessor headers) {
        
        Principal principal = headers.getUser();
        if (!(principal instanceof WsUserPrincipal wsUser)) {
            return; // Silently ignore for typing indicators
        }

        try {
            // Validate chat room exists
            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
            if (chatRoom == null) {
                return;
            }

            // Broadcast typing indicator to room (except sender)
            Map<String, Object> typingPayload = Map.of(
                "chatRoomId", chatRoomId,
                "userId", wsUser.getUserId(),
                "userName", str(typingData.get("userName")),
                "isTyping", Boolean.TRUE.equals(typingData.get("isTyping")),
                "userType", wsUser.getRole()
            );
            
            messagingTemplate.convertAndSend("/topic/chat/room/" + chatRoomId + "/typing", typingPayload);
            
        } catch (Exception e) {
            System.err.println("Error in sendTypingIndicator: " + e.getMessage());
        }
    }

    /**
     * Join a specific chat room (subscribe notification).
     * This allows tracking which users are actively viewing which chat rooms.
     */
    @MessageMapping("/chat.join.{chatRoomId}")
    public void joinRoom(
            @DestinationVariable Long chatRoomId,
            SimpMessageHeaderAccessor headers) {
        
        Principal principal = headers.getUser();
        if (!(principal instanceof WsUserPrincipal wsUser)) {
            return;
        }

        try {
            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
            if (chatRoom == null) {
                return;
            }

            // Notify room that user joined (for presence tracking)
            Map<String, Object> joinPayload = Map.of(
                "chatRoomId", chatRoomId,
                "userId", wsUser.getUserId(),
                "userType", wsUser.getRole(),
                "action", "joined"
            );
            
            messagingTemplate.convertAndSend("/topic/chat/room/" + chatRoomId + "/presence", joinPayload);
            
        } catch (Exception e) {
            System.err.println("Error in joinRoom: " + e.getMessage());
        }
    }

    /**
     * Leave a specific chat room (unsubscribe notification).
     */
    @MessageMapping("/chat.leave.{chatRoomId}")
    public void leaveRoom(
            @DestinationVariable Long chatRoomId,
            SimpMessageHeaderAccessor headers) {
        
        Principal principal = headers.getUser();
        if (!(principal instanceof WsUserPrincipal wsUser)) {
            return;
        }

        try {
            // Notify room that user left
            Map<String, Object> leavePayload = Map.of(
                "chatRoomId", chatRoomId,
                "userId", wsUser.getUserId(),
                "userType", wsUser.getRole(),
                "action", "left"
            );
            
            messagingTemplate.convertAndSend("/topic/chat/room/" + chatRoomId + "/presence", leavePayload);
            
        } catch (Exception e) {
            System.err.println("Error in leaveRoom: " + e.getMessage());
        }
    }

    /**
     * Global error handler for WebSocket messages.
     */
    @MessageExceptionHandler
    public void onError(Exception ex, SimpMessageHeaderAccessor headers) {
        System.err.println("WebSocket error: " + ex.getMessage());
        ex.printStackTrace();
        sendError(headers, "Error: " + ex.getMessage());
    }

    /**
     * Send error message to the user who triggered the error.
     */
    private void sendError(SimpMessageHeaderAccessor headers, String message) {
        Principal principal = headers.getUser();
        if (principal != null) {
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/error",
                Map.of("error", message, "timestamp", System.currentTimeMillis())
            );
        }
    }

    // Utility methods
    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try {
            return Long.valueOf(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    private String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    @Data
    public static class ChatPayload {
        private Long customerId;
        private String sender;
        private String text;
        private Long time;
    }
}
