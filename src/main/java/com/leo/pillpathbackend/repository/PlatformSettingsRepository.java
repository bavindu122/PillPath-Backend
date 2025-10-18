package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.PlatformSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformSettingsRepository extends JpaRepository<PlatformSettings, Long> {
}

