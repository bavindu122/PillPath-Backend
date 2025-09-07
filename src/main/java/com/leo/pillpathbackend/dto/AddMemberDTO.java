package com.leo.pillpathbackend.dto;

import java.util.List;
import java.util.Date;

public class AddMemberDTO {
    private String name;
    private String relation;
    private Integer age;
    private String profilePicture;
    private String email;
    private String phone;
    private Date lastPrescriptionDate;
    private Integer activePrescriptions;
    private Integer totalPrescriptions;
    private List<String> allergies;
    private String bloodType;
    private List<String> medicalConditions;
    private List<String> currentMedications;

    public String getName() {
        return name;
    }

    public String getRelation() {
        return relation;
    }

    public Integer getAge() {
        return age;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Date getLastPrescriptionDate() {
        return lastPrescriptionDate;
    }

    public Integer getActivePrescriptions() {
        return activePrescriptions;
    }

    public Integer getTotalPrescriptions() {
        return totalPrescriptions;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public String getBloodType() {
        return bloodType;
    }

    public List<String> getMedicalConditions() {
        return medicalConditions;
    }

    public List<String> getCurrentMedications() {
        return currentMedications;
    }
}