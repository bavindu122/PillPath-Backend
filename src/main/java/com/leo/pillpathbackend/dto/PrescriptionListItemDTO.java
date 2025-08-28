    package com.leo.pillpathbackend.dto;

import com.leo.pillpathbackend.entity.enums.PrescriptionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class PrescriptionListItemDTO
{
    private Long id;
    private String code;
    private String pharmacyName;
    private PrescriptionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

