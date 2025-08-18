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
import java.util.List;
import java.util.stream.Collectors;

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
        // Handle "All" status filter
        if ("All".equals(status)) {
            status = null;
        }

        // Validate status
        if (status != null && !status.isEmpty() && !status.matches("Active|Pending|Suspended|Rejected")) {
            throw new IllegalArgumentException("Invalid status filter");
        }

        // Search and filter pharmacies
        return pharmacyRepository.findPharmaciesWithFilters(
                searchTerm == null ? "" : searchTerm,
                status,
                pageable
        ).map(mapper::convertToPharmacyDTO);
    }


@Override
public PharmacyStatsDTO getPharmacyStats() {
    Long activePharmacies = pharmacyRepository.countByIsActiveTrueAndIsVerifiedTrue();
    Long pendingApproval = pharmacyRepository.countByIsActiveTrueAndIsVerifiedFalse();
    Long suspendedPharmacies = pharmacyRepository.countByIsActiveFalseAndIsVerifiedTrue();
    Long rejectedPharmacies = pharmacyRepository.countByIsActiveFalseAndIsVerifiedFalse();

    return PharmacyStatsDTO.builder()
            .activePharmacies(activePharmacies)
            .pendingApproval(pendingApproval)
            .suspendedPharmacies(suspendedPharmacies)
            .rejectedPharmacies(rejectedPharmacies)
            .build();
}


@Override
    public PharmacyDTO getPharmacyById(Long id) {
        Pharmacy pharmacy = pharmacyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found with ID: " + id));

        return mapper.convertToPharmacyDTO(pharmacy);
    }
    @Override
    @Transactional
    public PharmacyDTO approvePharmacy(Long pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found with ID: " + pharmacyId));

        if (pharmacy.getIsVerified()) {
            throw new RuntimeException("Pharmacy is already verified");
        }

        pharmacy.setIsVerified(true);
        pharmacy.setIsActive(true);
       // Clear any previous rejection reason

        pharmacy = pharmacyRepository.save(pharmacy);
        return mapper.convertToPharmacyDTO(pharmacy);
    }
    @Override
    @Transactional
    public PharmacyDTO rejectPharmacy(Long pharmacyId, String reason) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found with ID: " + pharmacyId));

        if (pharmacy.getIsVerified()) {
            throw new RuntimeException("Cannot reject an already verified pharmacy");
        }

        pharmacy.setIsVerified(false);
        pharmacy.setIsActive(false);


        pharmacy = pharmacyRepository.save(pharmacy);
        return mapper.convertToPharmacyDTO(pharmacy);
    }

    @Override
    @Transactional
    public PharmacyDTO suspendPharmacy(Long pharmacyId, String reason) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found with ID: " + pharmacyId));

        if (!pharmacy.getIsVerified()) {
            throw new RuntimeException("Cannot suspend an unverified pharmacy");
        }

        if (!pharmacy.getIsActive()) {
            throw new RuntimeException("Pharmacy is already suspended");
        }

        pharmacy.setIsActive(false);
        pharmacy = pharmacyRepository.save(pharmacy);
        return mapper.convertToPharmacyDTO(pharmacy);
    }

    @Override
    @Transactional
    public PharmacyDTO activatePharmacy(Long pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found with ID: " + pharmacyId));

        if (!pharmacy.getIsVerified()) {
            throw new RuntimeException("Cannot activate an unverified pharmacy");
        }

        if (pharmacy.getIsActive()) {
            throw new RuntimeException("Pharmacy is already active");
        }

        pharmacy.setIsActive(true);// Clear suspension reason

        pharmacy = pharmacyRepository.save(pharmacy);
        return mapper.convertToPharmacyDTO(pharmacy);
    }

    @Override
    @Transactional
    public PharmacyDTO updatePharmacyDetails(Long pharmacyId, PharmacyDTO pharmacyDTO) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found with ID: " + pharmacyId));

        // Check for email uniqueness if being updated
        if (pharmacyDTO.getEmail() != null && !pharmacyDTO.getEmail().equals(pharmacy.getEmail())) {
            if (pharmacyRepository.existsByEmail(pharmacyDTO.getEmail())) {
                throw new RuntimeException("Pharmacy with this email already exists");
            }
        }

        // Check for license number uniqueness if being updated
        if (pharmacyDTO.getLicenseNumber() != null && !pharmacyDTO.getLicenseNumber().equals(pharmacy.getLicenseNumber())) {
            if (pharmacyRepository.existsByLicenseNumber(pharmacyDTO.getLicenseNumber())) {
                throw new RuntimeException("Pharmacy with this license number already exists");
            }
        }

        // Update pharmacy details using mapper
        mapper.updatePharmacyFromDTO(pharmacy, pharmacyDTO);

        pharmacy = pharmacyRepository.save(pharmacy);
        return mapper.convertToPharmacyDTO(pharmacy);
    }
    @Override
    public List<PharmacyMapDTO> getPharmaciesForMap(Double userLat, Double userLng, Double radiusKm) {
        List<Pharmacy> pharmacies;

        if (userLat != null && userLng != null) {
            // Get pharmacies within radius (you'll need to implement this query)
            pharmacies = pharmacyRepository.findActivePharmaciesWithinRadius(userLat, userLng, radiusKm);
        } else {
            // Get all active pharmacies with location data
            pharmacies = pharmacyRepository.findByIsActiveTrueAndIsVerifiedTrueAndLatitudeIsNotNullAndLongitudeIsNotNull();
        }

        return pharmacies.stream()
                .map(this::mapToPharmacyMapDTO)
                .collect(Collectors.toList());
    }

    private PharmacyMapDTO mapToPharmacyMapDTO(Pharmacy pharmacy) {
        PharmacyMapDTO dto = new PharmacyMapDTO();
        dto.setId(pharmacy.getId());
        dto.setName(pharmacy.getName());
        dto.setAddress(pharmacy.getAddress());
        dto.setLatitude(pharmacy.getLatitude());
        dto.setLongitude(pharmacy.getLongitude());
        dto.setAverageRating(pharmacy.getAverageRating());
        dto.setPhoneNumber(pharmacy.getPhoneNumber());
        dto.setDeliveryAvailable(pharmacy.getDeliveryAvailable());
        dto.setLogoUrl(pharmacy.getLogoUrl());
        dto.setOperatingHours(pharmacy.getOperatingHours());
        dto.setIsActive(pharmacy.getIsActive());
        dto.setIsVerified(pharmacy.getIsVerified());
        return dto;
    }
}