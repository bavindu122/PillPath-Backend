package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.UserDTO;
import com.leo.pillpathbackend.entity.User;
import com.leo.pillpathbackend.repository.UserRepository;
import com.leo.pillpathbackend.service.UserService;
import com.leo.pillpathbackend.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Mapper mapper;

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        // Note: Since User is abstract, you'll need to create specific user types
        // This is a placeholder implementation
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
}