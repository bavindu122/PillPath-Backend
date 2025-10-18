package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.ChatMessageHistoryResponse;
import com.leo.pillpathbackend.dto.ChatRoomDTO;
import com.leo.pillpathbackend.dto.SendMessageRequest;
import com.leo.pillpathbackend.dto.StartChatRequest;
import com.leo.pillpathbackend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chats")
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
            return ResponseEntity.status(401).build();
        }

        Long userId = (Long) auth.getPrincipal();

        // Determine user type from authorities
        String userType = auth.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> {
                    String authority = grantedAuthority.getAuthority();
                    if ("ROLE_USER".equals(authority)) return "CUSTOMER";
                    if ("ROLE_PHARMACIST".equals(authority)) return "PHARMACIST";
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
            @RequestParam(defaultValue = "50") int limit) {

        ChatMessageHistoryResponse messages = chatService.getChatMessages(chatId, page, limit);
        return ResponseEntity.ok(messages);
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
                    if ("ROLE_USER".equals(authority)) return "CUSTOMER";
                    if ("ROLE_PHARMACIST".equals(authority)) return "PHARMACIST";
                    return "CUSTOMER";
                })
                .orElse("CUSTOMER");

        int count = chatService.getUnreadCount(userId, userType);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // Persist a message and broadcast
    @PostMapping("/{chatId:\\d+}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable Long chatId, @RequestBody SendMessageRequest body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        }
        Long senderId = (Long) auth.getPrincipal();
        String userType = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .map(role -> {
                    if ("ROLE_USER".equals(role)) return "CUSTOMER";
                    if ("ROLE_PHARMACIST".equals(role)) return "PHARMACIST";
                    if ("ROLE_ADMIN".equals(role)) return "ADMIN";
                    return "CUSTOMER";
                })
                .orElse("CUSTOMER");

        if (body == null || body.getText() == null || body.getText().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "text required"));
        }

        try {
            chatService.persistAndBroadcastMessage(chatId, senderId, userType, body.getText());
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", "failed"));
        }
    }
}
