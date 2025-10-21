package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerActivityResponseDTO {
    private String customerId;
    private String name;
    private long prescriptionsUploaded;
    private String status; // Active | Suspended
    private String registrationDate; // yyyy-MM-dd HH:mm:ss
}

