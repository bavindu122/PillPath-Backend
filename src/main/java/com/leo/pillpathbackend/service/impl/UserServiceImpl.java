package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.*;
import com.leo.pillpathbackend.entity.Admin;
import com.leo.pillpathbackend.entity.Customer;
import com.leo.pillpathbackend.entity.PharmacyAdmin;
import com.leo.pillpathbackend.entity.User;
import com.leo.pillpathbackend.repository.UserRepository;
import com.leo.pillpathbackend.service.UserService;
import com.leo.pillpathbackend.util.Mapper;
import com.leo.pillpathbackend.util.JwtService;
import com.leo.pillpathbackend.util.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public UnifiedLoginResponse unifiedLogin(UnifiedLoginRequest request) {
        // Validate input
        if (request.getEmail() == null || request.getEmail().trim().isEmpty() ||
                request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return UnifiedLoginResponse.builder()
                    .success(false)
                    .message("Email and password are required")
                    .build();
        }

        String email = request.getEmail().trim().toLowerCase();

        // Check in User repository first to identify user type
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return UnifiedLoginResponse.builder()
                    .success(false)
                    .message("Invalid email or password")
                    .build();
        }

        User user = userOpt.get();

        // Verify user is active
        if (!user.getIsActive()) {
            return UnifiedLoginResponse.builder()
                    .success(false)
                    .message("Account is deactivated")
                    .build();
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return UnifiedLoginResponse.builder()
                    .success(false)
                    .message("Invalid email or password")
                    .build();
        }

        // Determine user type and create appropriate response
        if (user instanceof Customer customer) {
            CustomerProfileDTO profileDTO = mapper.convertToProfileDTO(customer);
            String token = jwtService.generateToken(customer.getId(), "CUSTOMER");
            return UnifiedLoginResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .token(token)
                    .userType("CUSTOMER")
                    .userId(customer.getId())
                    .userProfile(profileDTO)
                    .build();
        } else if (user instanceof PharmacyAdmin admin) {
            PharmacyAdminProfileDTO profileDTO = mapper.convertToPharmacyAdminProfileDTO(admin);
            String token = jwtService.generateToken(admin.getId(), "PHARMACY_ADMIN");
            return UnifiedLoginResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .token(token)
                    .userType("PHARMACY_ADMIN")
                    .userId(admin.getId())
                    .userProfile(profileDTO)
                    .build();
        }
        // Add other user types as needed (Pharmacist, etc.)

        // Fallback for unhandled user types
        return UnifiedLoginResponse.builder()
                .success(false)
                .message("Unsupported user type")
                .build();
    }
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
        String token = jwtService.generateToken(admin.getId(), "ADMIN");

        return AdminLoginResponse.builder()
                .success(true)
                .message("Login successful")
                .adminId(admin.getId())
                .adminLevel(admin.getAdminLevel().name())
                .token(token)
                .build();
    }

    public void logout(String token) {
        if (token != null && !token.trim().isEmpty()) {
            Date exp = null;
            try {
                exp = jwtService.getExpiration(token);
            } catch (Exception ignored) { }
            if (exp != null) {
                tokenBlacklistService.blacklist(token, exp);
            } else {
                tokenBlacklistService.blacklist(token);
            }
        }
    }
}