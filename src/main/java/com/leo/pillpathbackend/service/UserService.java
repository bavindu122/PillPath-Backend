package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.*;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO getUserById(Long id);
    UserDTO updateUser(UserDTO userDTO);
    void deleteUser(Long id);
    UserDTO getUserByUsername(String username);
    UserDTO getUserByEmail(String email);
    boolean validateUser(String username, String password);
    public void changePassword(String username, String currentPassword, String newPassword) ;
    public AdminLoginResponse loginAdmin(AdminLoginRequest request);
    // Add this method to UserService.java
    UnifiedLoginResponse unifiedLogin(UnifiedLoginRequest request);
}