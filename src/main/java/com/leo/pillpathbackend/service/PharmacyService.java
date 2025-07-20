package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.PharmacyRegistrationRequest;
import com.leo.pillpathbackend.dto.PharmacyRegistrationResponse;
import com.leo.pillpathbackend.dto.PharmacyAdminLoginResponse;
import com.leo.pillpathbackend.dto.PharmacyAdminProfileDTO;

public interface PharmacyService {
    PharmacyRegistrationResponse registerPharmacy(PharmacyRegistrationRequest request);
    PharmacyAdminLoginResponse loginPharmacyAdmin(String email, String password);
    PharmacyAdminProfileDTO getPharmacyAdminProfileById(Long id);
    PharmacyAdminProfileDTO updatePharmacyAdminProfile(Long id, PharmacyAdminProfileDTO profileDTO);
    boolean verifyPharmacy(Long pharmacyId, boolean approved);
}