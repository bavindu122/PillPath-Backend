// PillPath-Backend/src/main/java/com/leo/pillpathbackend/controller/AdminController.java
package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.AddAnnouncementResponse;
import com.leo.pillpathbackend.dto.AdminDashboardResponseDTO;
import com.leo.pillpathbackend.dto.AddAnnouncementRequest;
import com.leo.pillpathbackend.dto.AddModeratorRequest;
import com.leo.pillpathbackend.entity.Announcement;
import com.leo.pillpathbackend.service.AdminService;
import com.leo.pillpathbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponseDTO> getDashboardData() {
        try {
            return ResponseEntity.ok(adminService.getDashboardData());
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
        return ResponseEntity.ok(adminService.getAllAnnouncementsLatestFirst());
    }

    @PutMapping("/announcements/{id}")
    public ResponseEntity<Announcement> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody AddAnnouncementRequest request) {
        try {
            return ResponseEntity.ok(adminService.updateAnnouncement(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/announcements/{id}/toggle-status")
    public ResponseEntity<Announcement> toggleAnnouncementStatus(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminService.toggleAnnouncementStatus(id));
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

    // New endpoint: add moderator under admin controller
    @PostMapping("/moderators")
    public ResponseEntity<AddModeratorRequest> addModerator(@RequestBody AddModeratorRequest request) {
        AddModeratorRequest created = userService.addModerator(request);
        return ResponseEntity.ok(created);
    }
}