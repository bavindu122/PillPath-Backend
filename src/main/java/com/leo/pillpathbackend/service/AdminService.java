package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.AdminDashboardResponseDTO;
import com.leo.pillpathbackend.dto.AddAnnouncementRequest;
import com.leo.pillpathbackend.entity.Announcement;

import java.util.List;

public interface AdminService {
    AdminDashboardResponseDTO getDashboardData();
    Announcement addAnnouncement(AddAnnouncementRequest request);
    List<Announcement> getAllAnnouncementsLatestFirst();
    Announcement updateAnnouncement(Long id, AddAnnouncementRequest request);
    Announcement toggleAnnouncementStatus(Long id);


    // Future admin methods can go here:
    // List<UserDTO> getAllUsers();
    // void deactivateUser(Long userId);
    // etc.
}