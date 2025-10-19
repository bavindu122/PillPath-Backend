package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.AddAnnouncementRequest;
import com.leo.pillpathbackend.dto.AdminAnalyticsChartsDTO;
import com.leo.pillpathbackend.dto.AdminDashboardResponseDTO;
import com.leo.pillpathbackend.dto.AdminKpisDTO;
import com.leo.pillpathbackend.dto.AdminPrescriptionDTO;
import com.leo.pillpathbackend.dto.CustomerDTO;
import com.leo.pillpathbackend.dto.OverviewChartsResponseDTO;
import com.leo.pillpathbackend.dto.OverviewSummaryDTO;
import com.leo.pillpathbackend.entity.Announcement;
import com.leo.pillpathbackend.dto.PharmacyPerformanceResponseDTO;
import com.leo.pillpathbackend.dto.CustomerActivityResponseDTO;
import com.leo.pillpathbackend.dto.SuspendedAccountDTO;

import java.util.List;

public interface AdminService {
    AdminDashboardResponseDTO getDashboardData();
    Announcement addAnnouncement(AddAnnouncementRequest request);
    List<Announcement> getAllAnnouncementsLatestFirst();
    Announcement updateAnnouncement(Long id, AddAnnouncementRequest request);
    Announcement toggleAnnouncementStatus(Long id);
    void deleteAnnouncement(Long id);
    List<CustomerDTO> getAllCustomers();

    void suspendCustomer(Long id, String suspendReason);

    void activateCustomer(Long id);

    List<AdminPrescriptionDTO> getAllPrescriptionsForAdmin();

    OverviewSummaryDTO getOverviewSummary();

    OverviewChartsResponseDTO getOverviewCharts();

    AdminKpisDTO getKpis();

    AdminAnalyticsChartsDTO getAnalyticsCharts(Integer year);

    // New: analytics
    List<PharmacyPerformanceResponseDTO> getPharmacyPerformance();
    List<CustomerActivityResponseDTO> getCustomerActivity();
    List<SuspendedAccountDTO> getSuspendedAccounts();


    // Future admin methods can go here:
    // List<UserDTO> getAllUsers();
    // void deactivateUser(Long userId);
    // etc.
}