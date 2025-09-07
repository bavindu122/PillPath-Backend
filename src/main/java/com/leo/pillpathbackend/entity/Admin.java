package com.leo.pillpathbackend.entity;

import com.leo.pillpathbackend.entity.enums.AdminLevel;
import com.leo.pillpathbackend.entity.enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("ADMIN")
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends User {

    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Column(name = "department")
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_level")
    private AdminLevel adminLevel = AdminLevel.STANDARD;
    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions", columnDefinition = "jsonb")
    private List<String> permissions = new ArrayList<>();

    @Override
    public UserType getUserType() {
        return UserType.ADMIN;
    }
}