package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.entity.Announcement;
import com.leo.pillpathbackend.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementSchedulerService {

    private final AnnouncementRepository announcementRepository;

    // Runs every day at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void archiveExpiredAnnouncements() {
        LocalDate today = LocalDate.now();

        log.info("Starting automatic archiving process for expired announcements");

        // Find all active announcements that have expired
        List<Announcement> expiredAnnouncements = announcementRepository
                .findByActiveTrueAndExpiryDateBefore(today);

        if (!expiredAnnouncements.isEmpty()) {
            log.info("Found {} expired announcements to archive", expiredAnnouncements.size());

            // Set active = false for each expired announcement
            for (Announcement announcement : expiredAnnouncements) {
                announcement.setActive(false);
                log.info("Archived announcement: '{}' (ID: {}, Expired: {})",
                        announcement.getTitle(),
                        announcement.getId(),
                        announcement.getExpiryDate());
            }

            // Save all changes to database
            announcementRepository.saveAll(expiredAnnouncements);
            log.info("Successfully archived {} announcements", expiredAnnouncements.size());
        } else {
            log.info("No expired announcements found to archive");
        }
    }

    
}