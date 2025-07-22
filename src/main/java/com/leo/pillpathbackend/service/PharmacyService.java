package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PharmacyService {
    // Existing methods
    PharmacyRegistrationResponse registerPharmacy(PharmacyRegistrationRequest request);
//PharmacyAdminLoginResponse loginPharmacyAdmin(String email, String password);
    PharmacyAdminProfileDTO getPharmacyAdminProfileById(Long id);
    PharmacyAdminProfileDTO updatePharmacyAdminProfile(Long id, PharmacyAdminProfileDTO profileDTO);
    boolean verifyPharmacy(Long pharmacyId, boolean approved);

    // New admin management methods
    Page<PharmacyDTO> getAllPharmacies(String searchTerm, String status, Pageable pageable);
    PharmacyStatsDTO getPharmacyStats();
    PharmacyDTO getPharmacyById(Long id);
    PharmacyDTO approvePharmacy(Long pharmacyId);
    PharmacyDTO rejectPharmacy(Long pharmacyId, String reason);
    PharmacyDTO suspendPharmacy(Long pharmacyId, String reason);
    PharmacyDTO activatePharmacy(Long pharmacyId);
    PharmacyDTO updatePharmacyDetails(Long pharmacyId, PharmacyDTO pharmacyDTO);
}