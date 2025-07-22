package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.AdminDashboardResponseDTO;

public interface AdminService {
    AdminDashboardResponseDTO getDashboardData();
    // Future admin methods can go here:
    // List<UserDTO> getAllUsers();
    // void deactivateUser(Long userId);
    // etc.
}