package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.AdminLoginRequest;
import com.leo.pillpathbackend.dto.AdminLoginResponse;
import com.leo.pillpathbackend.dto.UserDTO;
import com.leo.pillpathbackend.entity.Admin;
import com.leo.pillpathbackend.entity.User;
import com.leo.pillpathbackend.repository.UserRepository;
import com.leo.pillpathbackend.service.UserService;
import com.leo.pillpathbackend.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        throw new UnsupportedOperationException("Cannot create abstract User. Use specific user type services.");
    }

    @Override
    public UserDTO getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(mapper::toUserDTO).orElse(null);
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO) {
        if (userDTO.getId() == null) {
            return null;
        }

        Optional<User> existingUser = userRepository.findById(userDTO.getId());
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            mapper.updateUserFromDTO(user, userDTO);
            User savedUser = userRepository.save(user);
            return mapper.toUserDTO(savedUser);
        }
        return null;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(mapper::toUserDTO).orElse(null);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(mapper::toUserDTO).orElse(null);
    }

    @Override
    public boolean validateUser(String username, String password) {
        Optional<User> user = userRepository.findByUsernameAndPassword(username, password);
        return user.isPresent() && user.get().getIsActive();
    }

    @Override
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public AdminLoginResponse loginAdmin(AdminLoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty()) {
            return AdminLoginResponse.builder()
                    .success(false)
                    .message("Invalid credentials")
                    .build();
        }

        User user = userOpt.get();

        if (!(user instanceof Admin)) {
            return AdminLoginResponse.builder()
                    .success(false)
                    .message("Access denied - Admin privileges required")
                    .build();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return AdminLoginResponse.builder()
                    .success(false)
                    .message("Invalid credentials")
                    .build();
        }

        Admin admin = (Admin) user;

        return AdminLoginResponse.builder()
                .success(true)
                .message("Login successful")
                .adminId(admin.getId())
                .adminLevel(admin.getAdminLevel().name())
                .build();
    }
}