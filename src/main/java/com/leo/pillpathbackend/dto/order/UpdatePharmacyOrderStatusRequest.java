package com.leo.pillpathbackend.dto.order;

import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
import lombok.Data;

@Data
public class UpdatePharmacyOrderStatusRequest {
    private PharmacyOrderStatus status;
}

