package com.leo.pillpathbackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyWithProductDTO {
    private Long pharmacyId;
    private String pharmacyName;
    private String address;
    private String phoneNumber;
    private String email;
    private List<OtcDTO> products;
}