package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.PharmacistCreateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistUpdateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistResponseDTO;

import java.util.List;

public interface PharmacistService {
    
    /**
     * Create a new pharmacist
     */
    PharmacistResponseDTO createPharmacist(PharmacistCreateRequestDTO request);

    /**
     * Update an existing pharmacist
     */
    PharmacistResponseDTO updatePharmacist(Long pharmacistId, PharmacistUpdateRequestDTO request);
    
    /**
     * Get pharmacist by ID
     */
    PharmacistResponseDTO getPharmacistById(Long pharmacistId);
    
    /**
     * Get all pharmacists for a specific pharmacy
     */
    List<PharmacistResponseDTO> getPharmacistsByPharmacyId(Long pharmacyId);
    
    /**
     * Search pharmacists by pharmacy ID and search term
     */
    List<PharmacistResponseDTO> searchPharmacistsByPharmacyId(Long pharmacyId, String searchTerm);
    
    /**
     * Delete a pharmacist (soft delete by setting isActive = false)
     */
    void deletePharmacist(Long pharmacistId);
    
    /**
     * Activate/Deactivate a pharmacist
     */
    PharmacistResponseDTO togglePharmacistStatus(Long pharmacistId, Boolean isActive);
    
    /**
     * Get count of pharmacists for a pharmacy
     */
    long getPharmacistCountByPharmacyId(Long pharmacyId);
    
    /**
     * Verify if pharmacist belongs to a pharmacy
     */
    boolean isPharmacistInPharmacy(Long pharmacistId, Long pharmacyId);

    List<PharmacistResponseDTO> getPharmacistsByPharmacy(Long pharmacyId);
}
