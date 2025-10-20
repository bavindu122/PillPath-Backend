package com.leo.pillpathbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;
@Data
@Entity
@Table(name = "family_member")
public class FamilyMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String relation;
    private Integer age;
    private String profilePicture;
    private String email;
    private String phone;
    private Date lastPrescriptionDate;
    private Integer activePrescriptions;
    private Integer totalPrescriptions;

    // Use user_id instead of customer_id to match your database
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ElementCollection
    private List<String> allergies;

    private String bloodType;

    @ElementCollection
    private List<String> medicalConditions;

    @ElementCollection
    private List<String> currentMedications;
}