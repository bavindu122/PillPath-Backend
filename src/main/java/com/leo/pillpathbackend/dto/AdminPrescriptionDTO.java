package com.leo.pillpathbackend.dto;

import lombok.Data;

@Data
public class AdminPrescriptionDTO {
    private String id;
    private String patient;
    private String pharmacy;
    private String status;
    private String submitted;
    private String totalPrice;
    private String patientImage;
    private String pharmacyImage;
}
