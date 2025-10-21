package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.PharmacyAdminProfileDTO;
import com.leo.pillpathbackend.dto.PharmacyAdminUpdateRequestDTO;

public interface PharmacyAdminService {
    PharmacyAdminProfileDTO getPharmacyAdminById(Long adminId);
    PharmacyAdminProfileDTO updatePharmacyAdmin(Long adminId, PharmacyAdminUpdateRequestDTO request);
    PharmacyAdminProfileDTO updateProfilePicture(Long adminId, String imageUrl);
    PharmacyAdminProfileDTO removeProfilePicture(Long adminId);
}
