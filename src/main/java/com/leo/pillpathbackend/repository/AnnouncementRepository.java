package com.leo.pillpathbackend.repository;


import com.leo.pillpathbackend.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findAllByOrderByCreatedAtDesc();

    // Find announcements that are still active but have expired
    List<Announcement> findByActiveTrueAndExpiryDateBefore(LocalDate date);

}
