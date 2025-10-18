package com.leo.pillpathbackend.security.google;

public record GoogleProfile(
        String sub,
        String email,
        boolean emailVerified,
        String name
) {}