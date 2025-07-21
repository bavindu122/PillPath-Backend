package com.leo.pillpathbackend.config;

import com.leo.pillpathbackend.entity.Admin;
import com.leo.pillpathbackend.entity.enums.AdminLevel;
import com.leo.pillpathbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AdminSeeder {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void seedDefaultAdmin() {
        // Check if any SUPER admin exists
        if (!userRepository.existsByAdminLevel(AdminLevel.SUPER)) {

            Admin superAdmin = new Admin();

            // Set User properties
            superAdmin.setUsername("admin");
            superAdmin.setFullName("System Administrator");
            superAdmin.setEmail("admin@pillpath.com");
            superAdmin.setPhoneNumber("+1234567890");
            superAdmin.setPassword(passwordEncoder.encode("TempPassword123!"));
            superAdmin.setIsActive(true);
            superAdmin.setEmailVerified(true);
            superAdmin.setCreatedAt(LocalDateTime.now());
            superAdmin.setUpdatedAt(LocalDateTime.now());

            // Set Admin-specific properties
            superAdmin.setEmployeeId("EMP001");
            superAdmin.setDepartment("IT");
            superAdmin.setAdminLevel(AdminLevel.SUPER);

            userRepository.save(superAdmin);

            System.out.println("Super Admin created: admin@pillpath.com");
            System.out.println("Please change the default password immediately!");
        }
    }
}