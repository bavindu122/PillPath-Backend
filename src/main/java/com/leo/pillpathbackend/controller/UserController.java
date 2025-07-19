package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.UserDTO;
import com.leo.pillpathbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


//
//    @GetMapping("/{id}")
//    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
//        UserDTO user = userService.getUserById(id);
//        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
//    }
//
//    @GetMapping("/username/{username}")
//    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
//        UserDTO user = userService.getUserByUsername(username);
//        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
//    }
//
//    @GetMapping("/email/{email}")
//    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
//        UserDTO user = userService.getUserByEmail(email);
//        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
//    }
//
//    @PutMapping
//    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDTO) {
//        UserDTO updatedUser = userService.updateUser(userDTO);
//        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.badRequest().build();
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
//        userService.deleteUser(id);
//        return ResponseEntity.noContent().build();
//    }
}