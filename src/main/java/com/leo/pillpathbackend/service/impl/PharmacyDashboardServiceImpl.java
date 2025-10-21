package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.PharmacyDashboardStatsDTO;
import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
import com.leo.pillpathbackend.repository.OtcRepository;
import com.leo.pillpathbackend.repository.PharmacistUserRepository;
import com.leo.pillpathbackend.repository.PharmacyOrderRepository;
import com.leo.pillpathbackend.repository.PharmacyRepository;
import com.leo.pillpathbackend.service.PharmacyDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PharmacyDashboardServiceImpl implements PharmacyDashboardService {

    private final PharmacyRepository pharmacyRepository;
    private final PharmacyOrderRepository pharmacyOrderRepository;
    private final OtcRepository otcRepository;
    private final PharmacistUserRepository pharmacistRepository;

    @Override
    @Transactional(readOnly = true)
    public PharmacyDashboardStatsDTO getDashboardStatistics(Long pharmacyId) {
        log.info("Fetching dashboard statistics for pharmacy ID: {}", pharmacyId);

        // Verify pharmacy exists
        if (!pharmacyRepository.existsById(pharmacyId)) {
            throw new IllegalArgumentException("Pharmacy not found with ID: " + pharmacyId);
        }

        // Get total revenue (handed over orders only - completed orders)
        BigDecimal totalRevenue = pharmacyOrderRepository.calculateTotalRevenueByPharmacyId(pharmacyId);

        // Get total orders
        Long totalOrders = pharmacyOrderRepository.countByPharmacyId(pharmacyId);

        // Get total inventory items
        Long totalInventoryItems = otcRepository.countTotalInventoryByPharmacyId(pharmacyId);

        // Get active staff members
        Long activeStaffMembers = pharmacistRepository.countActivePharmacistsByPharmacyId(pharmacyId);

        // Get pending orders (RECEIVED status = waiting to be processed)
        Long pendingOrders = pharmacyOrderRepository.countByPharmacyIdAndStatus(
                pharmacyId, PharmacyOrderStatus.RECEIVED);

        // Get completed orders (HANDED_OVER = successfully completed)
        Long completedOrders = pharmacyOrderRepository.countByPharmacyIdAndStatus(
                pharmacyId, PharmacyOrderStatus.HANDED_OVER);

        // Get low stock items
        Long lowStockItems = otcRepository.countLowStockByPharmacyId(pharmacyId);

        // Get out of stock items
        Long outOfStockItems = otcRepository.countOutOfStockByPharmacyId(pharmacyId);

        log.info("Dashboard stats for pharmacy {}: Revenue={}, Orders={}, Inventory={}, Staff={}",
                pharmacyId, totalRevenue, totalOrders, totalInventoryItems, activeStaffMembers);

        return PharmacyDashboardStatsDTO.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalOrders(totalOrders != null ? totalOrders : 0L)
                .totalInventoryItems(totalInventoryItems != null ? totalInventoryItems : 0L)
                .activeStaffMembers(activeStaffMembers != null ? activeStaffMembers : 0L)
                .pendingOrders(pendingOrders != null ? pendingOrders : 0L)
                .completedOrders(completedOrders != null ? completedOrders : 0L)
                .lowStockItems(lowStockItems != null ? lowStockItems : 0L)
                .outOfStockItems(outOfStockItems != null ? outOfStockItems : 0L)
                .build();
    }
}

