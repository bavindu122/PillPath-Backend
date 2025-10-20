package com.leo.pillpathbackend.dto.otcorder;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long otcProductId;
    private Long pharmacyId;
    private Integer quantity;
}