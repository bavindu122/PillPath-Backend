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

    // Getters
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

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setLastPrescriptionDate(Date lastPrescriptionDate) {
        this.lastPrescriptionDate = lastPrescriptionDate;
    }

    public void setActivePrescriptions(Integer activePrescriptions) {
        this.activePrescriptions = activePrescriptions;
    }

    public void setTotalPrescriptions(Integer totalPrescriptions) {
        this.totalPrescriptions = totalPrescriptions;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public void setMedicalConditions(List<String> medicalConditions) {
        this.medicalConditions = medicalConditions;
    }

    public void setCurrentMedications(List<String> currentMedications) {
        this.currentMedications = currentMedications;
    }
}