package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.*;
import com.leo.pillpathbackend.entity.User;
import com.leo.pillpathbackend.repository.UserRepository;
import com.leo.pillpathbackend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.leo.pillpathbackend.dto.AddAnnouncementRequest;
import com.leo.pillpathbackend.entity.Announcement;
import com.leo.pillpathbackend.repository.AnnouncementRepository;
import com.leo.pillpathbackend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final AnnouncementRepository announcementRepository;
    // You can inject other repositories here as needed:
    // private final PharmacyRepository pharmacyRepository;
    // private final OrderRepository orderRepository;

    @Override
    public AdminDashboardResponseDTO getDashboardData() {
        return AdminDashboardResponseDTO.builder()
                .summary(getDashboardSummary())
                .userRegistrationTrend(getUserRegistrationTrend())
                .userRolesData(getUserRoleDistribution())
                .recentActivity(getRecentActivity())
                .monthlyOTCData(getMonthlyOTCData())
                .pharmacyOnboardingData(getPharmacyOnboardingData())
                .build();
    }

    private DashboardSummaryDTO getDashboardSummary() {
        return DashboardSummaryDTO.builder()
                .totalUsers(userRepository.count())
                .activePharmacies(25L) // Will come from PharmacyRepository when available
                .pendingPrescriptions(45L) // Will come from PrescriptionRepository when available
                .completedOrders(980L) // Will come from OrderRepository when available
                .totalRevenue(35000.0) // Will come from OrderRepository when available
                .walletBalance(25600.0) // Will come from WalletRepository when available
                .build();
    }

    private List<UserRegistrationTrendDTO> getUserRegistrationTrend() {
        List<Object[]> results = userRepository.getUserRegistrationTrend();
        List<UserRegistrationTrendDTO> trend = new ArrayList<>();

        for (Object[] result : results) {
            trend.add(UserRegistrationTrendDTO.builder()
                    .month((String) result[0])
                    .users(((Number) result[1]).longValue())
                    .build());
        }

        return trend;
    }

    private List<UserRoleDistributionDTO> getUserRoleDistribution() {
        List<UserRoleDistributionDTO> distribution = new ArrayList<>();

        long customerCount = userRepository.countCustomers();
        long pharmacyAdminCount = userRepository.countPharmacyAdmins();
        long adminCount = userRepository.countSystemAdmins();

        distribution.add(UserRoleDistributionDTO.builder()
                .name("Customers").value(customerCount).color("#3b82f6").build());
        distribution.add(UserRoleDistributionDTO.builder()
                .name("Pharmacy Admins").value(pharmacyAdminCount).color("#10b981").build());
        distribution.add(UserRoleDistributionDTO.builder()
                .name("System Admins").value(adminCount).color("#ef4444").build());

        return distribution;
    }

    private List<RecentActivityDTO> getRecentActivity() {
        List<RecentActivityDTO> activities = new ArrayList<>();
        List<User> recentUsers = userRepository.findTop5ByOrderByCreatedAtDesc();

        for (int i = 0; i < recentUsers.size(); i++) {
            User user = recentUsers.get(i);
            activities.add(RecentActivityDTO.builder()
                    .id((long) (i + 1))
                    .user(user.getFullName() != null ? user.getFullName() : user.getUsername())
                    .action("Account created")
                    .time(getTimeAgo(user.getCreatedAt()))
                    .status("success")
                    .build());
        }

        return activities;
    }

    private List<MonthlyDataDTO> getMonthlyOTCData() {
        // Mock data for now - replace with actual repository calls
        List<MonthlyDataDTO> data = new ArrayList<>();
        data.add(MonthlyDataDTO.builder().month("Jan").value(4800L).build());
        data.add(MonthlyDataDTO.builder().month("Feb").value(5200L).build());
        data.add(MonthlyDataDTO.builder().month("Mar").value(4900L).build());
        data.add(MonthlyDataDTO.builder().month("Apr").value(6100L).build());
        data.add(MonthlyDataDTO.builder().month("May").value(5800L).build());
        data.add(MonthlyDataDTO.builder().month("Jun").value(7200L).build());
        return data;
    }

    private List<MonthlyDataDTO> getPharmacyOnboardingData() {
        // Mock data for now - replace with actual repository calls
        List<MonthlyDataDTO> data = new ArrayList<>();
        data.add(MonthlyDataDTO.builder().month("Jan").value(12L).build());
        data.add(MonthlyDataDTO.builder().month("Feb").value(19L).build());
        data.add(MonthlyDataDTO.builder().month("Mar").value(15L).build());
        data.add(MonthlyDataDTO.builder().month("Apr").value(22L).build());
        data.add(MonthlyDataDTO.builder().month("May").value(28L).build());
        data.add(MonthlyDataDTO.builder().month("Jun").value(35L).build());
        return data;
    }

    private String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "Unknown";

        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " minutes ago";
        if (minutes < 1440) return (minutes / 60) + " hours ago";
        return (minutes / 1440) + " days ago";
    }

    @Override
    public Announcement addAnnouncement(AddAnnouncementRequest request) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setExpiryDate(request.getExpiryDate());
        // publishedDate is set automatically by @PrePersist
        return announcementRepository.save(announcement);
    }


    @Override
    public List<Announcement> getAllAnnouncementsLatestFirst() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Announcement updateAnnouncement(Long id, AddAnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found with id: " + id));

        // Update only provided fields (partial update)
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            announcement.setTitle(request.getTitle());
        }
        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            announcement.setContent(request.getContent());
        }
        if (request.getExpiryDate() != null) {
            announcement.setExpiryDate(request.getExpiryDate());
        }

        // publishedDate and other fields remain unchanged
        return announcementRepository.save(announcement);
    }

    @Override
    public Announcement toggleAnnouncementStatus(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found with id: " + id));

        // Toggle the active status
        announcement.setActive(!announcement.isActive());

        // updatedAt will be automatically set by @PreUpdate
        return announcementRepository.save(announcement);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found with id: " + id));

        announcementRepository.delete(announcement);
    }
}