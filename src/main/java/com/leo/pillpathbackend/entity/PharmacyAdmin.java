package com.leo.pillpathbackend.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.leo.pillpathbackend.entity.enums.UserType;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("PHARMACY_ADMIN")
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyAdmin extends User {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;

    @Column(name = "position")
    private String position;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "is_primary_admin")
    private Boolean isPrimaryAdmin = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions", columnDefinition = "jsonb")
    private List<String> permissions = new ArrayList<>();

    @Override
    public UserType getUserType() {
        return UserType.PHARMACY_ADMIN;
    }
}
