package com.leo.pillpathbackend.dto.order;

import com.leo.pillpathbackend.entity.enums.PaymentMethod;
import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderRequestDTO {
    private String prescriptionCode;
    private PaymentMethod paymentMethod; // optional
    private List<PharmacyOrderSelectionDTO> pharmacies; // required
}

