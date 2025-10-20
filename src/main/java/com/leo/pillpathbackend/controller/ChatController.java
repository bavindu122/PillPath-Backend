package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.ChatMessageHistoryResponse;
import com.leo.pillpathbackend.dto.ChatRoomDTO;
import com.leo.pillpathbackend.dto.MessageDTO;
import com.leo.pillpathbackend.dto.StartChatRequest;
import com.leo.pillpathbackend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping({"/api/v1/chats", "/api/chats", "/api/v1/v1/chats"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    /**
     * Start a new chat or get existing chat with a pharmacy
     */
    @PostMapping("/start")
    public ResponseEntity<ChatRoomDTO> startChat(@RequestBody StartChatRequest request) {
        // Extract userId from Spring Security context (set by JWT filter)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            return ResponseEntity.status(401).build();
        }

        Long userId = (Long) auth.getPrincipal();
        ChatRoomDTO chatRoom = chatService.startChat(userId, request);
        return ResponseEntity.ok(chatRoom);
    }

    /**
     * Get all chat rooms for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<ChatRoomDTO>> getMyChats() {
        // Extract userId from Spring Security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            return ResponseEntity.status(401).body(java.util.Collections.emptyList());
        }

        Long userId = (Long) auth.getPrincipal();

        // Determine user type from authorities
        String userType = auth.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> {
                    String authority = grantedAuthority.getAuthority();
                    if ("ROLE_USER".equals(authority) || "ROLE_CUSTOMER".equals(authority)) return "CUSTOMER";
                    if ("ROLE_PHARMACIST".equals(authority)) return "PHARMACIST";
                    if ("ROLE_PHARMACY_ADMIN".equals(authority)) return "ADMIN"; // treat pharmacy admin as admin side
                    if ("ROLE_ADMIN".equals(authority)) return "ADMIN";
                    return "CUSTOMER";
                })
                .orElse("CUSTOMER");

        List<ChatRoomDTO> chats = chatService.getMyChats(userId, userType);
        return ResponseEntity.ok(chats);
    }

    /**
     * Alias route used by frontend: GET /threads
     */
    @GetMapping("/threads")
    public ResponseEntity<List<ChatRoomDTO>> getThreads() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }

        Long userId = (Long) auth.getPrincipal();
        String userType = auth.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> {
                    String authority = grantedAuthority.getAuthority();
                    if ("ROLE_USER".equals(authority) || "ROLE_CUSTOMER".equals(authority)) return "CUSTOMER";
                    if ("ROLE_PHARMACIST".equals(authority)) return "PHARMACIST";
                    if ("ROLE_PHARMACY_ADMIN".equals(authority)) return "ADMIN";
                    if ("ROLE_ADMIN".equals(authority)) return "ADMIN";
                    return "CUSTOMER";
                })
                .orElse("CUSTOMER");

        List<ChatRoomDTO> chats = chatService.getMyChats(userId, userType);
        return ResponseEntity.ok(chats);
    }

    /**
     * Get a specific chat room by ID
     */
    @GetMapping("/{chatId:\\d+}")
    public ResponseEntity<ChatRoomDTO> getChatRoom(@PathVariable Long chatId) {
        ChatRoomDTO chatRoom = chatService.getChatRoomById(chatId);
        return ResponseEntity.ok(chatRoom);
    }

    /**
     * Get chat messages with pagination
     */
    @GetMapping("/{chatId:\\d+}/messages")
    public ResponseEntity<ChatMessageHistoryResponse> getChatMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit,
            HttpServletRequest request) {

        System.out.println("=== GET CHAT MESSAGES DEBUG ===");
        System.out.println("ChatId: " + chatId + ", Page: " + page + ", Limit: " + limit);
        System.out.println("Request URI: " + request.getRequestURI());
        
        try {
            ChatMessageHistoryResponse messages = chatService.getChatMessages(chatId, page, limit);
            System.out.println("Returning " + messages.getMessages().size() + " messages");
            
            // Debug: print first few messages
            messages.getMessages().stream().limit(3).forEach(msg -> {
                System.out.println("  - Message " + msg.getId() + ": ");
                System.out.println("      Content: '" + msg.getContent() + "'");
                System.out.println("      SenderType: " + msg.getSenderType());
                System.out.println("      SenderId: " + msg.getSenderId());
                System.out.println("      SenderName: " + msg.getSenderName());
                System.out.println("      Content length: " + (msg.getContent() != null ? msg.getContent().length() : "null"));
            });
            
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("ERROR getting messages: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(ChatMessageHistoryResponse.builder
                            ()
                .chatRoomId(chatId)
                .messages(java.util.Collections.emptyList())
                .currentPage(0)
                .totalPages(0)
                .totalMessages(0)
                .hasMore(false)
                .build());
        }
    }

    /**
     * Mark a chat as read (reset unread count for current user)
     */
    @PostMapping("/{chatId:\\d+}/mark-read")
    public ResponseEntity<?> markChatAsRead(@PathVariable Long chatId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        }

        Long userId = (Long) auth.getPrincipal();
        String userType = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .map(role -> {
                    if ("ROLE_USER".equals(role) || "ROLE_CUSTOMER".equals(role)) return "CUSTOMER";
                    if ("ROLE_PHARMACIST".equals(role)) return "PHARMACIST";
                    if ("ROLE_PHARMACY_ADMIN".equals(role)) return "ADMIN";
                    if ("ROLE_ADMIN".equals(role)) return "ADMIN";
                    return "CUSTOMER";
                })
                .orElse("CUSTOMER");

        try {
            chatService.markChatAsRead(chatId, userId, userType);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "message", "Chat service is running"));
    }

    /**
     * Get total unread messages for the current authenticated user
     * Returns: { count: number }
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            // Allow anonymous calls (return 0) to avoid noisy errors in local dev
            return ResponseEntity.ok(Map.of("count", 0));
        }

        Long userId = (Long) auth.getPrincipal();
        String userType = auth.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> {
                    String authority = grantedAuthority.getAuthority();
                    if ("ROLE_USER".equals(authority) || "ROLE_CUSTOMER".equals(authority)) return "CUSTOMER";
                    if ("ROLE_PHARMACIST".equals(authority)) return "PHARMACIST";
                    if ("ROLE_PHARMACY_ADMIN".equals(authority)) return "ADMIN";
                    if ("ROLE_ADMIN".equals(authority)) return "ADMIN";
                    return "CUSTOMER";
                })
                .orElse("CUSTOMER");

        int count = chatService.getUnreadCount(userId, userType);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // Persist a message and broadcast
    @PostMapping("/{chatId:\\d+}/messages")
    public ResponseEntity<?> sendMessage(
        @PathVariable Long chatId,
        @RequestBody(required = false) Object body,
        @RequestParam(value = "text", required = false) String textParam,
        @RequestParam(value = "content", required = false) String contentParam,
        @RequestParam(value = "message", required = false) String messageParam,
        HttpServletRequest request
    ) {
        System.out.println("=== SEND MESSAGE DEBUG ===");
        System.out.println("Method: " + request.getMethod());
        System.out.println("URI: " + request.getRequestURI());
        System.out.println("ChatId: " + chatId);
        System.out.println("Authorization Header: " + request.getHeader("Authorization"));
        System.out.println("Content-Type: " + request.getContentType());
        System.out.println("Body: " + body);
        System.out.println("Text Param: " + textParam);
        try {
            System.out.println("SendMessage called for chatId: " + chatId);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Auth: " + (auth != null ? auth.toString() : "null"));

            if (auth == null || !(auth.getPrincipal() instanceof Long)) {
                System.out.println("Authentication failed - returning 401");
                return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
            }

            Long senderId = (Long) auth.getPrincipal();
            System.out.println("SenderId: " + senderId);

            String userType = auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority())
                    .map(role -> {
                        if ("ROLE_USER".equals(role) || "ROLE_CUSTOMER".equals(role)) return "CUSTOMER";
                        if ("ROLE_PHARMACIST".equals(role)) return "PHARMACIST";
                        if ("ROLE_PHARMACY_ADMIN".equals(role)) return "ADMIN";
                        if ("ROLE_ADMIN".equals(role)) return "ADMIN";
                        return "CUSTOMER";
                    })
                    .orElse("CUSTOMER");

            System.out.println("UserType: " + userType);

            String text = null;
            String contentType = request.getContentType();
            System.out.println("Content-Type: " + contentType);

            if (body == null) {
                // fall back to query params if body is not JSON or missing
                if (textParam != null && !textParam.isBlank()) text = textParam;
                else if (contentParam != null && !contentParam.isBlank()) text = contentParam;
                else if (messageParam != null && !messageParam.isBlank()) text = messageParam;
            } else if (body instanceof java.util.Map) {
                // accept common keys from different UIs
                Map<?,?> map = (Map<?,?>) body;
                Object t1 = map.get("text");
                Object t2 = map.get("content");
                Object t3 = map.get("message");
                if (t1 != null) text = String.valueOf(t1);
                else if (t2 != null) text = String.valueOf(t2);
                else if (t3 != null) text = String.valueOf(t3);
            } else if (body instanceof String) {
                text = (String) body;
            } else {
                text = String.valueOf(body);
            }

            System.out.println("Extracted text: " + text);

            if (text == null || text.isBlank()) {
                // As a last resort, try reading raw body (e.g., text/plain)
                try {
                    String raw = request.getReader() != null ? request.getReader().lines().reduce("", (a,b) -> a + b) : null;
                    if (raw != null && !raw.isBlank()) {
                        text = raw;
                    }
                } catch (Exception ignored) {}
            }

            if (text == null || text.isBlank()) {
                System.out.println("No text found - returning 400");
                return ResponseEntity.badRequest().body(Map.of("error", "text required"));
            }

            System.out.println("Calling persistAndBroadcastMessage...");
            MessageDTO savedMessage = chatService.persistAndBroadcastMessage(chatId, senderId, userType, text);
            System.out.println("Message sent successfully, returning MessageDTO: " + savedMessage);
            return ResponseEntity.ok(savedMessage);
        } catch (IllegalArgumentException ex) {
            System.out.println("IllegalArgumentException: " + ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "failed"));
        }
    }

    /**
     * API specifically for pharmacy admin dashboard - Get all chats for pharmacy
     */
    @GetMapping("/pharmacy-admin/dashboard/chats")
    public ResponseEntity<List<ChatRoomDTO>> getPharmacyAdminChats(HttpServletRequest request) {
        try {
            // Extract userId from Spring Security context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !(auth.getPrincipal() instanceof Long)) {
                return ResponseEntity.status(401).body(java.util.Collections.emptyList());
            }

            Long userId = (Long) auth.getPrincipal();
            
            // Force userType to ADMIN for pharmacy admin dashboard
            List<ChatRoomDTO> chats = chatService.getMyChats(userId, "ADMIN");
            return ResponseEntity.ok(chats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Collections.emptyList());
        }
    }

    /**
     * API specifically for pharmacy admin dashboard - Get chat messages
     */
    @GetMapping("/pharmacy-admin/dashboard/chats/{chatId}/messages")
    public ResponseEntity<ChatMessageHistoryResponse> getPharmacyAdminChatMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit,
            HttpServletRequest request) {
        
        try {
            System.out.println("=== PHARMACY ADMIN GET MESSAGES DEBUG ===");
            System.out.println("ChatId: " + chatId);
            System.out.println("Page: " + page + ", Limit: " + limit);
            System.out.println("Authorization Header: " + request.getHeader("Authorization"));
            
            // Get authentication - allow anonymous for testing
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                System.out.println("Auth Principal: " + auth.getPrincipal());
                System.out.println("Auth Authorities: " + auth.getAuthorities());
            } else {
                System.out.println("No authentication found - allowing for testing");
            }
            
            ChatMessageHistoryResponse messages = chatService.getChatMessages(chatId, page, limit);
            System.out.println("Retrieved " + messages.getMessages().size() + " messages");
            System.out.println("Has more: " + messages.isHasMore());
            
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("ERROR getting pharmacy admin messages: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(ChatMessageHistoryResponse.builder()
                .chatRoomId(chatId)
                .messages(java.util.Collections.emptyList())
                .currentPage(0)
                .totalPages(0)
                .totalMessages(0)
                .hasMore(false)
                .build());
        }
    }

    /**
     * API specifically for pharmacy admin dashboard - Send message
     */
    @PostMapping("/pharmacy-admin/dashboard/chats/{chatId}/messages")
    public ResponseEntity<?> sendPharmacyAdminMessage(
            @PathVariable Long chatId,
            @RequestBody Map<String, String> messageData,
            HttpServletRequest request) {
        
        try {
            System.out.println("=== PHARMACY ADMIN SEND MESSAGE DEBUG ===");
            System.out.println("ChatId: " + chatId);
            System.out.println("Request body: " + messageData);
            System.out.println("Authorization Header: " + request.getHeader("Authorization"));
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Auth: " + (auth != null ? auth.toString() : "null"));
            
            Long senderId = null;
            
            // Try to get senderId from authentication
            if (auth != null && auth.getPrincipal() instanceof Long) {
                senderId = (Long) auth.getPrincipal();
                System.out.println("SenderId from auth: " + senderId);
            } 
            // Fallback: Allow passing senderId in request body for testing
            else if (messageData.containsKey("senderId")) {
                senderId = Long.parseLong(messageData.get("senderId"));
                System.out.println("SenderId from request body: " + senderId);
            }
            // Fallback: Use default CURRENT_USER_ID from frontend
            else {
                System.out.println("❌ No senderId found in auth or request body");
                return ResponseEntity.status(401).body(Map.of(
                    "error", "unauthorized", 
                    "hint", "Set AUTH_TOKEN or include senderId in request body"
                ));
            }

            String text = messageData.get("text");
            
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "text required"));
            }

            System.out.println("Calling persistAndBroadcastMessage with:");
            System.out.println("  chatId: " + chatId);
            System.out.println("  senderId: " + senderId);
            System.out.println("  userType: ADMIN");
            System.out.println("  text: " + text);
            
            // Force userType to ADMIN for pharmacy admin dashboard
            chatService.persistAndBroadcastMessage(chatId, senderId, "ADMIN", text);
            
            System.out.println("✅ Message sent successfully");
            return ResponseEntity.ok(Map.of("success", true, "message", "Message sent"));
            
        } catch (IllegalArgumentException ex) {
            System.err.println("❌ IllegalArgumentException: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            System.err.println("❌ Exception: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "failed to send message", "details", ex.getMessage()));
        }
    }
}
