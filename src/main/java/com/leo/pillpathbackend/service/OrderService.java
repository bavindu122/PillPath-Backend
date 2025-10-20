package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.order.*;
import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
import java.util.List;

public interface OrderService {
    CustomerOrderDTO placeOrder(Long customerId, PlaceOrderRequestDTO request);
    CustomerOrderDTO getCustomerOrder(Long customerId, String orderCode);
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
}





















// package com.leo.pillpathbackend.service;

// import com.leo.pillpathbackend.dto.order.*;
// import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;



// import java.util.List;

// public interface OrderService {
//     CustomerOrderDTO placeOrder(Long customerId, PlaceOrderRequestDTO request);
//     CustomerOrderDTO getCustomerOrder(Long customerId, String orderCode);
//     List<CustomerOrderDTO> listCustomerOrders(Long customerId, boolean includeItems);

//     PharmacyOrderDTO getPharmacyOrder(Long pharmacistId, Long pharmacyOrderId);
//     List<PharmacyOrderDTO> listPharmacyOrders(Long pharmacistId, PharmacyOrderStatus status);
//     PharmacyOrderDTO updatePharmacyOrderStatus(Long pharmacistId, Long pharmacyOrderId, PharmacyOrderStatus status);

//     CustomerOrderDTO payOrder(Long customerId, String orderCode, PayOrderRequestDTO request);
// }
