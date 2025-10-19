package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.*;
import com.leo.pillpathbackend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.leo.pillpathbackend.entity.Announcement;
import com.leo.pillpathbackend.service.AdminService;
import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;


@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;


    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponseDTO> getDashboardData() {
        try {
            AdminDashboardResponseDTO dashboardData = adminService.getDashboardData();
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/announcements")
    public ResponseEntity<AddAnnouncementResponse> addAnnouncement(@RequestBody @Valid AddAnnouncementRequest request) {
        Announcement announcement = adminService.addAnnouncement(request);
        AddAnnouncementResponse response = new AddAnnouncementResponse("Announcement added successfully", announcement);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/announcements")
    public ResponseEntity<List<Announcement>> getAllAnnouncements() {
        List<Announcement> announcements = adminService.getAllAnnouncementsLatestFirst();
        return ResponseEntity.ok(announcements);
    }

    @PutMapping("/announcements/{id}")
    public ResponseEntity<Announcement> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody AddAnnouncementRequest request) {

        try {
            Announcement updatedAnnouncement = adminService.updateAnnouncement(id, request);
            return ResponseEntity.ok(updatedAnnouncement);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/announcements/{id}/toggle-status")
    public ResponseEntity<Announcement> toggleAnnouncementStatus(@PathVariable Long id) {
        try {
            Announcement updatedAnnouncement = adminService.toggleAnnouncementStatus(id);
            return ResponseEntity.ok(updatedAnnouncement);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        try {
            adminService.deleteAnnouncement(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        List<CustomerDTO> customers = adminService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @PatchMapping("/customers/{id}/suspend")
    public ResponseEntity<String> suspendCustomer(@PathVariable Long id, @RequestBody SuspendCustomerRequest request) {
        try {
            adminService.suspendCustomer(id, request.getReason());
            return ResponseEntity.ok("Customer suspended successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/customers/{id}/activate")
    public ResponseEntity<String> activateCustomer(@PathVariable Long id) {
        try {
            adminService.activateCustomer(id);
            return ResponseEntity.ok("Customer activated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/prescriptions")
    public ResponseEntity<List<AdminPrescriptionDTO>> getPrescriptionsForAdmin() {
        return ResponseEntity.ok(adminService.getAllPrescriptionsForAdmin());
    }

    // Overview stat cards (all-time, admin only)
    @GetMapping("/overview/summary")
    public ResponseEntity<OverviewSummaryDTO> getOverviewSummary() {
        return ResponseEntity.ok(adminService.getOverviewSummary());
    }

    // Overview charts (last 6 months, admin only)
    @GetMapping("/overview/charts")
    public ResponseEntity<OverviewChartsResponseDTO> getOverviewCharts() {
        return ResponseEntity.ok(adminService.getOverviewCharts());
    }

    // KPIs (all-time aggregates, admin only)
    @GetMapping("/analytics/kpis")
    public ResponseEntity<AdminKpisDTO> getKpis() {
        return ResponseEntity.ok(adminService.getKpis());
    }

    // Analytics charts (12 months for selected year, admin only)
    @GetMapping("/analytics/charts")
    public ResponseEntity<AdminAnalyticsChartsDTO> getAnalyticsCharts(@RequestParam(value = "year", required = false) Integer year) {
        return ResponseEntity.ok(adminService.getAnalyticsCharts(year));
    }

    // Pharmacy performance (no pagination/sorting)
    @GetMapping("/analytics/pharmacy-performance")
    public ResponseEntity<List<PharmacyPerformanceResponseDTO>> getPharmacyPerformance() {
        return ResponseEntity.ok(adminService.getPharmacyPerformance());
    }

    // Customer activity (no pagination/sorting)
    @GetMapping("/analytics/customer-activity")
    public ResponseEntity<List<CustomerActivityResponseDTO>> getCustomerActivity() {
        return ResponseEntity.ok(adminService.getCustomerActivity());
    }

    // Suspended accounts (no pagination/sorting)
    @GetMapping("/analytics/suspended-accounts")
    public ResponseEntity<List<SuspendedAccountDTO>> getSuspendedAccounts() {
        return ResponseEntity.ok(adminService.getSuspendedAccounts());
    }

    // Future admin endpoints:
    // @GetMapping("/users")
    // @PostMapping("/users/{id}/deactivate")
    // etc.
}