package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.order.*;
import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
import com.leo.pillpathbackend.dto.PharmacyReviewRequest;
import com.leo.pillpathbackend.dto.PharmacyReviewResponse;

import java.util.List;

public interface OrderService {
    CustomerOrderDTO placeOrder(Long customerId, PlaceOrderRequestDTO request);
    CustomerOrderDTO getCustomerOrder(Long customerId, String orderCode);
    PharmacyOrderDTO getCustomerPharmacyOrderByCode(Long customerId, String pharmacyOrderCode);
    List<CustomerOrderDTO> listCustomerOrders(Long customerId, boolean includeItems);
    
    // Existing pharmacist methods
    PharmacyOrderDTO getPharmacyOrder(Long pharmacistId, Long pharmacyOrderId);
    List<PharmacyOrderDTO> listPharmacyOrders(Long pharmacistId, PharmacyOrderStatus status);
    PharmacyOrderDTO updatePharmacyOrderStatus(Long pharmacistId, Long pharmacyOrderId, PharmacyOrderStatus status);
    
    // ✅ NEW: Pharmacy admin methods
    PharmacyOrderDTO getPharmacyOrderByAdmin(Long pharmacyAdminId, Long pharmacyOrderId);
    List<PharmacyOrderDTO> listPharmacyOrdersByAdmin(Long pharmacyAdminId, PharmacyOrderStatus status);
    PharmacyOrderDTO updatePharmacyOrderStatusByAdmin(Long pharmacyAdminId, Long pharmacyOrderId, PharmacyOrderStatus status);
    
    CustomerOrderDTO payOrder(Long customerId, String orderCode, PayOrderRequestDTO request);
    void assignOrderToFamilyMember(String orderCode, Long customerId, Long familyMemberId);
    PharmacyReviewResponse submitPharmacyReview(Long customerId, String orderCode, Long pharmacyId, PharmacyReviewRequest request);
}





















