package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.PharmacistCreateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistUpdateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistResponseDTO;

import java.util.List;

public interface PharmacistService {
    PharmacistResponseDTO createPharmacist(PharmacistCreateRequestDTO request);
    PharmacistResponseDTO updatePharmacist(Long pharmacistId, PharmacistUpdateRequestDTO request);
    PharmacistResponseDTO getPharmacistById(Long pharmacistId);
    List<PharmacistResponseDTO> getPharmacistsByPharmacyId(Long pharmacyId);
    List<PharmacistResponseDTO> getPharmacistsByPharmacy(Long pharmacyId);
    List<PharmacistResponseDTO> searchPharmacistsByPharmacyId(Long pharmacyId, String searchTerm);
    void deletePharmacist(Long pharmacistId);
    PharmacistResponseDTO togglePharmacistStatus(Long pharmacistId, Boolean isActive);
    long getPharmacistCountByPharmacyId(Long pharmacyId);
    boolean isPharmacistInPharmacy(Long pharmacistId, Long pharmacyId);

    // NEW: Profile picture methods
    PharmacistResponseDTO updateProfilePicture(Long pharmacistId, String imageUrl);
    PharmacistResponseDTO removeProfilePicture(Long pharmacistId);
}