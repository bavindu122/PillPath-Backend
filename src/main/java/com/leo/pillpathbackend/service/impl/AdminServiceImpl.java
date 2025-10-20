package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.*;
import com.leo.pillpathbackend.entity.*;
import com.leo.pillpathbackend.repository.UserRepository;
import com.leo.pillpathbackend.service.AdminService;
import org.springframework.stereotype.Service;
import com.leo.pillpathbackend.repository.PrescriptionRepository;
import com.leo.pillpathbackend.repository.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import com.leo.pillpathbackend.repository.PharmacyRepository;
import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus;
import com.leo.pillpathbackend.dto.OverviewChartsResponseDTO;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import com.leo.pillpathbackend.dto.AdminAnalyticsChartsDTO;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.leo.pillpathbackend.repository.AnnouncementRepository;
import com.leo.pillpathbackend.dto.ModeratorListItemDTO;
import com.leo.pillpathbackend.entity.enums.AdminLevel;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import com.leo.pillpathbackend.repository.PrescriptionSubmissionRepository;
import com.leo.pillpathbackend.entity.enums.PrescriptionStatus;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final AnnouncementRepository announcementRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final PharmacyRepository pharmacyRepository;
    private final com.leo.pillpathbackend.repository.PharmacyOrderRepository pharmacyOrderRepository; // New repository for pharmacy orders
    private final PasswordEncoder passwordEncoder;
    private final PrescriptionSubmissionRepository prescriptionSubmissionRepository;

    private static final DateTimeFormatter CUSTOMER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // You can inject other repositories here as needed:
    // private final PharmacyRepository pharmacyRepository;
    // private final OrderRepository orderRepository

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

    @Override
    public List<CustomerDTO> getAllCustomers() {
        // Assuming you have a method in UserRepository to fetch all customers
        List<User> customers = userRepository.findAllCustomers();
        List<CustomerDTO> dtos = new ArrayList<>();
        for (User user : customers) {
            CustomerDTO dto = new CustomerDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setFullName(user.getFullName());
            dto.setPhoneNumber(user.getPhoneNumber());
            dto.setDateOfBirth(user.getDateOfBirth());
            dto.setAddress(user.getAddress());
            dto.setProfilePictureUrl(user.getProfilePictureUrl());
            dto.setIsActive(user.getIsActive());
            dto.setSuspendReason(user.getSuspendReason());
            dto.setCreatedAt(user.getCreatedAt());
            dto.setPrescriptionCount((int) prescriptionRepository.countByCustomerId(user.getId()));
            dto.setOrderCount((int) customerOrderRepository.countByCustomerId(user.getId()));

            // Set other fields as needed
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public void suspendCustomer(Long id, String suspendReason) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setIsActive(false);
        user.setSuspendReason(suspendReason);
        userRepository.save(user);
    }

    @Override
    public void activateCustomer(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setIsActive(true);

        userRepository.save(user);
    }

    @Override
    public List<AdminPrescriptionDTO> getAllPrescriptionsForAdmin() {
        List<Prescription> prescriptions = prescriptionRepository.findAll();
        List<AdminPrescriptionDTO> dtos = new ArrayList<>();
        for (Prescription p : prescriptions) {
            AdminPrescriptionDTO dto = new AdminPrescriptionDTO();
            dto.setId(p.getCode()); // or p.getId().toString()
            dto.setPatient(p.getCustomer().getFullName());
            dto.setPharmacy(p.getPharmacy().getName());
            // Fetch submission for this prescription-pharmacy pair
            var subOpt = prescriptionSubmissionRepository.findByPrescriptionIdAndPharmacyId(p.getId(), p.getPharmacy().getId());
            String status = subOpt.map(s -> s.getStatus().name()).orElse(PrescriptionStatus.REJECTED.name());
            dto.setStatus(status);
            // total_price from prescription_submission
            String total = subOpt.map(s -> s.getTotalPrice())
                    .map(java.math.BigDecimal::toPlainString)
                    .orElse("0");
            dto.setTotalPrice(total);
            dto.setSubmitted(p.getCreatedAt().toLocalDate().toString());
            dto.setPatientImage(p.getCustomer().getProfilePictureUrl());
            dto.setPharmacyImage(p.getPharmacy().getImageUrl());
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public OverviewSummaryDTO getOverviewSummary() {
        int totalUsers = (int) userRepository.count();
        int activePharmacies = pharmacyRepository.countActivePharmacies() != null
                ? pharmacyRepository.countActivePharmacies().intValue()
                : 0;
        int prescriptionsUploaded = (int) prescriptionRepository.count();
        int completedOrders = (int) customerOrderRepository.countByStatus(CustomerOrderStatus.COMPLETED);
        double totalRevenue = customerOrderRepository.sumTotal(); // placeholder: total of all orders
        double walletBalance = 0.0; // TODO: implement wallet balance source
        return OverviewSummaryDTO.builder()
                .totalUsers(totalUsers)
                .activePharmacies(activePharmacies)
                .prescriptionsUploaded(prescriptionsUploaded)
                .completedOrders(completedOrders)
                .totalRevenue(totalRevenue)
                .walletBalance(walletBalance)
                .currency("LKR")
                .build();
    }

    @Override
    public OverviewChartsResponseDTO getOverviewCharts() {
        // Use server timezone
        ZoneId zone = ZoneId.systemDefault();
        LocalDate now = LocalDate.now(zone);
        int months = 6;

        // Build last 6 months inclusive of current
        List<OverviewChartsResponseDTO.UserRegistrationTrendItem> userTrend = new ArrayList<>();
        List<OverviewChartsResponseDTO.PharmacyOnboardingItem> pharmacyTrend = new ArrayList<>();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).withDayOfMonth(1); // exclusive end
            String monthLabel = monthStart.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            String isoMonth = monthStart.toString().substring(0, 7);

            int users = (int) userRepository.countByCreatedAtBetween(monthStart.atStartOfDay(), monthEnd.atStartOfDay());
            userTrend.add(OverviewChartsResponseDTO.UserRegistrationTrendItem.builder()
                    .month(monthLabel)
                    .isoMonth(isoMonth)
                    .users(users)
                    .build());

            int pharmacies = (int) pharmacyRepository.countByCreatedAtBetween(monthStart.atStartOfDay(), monthEnd.atStartOfDay());
            pharmacyTrend.add(OverviewChartsResponseDTO.PharmacyOnboardingItem.builder()
                    .month(monthLabel)
                    .isoMonth(isoMonth)
                    .pharmacies(pharmacies)
                    .build());
        }

        // Role distribution snapshot
        List<OverviewChartsResponseDTO.RoleDistributionItem> roles = new ArrayList<>();
        roles.add(OverviewChartsResponseDTO.RoleDistributionItem.builder()
                .name("Customers").value(userRepository.countCustomers().intValue()).build());
        roles.add(OverviewChartsResponseDTO.RoleDistributionItem.builder()
                .name("Pharmacists").value(userRepository.countPharmacists().intValue()).build());
        roles.add(OverviewChartsResponseDTO.RoleDistributionItem.builder()
                .name("Pharmacy Admins").value(userRepository.countPharmacyAdmins().intValue()).build());
        roles.add(OverviewChartsResponseDTO.RoleDistributionItem.builder()
                .name("System Admins").value(userRepository.countSystemAdmins().intValue()).build());

        return OverviewChartsResponseDTO.builder()
                .userRegistrationTrend(userTrend)
                .userRolesDistribution(roles)
                .pharmacyOnboardingData(pharmacyTrend)
                .build();
    }

    @Override
    public AdminKpisDTO getKpis() {
        int totalUsers = (int) userRepository.count();
        int totalPharmacies = (int) pharmacyRepository.count();
        int totalPrescriptionsUploaded = (int) prescriptionRepository.count();

        // Spec mentions COMPLETED and FULFILLED; enum has no FULFILLED -> count COMPLETED only
        int ordersProcessed = (int) customerOrderRepository.countByStatusIn(
                Collections.singletonList(CustomerOrderStatus.COMPLETED)
        );

        int activePharmacies = (int) pharmacyRepository.countByIsActiveTrue();
        int suspendedPharmacies = (int) pharmacyRepository.countByIsActiveFalse();

        double totalPayments = 0.0; // placeholder; implement when payment source available

        return AdminKpisDTO.builder()
                .totalUsers(totalUsers)
                .totalPharmacies(totalPharmacies)
                .totalPrescriptionsUploaded(totalPrescriptionsUploaded)
                .ordersProcessed(ordersProcessed)
                .activePharmacies(activePharmacies)
                .suspendedPharmacies(suspendedPharmacies)
                .totalPayments(totalPayments)
                .build();
    }

    @Override
    public AdminAnalyticsChartsDTO getAnalyticsCharts(Integer year) {
        int y = (year == null || year <= 0) ? Year.now().getValue() : year;
        List<AdminAnalyticsChartsDTO.PrescriptionUploadsItem> prescriptionUploads = new ArrayList<>();
        List<AdminAnalyticsChartsDTO.PharmacyRegistrationsItem> pharmacyRegistrations = new ArrayList<>();
        List<AdminAnalyticsChartsDTO.GrowthRegistrationsItem> growthRegistrations = new ArrayList<>();

        // Build 12 calendar months for the requested year
        for (int month = 1; month <= 12; month++) {
            LocalDate monthStart = LocalDate.of(y, month, 1);
            LocalDate monthEnd = monthStart.plusMonths(1); // exclusive end
            String monthLabel = monthStart.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            int uploads = (int) prescriptionRepository.countByCreatedAtBetween(monthStart.atStartOfDay(), monthEnd.atStartOfDay());
            prescriptionUploads.add(AdminAnalyticsChartsDTO.PrescriptionUploadsItem.builder()
                    .month(monthLabel)
                    .uploads(uploads)
                    .build());

            int registeredPharmacies = (int) pharmacyRepository.countByCreatedAtBetween(monthStart.atStartOfDay(), monthEnd.atStartOfDay());
            pharmacyRegistrations.add(AdminAnalyticsChartsDTO.PharmacyRegistrationsItem.builder()
                    .month(monthLabel)
                    .registered(registeredPharmacies)
                    .build());

            int newCustomers = (int) userRepository.countCustomersByCreatedAtBetween(monthStart.atStartOfDay(), monthEnd.atStartOfDay());
            int newPharmacies = registeredPharmacies; // pharmacy registrations in the month
            growthRegistrations.add(AdminAnalyticsChartsDTO.GrowthRegistrationsItem.builder()
                    .month(monthLabel)
                    .customers(newCustomers)
                    .pharmacies(newPharmacies)
                    .build());
        }

        // Order fulfillment (total counts for the selected year)
        LocalDate yearStart = LocalDate.of(y, 1, 1);
        LocalDate yearEnd = yearStart.plusYears(1); // exclusive end
        int delivered = (int) customerOrderRepository.countByStatusInAndCreatedAtBetween(
                java.util.List.of(CustomerOrderStatus.COMPLETED), yearStart.atStartOfDay(), yearEnd.atStartOfDay());
        int pending = (int) customerOrderRepository.countByStatusInAndCreatedAtBetween(
                java.util.List.of(CustomerOrderStatus.PENDING, CustomerOrderStatus.PAID), yearStart.atStartOfDay(), yearEnd.atStartOfDay());
        int cancelled = (int) customerOrderRepository.countByStatusInAndCreatedAtBetween(
                java.util.List.of(CustomerOrderStatus.CANCELLED), yearStart.atStartOfDay(), yearEnd.atStartOfDay());

        List<AdminAnalyticsChartsDTO.OrderFulfillmentItem> orderFulfillment = new ArrayList<>();
        orderFulfillment.add(AdminAnalyticsChartsDTO.OrderFulfillmentItem.builder().status("delivered").count(delivered).build());
        orderFulfillment.add(AdminAnalyticsChartsDTO.OrderFulfillmentItem.builder().status("pending").count(pending).build());
        orderFulfillment.add(AdminAnalyticsChartsDTO.OrderFulfillmentItem.builder().status("cancelled").count(cancelled).build());

        return AdminAnalyticsChartsDTO.builder()
                .prescriptionUploads(prescriptionUploads)
                .pharmacyRegistrations(pharmacyRegistrations)
                .growthRegistrations(growthRegistrations)
                .orderFulfillment(orderFulfillment)
                .build();
    }

    @Override
    public List<PharmacyPerformanceResponseDTO> getPharmacyPerformance() {
        List<com.leo.pillpathbackend.entity.Pharmacy> pharmacies = pharmacyRepository.findAll();
        List<PharmacyPerformanceResponseDTO> result = new ArrayList<>();
        for (com.leo.pillpathbackend.entity.Pharmacy p : pharmacies) {
            long fulfilled = pharmacyOrderRepository.countByPharmacyIdAndStatus(p.getId(), com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus.HANDED_OVER);
            String status = mapStatus(p.getIsActive(), p.getIsVerified());
            String regDate = null;
            if (p.getCreatedAt() != null) {
                regDate = p.getCreatedAt()
                        .atZone(ZoneId.systemDefault())
                        .withZoneSameInstant(ZoneId.of("UTC"))
                        .toInstant()
                        .toString();
            }
            double rating = p.getAverageRating() != null ? p.getAverageRating() : 0.0;
            result.add(PharmacyPerformanceResponseDTO.builder()
                    .pharmacyId(formatPharmacyId(p.getId()))
                    .name(p.getName())
                    .ordersFulfilled(fulfilled)
                    .rating(rating)
                    .status(status)
                    .registrationDate(regDate)
                    .build());
        }
        return result;
    }

    @Override
    public List<CustomerActivityResponseDTO> getCustomerActivity() {
        List<User> customers = userRepository.findAllCustomers();
        List<CustomerActivityResponseDTO> result = new ArrayList<>();
        for (User u : customers) {
            long uploads = prescriptionRepository.countByCustomerId(u.getId());
            String status = Boolean.TRUE.equals(u.getIsActive()) ? "Active" : "Suspended";
            String regDate = u.getCreatedAt() != null ? u.getCreatedAt().format(CUSTOMER_DATE_FORMAT) : null;
            result.add(CustomerActivityResponseDTO.builder()
                    .customerId(formatCustomerId(u.getId()))
                    .name(u.getFullName() != null ? u.getFullName() : u.getUsername())
                    .prescriptionsUploaded(uploads)
                    .status(status)
                    .registrationDate(regDate)
                    .build());
        }
        return result;
    }

    @Override
    public List<SuspendedAccountDTO> getSuspendedAccounts() {
        List<SuspendedAccountDTO> result = new ArrayList<>();

        // Pharmacies: suspended = isActive=false AND isVerified=true
        List<com.leo.pillpathbackend.entity.Pharmacy> suspendedPharmacies = pharmacyRepository.findSuspendedPharmacies();
        for (com.leo.pillpathbackend.entity.Pharmacy p : suspendedPharmacies) {
            result.add(SuspendedAccountDTO.builder()
                    .type("Pharmacy")
                    .id(formatPharmacyId(p.getId()))
                    .name(p.getName())
                    .reason("") // no field available in entity
                    .suspendedAt("") // not tracked
                    .build());
        }

        // Customers: suspended if isActive=false
        List<User> customers = userRepository.findAllCustomers();
        for (User u : customers) {
            if (Boolean.FALSE.equals(u.getIsActive())) {
                result.add(SuspendedAccountDTO.builder()
                        .type("Customer")
                        .id(formatUserId(u.getId()))
                        .name(u.getFullName() != null ? u.getFullName() : u.getUsername())
                        .reason(u.getSuspendReason() != null ? u.getSuspendReason() : "")
                        .suspendedAt("") // not tracked
                        .build());
            }
        }

        return result;
    }

    private String formatUserId(Long id) {
        if (id == null) return null;
        String s = String.valueOf(id);
        if (s.length() < 3) s = String.format("%03d", id);
        return "usr_" + s;
    }

    private String formatCustomerId(Long id) {
        if (id == null) return null;
        String s = String.valueOf(id);
        if (s.length() < 3) s = String.format("%03d", id);
        return "cus_" + s;
    }

    private String formatPharmacyId(Long id) {
        if (id == null) return null;
        String s = String.valueOf(id);
        if (s.length() < 3) {
            s = String.format("%03d", id);
        }
        return "ph_" + s;
    }

    private String formatModeratorId(Long id) {
        if (id == null) return null;
        String s = String.valueOf(id);
        if (s.length() < 3) s = String.format("%03d", id);
        return "mod_" + s;
    }

    private String mapStatus(Boolean isActive, Boolean isVerified) {
        boolean active = Boolean.TRUE.equals(isActive);
        boolean verified = Boolean.TRUE.equals(isVerified);
        if (active && verified) return "Active";
        if (!active && verified) return "Suspended";
        // When not verified, treat as Pending (includes inactive+unverified which may be Rejected elsewhere)
        return "Pending";
    }

    @Override
    public ModeratorCreateResponse addModerator(ModeratorCreateRequest request) {
        if (request == null || request.getUsername() == null || request.getUsername().trim().isEmpty() ||
                request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password are required");
        }

        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Generate a synthetic email to satisfy not-null+unique constraint
        String email = ("mod_" + username + "@pillpath.local").toLowerCase();
        if (userRepository.existsByEmail(email)) {
            email = ("mod_" + username + "+1@pillpath.local").toLowerCase();
        }

        Admin moderator = new Admin();
        moderator.setUsername(username);
        moderator.setPassword(passwordEncoder.encode(request.getPassword()));
        moderator.setEmail(email);
        moderator.setFullName("Moderator");
        moderator.setIsActive(true);
        moderator.setEmailVerified(true);
        // Admin-specific
        moderator.setAdminLevel(com.leo.pillpathbackend.entity.enums.AdminLevel.STANDARD);
        moderator.setDepartment("Administration");
        moderator.setEmployeeId("MOD" + System.currentTimeMillis());

        Admin saved = (Admin) userRepository.save(moderator);
        return ModeratorCreateResponse.builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .message("Moderator created successfully")
                .build();
    }

    @Override
    public List<ModeratorListItemDTO> getModerators() {
        List<Admin> admins = userRepository.findAdminsByLevel(AdminLevel.STANDARD);
        return admins.stream().map(a -> ModeratorListItemDTO.builder()
                .id(a.getEmployeeId()) // use emp_id as id
                .username(a.getUsername())
                .createdAt(a.getCreatedAt() != null ? a.getCreatedAt().format(CUSTOMER_DATE_FORMAT) : null)
                .build()).collect(Collectors.toList());
    }

    @Override
    public void deleteModerator(String idOrCode) {
        String empId = (idOrCode == null) ? null : idOrCode.trim();
        if (empId == null || empId.isEmpty()) {
            throw new IllegalArgumentException("Moderator id (emp_id) is required");
        }
        Admin admin = userRepository.findAdminByEmployeeId(empId)
                .orElseThrow(() -> new RuntimeException("Moderator not found"));
        if (admin.getAdminLevel() != AdminLevel.STANDARD) {
            throw new RuntimeException("Cannot delete non-moderator admin");
        }
        userRepository.deleteById(admin.getId());
    }
}
