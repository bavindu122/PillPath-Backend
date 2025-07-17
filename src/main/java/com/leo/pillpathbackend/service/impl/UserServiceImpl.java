package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.UserDTO;
import com.leo.pillpathbackend.repository.UserRepository;
import com.leo.pillpathbackend.service.UserService;
import com.leo.pillpathbackend.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final Mapper mapper;

    @Override
    public UserDTO createUser(UserDTO userDTO) {
      return null;
    }

    @Override
    public UserDTO getUserById(int id) {
        return null;
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO) {
        return null;
    }

    @Override
    public void deleteUser(int id) {

    }

    @Override
    public UserDTO getUserByUsername(String username) {
        return null;
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        return null;
    }

    @Override
    public boolean validateUser(String username, String password) {
        return false;
    }
}
