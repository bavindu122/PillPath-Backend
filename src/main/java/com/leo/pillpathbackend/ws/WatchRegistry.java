package com.leo.pillpathbackend.ws;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WatchRegistry {
    // customerId -> adminIds watching
    private final Map<Long, Set<Long>> watchers = new ConcurrentHashMap<>();
    // session -> role->user mapping for cleanup
    private final Map<String, String> sessionRoles = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUsers = new ConcurrentHashMap<>();

    public void registerSession(String sessionId, String role, Long userId) {
        if (sessionId == null) return;
        sessionRoles.put(sessionId, role);
        sessionUsers.put(sessionId, userId);
    }

    public void unregisterSession(String sessionId) {
        if (sessionId == null) return;
        String role = sessionRoles.remove(sessionId);
        Long userId = sessionUsers.remove(sessionId);
        if (role != null && userId != null && "admin".equalsIgnoreCase(role)) {
            for (Set<Long> set : watchers.values()) {
                set.remove(userId);
            }
        }
    }

    public void watch(Long adminId, Long customerId) {
        if (adminId == null || customerId == null) return;
        watchers.computeIfAbsent(customerId, WatchRegistry::newSet).add(adminId);
    }

    private static Set<Long> newSet(Long ignored) {
        return ConcurrentHashMap.newKeySet();
    }

    public Set<Long> getAdmins(Long customerId) {
        return watchers.getOrDefault(customerId, Collections.emptySet());
    }
}
