package com.leo.pillpathbackend.ws;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WatchRegistry {
    // customerId -> set of watchers (role + userId)
    private final Map<Long, Set<Watcher>> watchers = new ConcurrentHashMap<>();
    // session -> role/user for cleanup
    private final Map<String, String> sessionRoles = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUsers = new ConcurrentHashMap<>();

    public static record Watcher(String role, Long userId) { }

    public void registerSession(String sessionId, String role, Long userId) {
        if (sessionId == null) return;
        sessionRoles.put(sessionId, role != null ? role.toLowerCase() : null);
        sessionUsers.put(sessionId, userId);
    }

    public void unregisterSession(String sessionId) {
        if (sessionId == null) return;
        String role = sessionRoles.remove(sessionId);
        Long userId = sessionUsers.remove(sessionId);
        if (role != null && userId != null) {
            Watcher w = new Watcher(role.toLowerCase(), userId);
            for (Set<Watcher> set : watchers.values()) {
                set.remove(w);
            }
        }
    }

    public void watch(String role, Long userId, Long customerId) {
        if (role == null || userId == null || customerId == null) return;
        Watcher w = new Watcher(role.toLowerCase(), userId);
        watchers.computeIfAbsent(customerId, WatchRegistry::newSet).add(w);
    }

    public Set<Watcher> getWatchers(Long customerId) {
        return watchers.getOrDefault(customerId, Collections.emptySet());
    }

    private static Set<Watcher> newSet(Long ignored) {
        return ConcurrentHashMap.newKeySet();
    }
}
