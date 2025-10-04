package com.leo.pillpathbackend.dto.orderpreview;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderPreviewActionsDTO {
    private boolean canViewOrderPreview;
    private boolean canProceedToPayment;
}

