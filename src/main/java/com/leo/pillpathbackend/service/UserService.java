package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.UserDTO;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO getUserById(int id);
    UserDTO updateUser(UserDTO userDTO);
    void deleteUser(int id);
    UserDTO getUserByUsername(String username);
    UserDTO getUserByEmail(String email);
    boolean validateUser(String username, String password);
}
