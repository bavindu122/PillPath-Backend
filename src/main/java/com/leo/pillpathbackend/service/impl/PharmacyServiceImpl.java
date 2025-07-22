package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.*;
import com.leo.pillpathbackend.entity.Pharmacy;
import com.leo.pillpathbackend.entity.PharmacyAdmin;
import com.leo.pillpathbackend.repository.PharmacyRepository;
import com.leo.pillpathbackend.repository.PharmacyAdminRepository;
import com.leo.pillpathbackend.service.PharmacyService;
import com.leo.pillpathbackend.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PharmacyServiceImpl implements PharmacyService {

    private final PharmacyRepository pharmacyRepository;
    private final PharmacyAdminRepository pharmacyAdminRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public PharmacyRegistrationResponse registerPharmacy(PharmacyRegistrationRequest request) {
        // Validate inputs
        if (pharmacyRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Pharmacy with this email already exists");
        }

        if (pharmacyRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new RuntimeException("Pharmacy with this license number already exists");
        }

        // Create pharmacy entity
        Pharmacy pharmacy = mapper.convertToPharmacyEntity(request);

        // Initialize collections
        if (pharmacy.getServices() == null) {
            pharmacy.setServices(new ArrayList<>());
        }

        // Save pharmacy first to get ID
        pharmacy = pharmacyRepository.save(pharmacy);

        // Create admin entity
        PharmacyAdmin admin = mapper.convertToPharmacyAdminEntity(request, pharmacy);
        admin = pharmacyAdminRepository.save(admin);

        // Create response
        return mapper.convertToPharmacyRegistrationResponse(pharmacy, admin);
    }

//    @Override
//    public PharmacyAdminLoginResponse loginPharmacyAdmin(String email, String password) {
//        PharmacyAdmin admin = pharmacyAdminRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
//
//        if (!passwordEncoder.matches(password, admin.getPassword())) {
//            throw new RuntimeException("Invalid credentials");
//        }
//
//        if (!admin.getIsActive()) {
//            throw new RuntimeException("Account is inactive");
//        }
//
//        Pharmacy pharmacy = admin.getPharmacy();
//        if (!pharmacy.getIsActive()) {
//            throw new RuntimeException("Pharmacy account is inactive");
//        }
//
//        if (!pharmacy.getIsVerified()) {
//            throw new RuntimeException("Pharmacy is pending verification");
//        }
//
//        return mapper.convertToPharmacyAdminLoginResponse(admin);
//    }

    @Override
    public PharmacyAdminProfileDTO getPharmacyAdminProfileById(Long id) {
        PharmacyAdmin admin = pharmacyAdminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacy admin not found"));

        return mapper.convertToPharmacyAdminProfileDTO(admin);
    }

    @Override
    @Transactional
    public PharmacyAdminProfileDTO updatePharmacyAdminProfile(Long id, PharmacyAdminProfileDTO profileDTO) {
        PharmacyAdmin admin = pharmacyAdminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacy admin not found"));

        // Update fields (add logic to update specific fields as needed)
        if (profileDTO.getFullName() != null) {
            admin.setFullName(profileDTO.getFullName());
        }
        if (profileDTO.getPhoneNumber() != null) {
            admin.setPhoneNumber(profileDTO.getPhoneNumber());
        }
        if (profileDTO.getAddress() != null) {
            admin.setAddress(profileDTO.getAddress());
        }
        if (profileDTO.getProfilePictureUrl() != null) {
            admin.setProfilePictureUrl(profileDTO.getProfilePictureUrl());
        }
        if (profileDTO.getPosition() != null) {
            admin.setPosition(profileDTO.getPosition());
        }

        admin = pharmacyAdminRepository.save(admin);
        return mapper.convertToPharmacyAdminProfileDTO(admin);
    }

    @Override
    @Transactional
    public boolean verifyPharmacy(Long pharmacyId, boolean approved) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));

        pharmacy.setIsVerified(approved);

        // If not approved, maybe set inactive
        if (!approved) {
            pharmacy.setIsActive(false);
        }

        pharmacyRepository.save(pharmacy);
        return approved;
    }

    @Override
    public Page<PharmacyDTO> getAllPharmacies(String searchTerm, String status, Pageable pageable) {
        return null;
    }

    @Override
    public PharmacyStatsDTO getPharmacyStats() {
        return null;
    }

    @Override
    public PharmacyDTO getPharmacyById(Long id) {
        return null;
    }

    @Override
    public PharmacyDTO approvePharmacy(Long pharmacyId) {
        return null;
    }

    @Override
    public PharmacyDTO rejectPharmacy(Long pharmacyId, String reason) {
        return null;
    }

    @Override
    public PharmacyDTO suspendPharmacy(Long pharmacyId, String reason) {
        return null;
    }

    @Override
    public PharmacyDTO activatePharmacy(Long pharmacyId) {
        return null;
    }

    @Override
    public PharmacyDTO updatePharmacyDetails(Long pharmacyId, PharmacyDTO pharmacyDTO) {
        return null;
    }
}