package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.AdminLoginRequest;
import com.leo.pillpathbackend.dto.AdminLoginResponse;
import com.leo.pillpathbackend.dto.ChangePasswordRequest;
import com.leo.pillpathbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")  // Changed from "/users" to "/api/v1/users"
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(request.getUsername(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password changed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to change password: " + e.getMessage());
        }
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AdminLoginResponse> loginAdmin(@RequestBody AdminLoginRequest request) {
        try {
            AdminLoginResponse response = userService.loginAdmin(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AdminLoginResponse.builder()
                            .success(false)
                            .message("Login failed: " + e.getMessage())
                            .build());
        }
    }
}