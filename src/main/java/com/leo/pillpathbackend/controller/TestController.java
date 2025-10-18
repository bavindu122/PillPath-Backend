package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.ChatRoomDTO;
import com.leo.pillpathbackend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final ChatService chatService;

    @GetMapping("/api/v1/v1/chats/threads")
    public ResponseEntity<List<ChatRoomDTO>> getThreads() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !(auth.getPrincipal() instanceof Long)) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            Long userId = (Long) auth.getPrincipal();
            String userType = auth.getAuthorities().stream()
                    .findFirst()
                    .map(grantedAuthority -> {
                        String authority = grantedAuthority.getAuthority();
                        if ("ROLE_USER".equals(authority)) return "CUSTOMER";
                        if ("ROLE_PHARMACIST".equals(authority)) return "PHARMACIST";
                        if ("ROLE_ADMIN".equals(authority)) return "ADMIN";
                        return "CUSTOMER";
                    })
                    .orElse("CUSTOMER");

            List<ChatRoomDTO> chats = chatService.getMyChats(userId, userType);
            return ResponseEntity.ok(chats);
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> simpleTest() {
        return ResponseEntity.ok(Map.of("status", "ok", "message", "Server is running"));
    }
}