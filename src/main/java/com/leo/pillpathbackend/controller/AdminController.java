package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.AdminDashboardResponseDTO;
import com.leo.pillpathbackend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.leo.pillpathbackend.dto.AddAnnouncementRequest;
import com.leo.pillpathbackend.dto.AddAnnouncementResponse;
import com.leo.pillpathbackend.entity.Announcement;
import com.leo.pillpathbackend.service.AdminService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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







    // Future admin endpoints:
    // @GetMapping("/users")
    // @PostMapping("/users/{id}/deactivate")
    // etc.
}