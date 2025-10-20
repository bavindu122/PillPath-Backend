package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.otcorder.OrderRequestDTO;
import com.leo.pillpathbackend.dto.otcorder.OrderResponseDTO;

import java.util.List;

public interface OtcOrderService {
    OrderResponseDTO createOrder(OrderRequestDTO orderRequest);
    OrderResponseDTO getOrderById(Long orderId);
    OrderResponseDTO getOrderByCode(String orderCode);
    List<OrderResponseDTO> getCustomerOrders(Long customerId);
    OrderResponseDTO updateOrderStatus(Long orderId, String status);
}