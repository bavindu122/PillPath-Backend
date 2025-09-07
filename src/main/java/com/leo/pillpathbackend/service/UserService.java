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
    void changePassword(String username, String currentPassword, String newPassword);
    AdminLoginResponse loginAdmin(AdminLoginRequest request);
    UnifiedLoginResponse unifiedLogin(UnifiedLoginRequest request);
    void logout(String token);

    AddModeratorRequest addModerator(AddModeratorRequest request);

}