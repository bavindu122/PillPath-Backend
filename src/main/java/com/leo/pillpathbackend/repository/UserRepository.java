package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.User;
import com.leo.pillpathbackend.entity.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameAndPassword(String username, String password);

//    List<User> findByUserType(UserType userType);
//
//    boolean existsByUsername(String username);
//
//    boolean existsByEmail(String email);
}