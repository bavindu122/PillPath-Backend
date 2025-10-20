package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.PharmacyDashboardStatsDTO;

public interface PharmacyDashboardService {
    
    PharmacyDashboardStatsDTO getDashboardStatistics(Long pharmacyId);
}