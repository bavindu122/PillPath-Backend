package com.leo.pillpathbackend.dto.request;

import com.leo.pillpathbackend.entity.enums.DeliveryPreference;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class CreatePrescriptionRequest {
    // Customer selects pharmacies to submit to (new multi support)
    private List<Long> pharmacyIds; // optional
    // Legacy single pharmacy id (will be deprecated)
    private Long pharmacyId;

    // Optional note for pharmacist
    private String note;

    // Delivery preference and optional address
    private DeliveryPreference deliveryPreference;
    private String deliveryAddress;

    // Customer location (optional) for delivery estimation
    private BigDecimal latitude;
    private BigDecimal longitude;
}
