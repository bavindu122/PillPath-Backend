package com.leo.pillpathbackend.repository;


import com.leo.pillpathbackend.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findAllByOrderByCreatedAtDesc();
}
