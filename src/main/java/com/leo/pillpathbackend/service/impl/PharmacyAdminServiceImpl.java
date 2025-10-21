package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.PharmacyAdminProfileDTO;
import com.leo.pillpathbackend.dto.PharmacyAdminUpdateRequestDTO;
import com.leo.pillpathbackend.entity.PharmacyAdmin;
import com.leo.pillpathbackend.util.Mapper;
import com.leo.pillpathbackend.repository.PharmacyAdminRepository;
import com.leo.pillpathbackend.service.PharmacyAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacyAdminServiceImpl implements PharmacyAdminService {

    private final PharmacyAdminRepository pharmacyAdminRepository;
    private final Mapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PharmacyAdminProfileDTO getPharmacyAdminById(Long adminId) {
        PharmacyAdmin admin = pharmacyAdminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Pharmacy Admin not found with ID: " + adminId));
        return mapper.convertToPharmacyAdminProfileDTO(admin);
    }

    @Override
    public PharmacyAdminProfileDTO updatePharmacyAdmin(Long adminId, PharmacyAdminUpdateRequestDTO request) {
        PharmacyAdmin admin = pharmacyAdminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Pharmacy Admin not found with ID: " + adminId));

        // Update basic user fields
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            admin.setFullName(request.getFullName().trim());
        }
        if (request.getPhoneNumber() != null) {
            admin.setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getDateOfBirth() != null) {
            admin.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAddress() != null) {
            admin.setAddress(request.getAddress().trim());
        }
        if (request.getProfilePictureUrl() != null) {
            admin.setProfilePictureUrl(request.getProfilePictureUrl());
        }

        // Update pharmacy admin specific fields
        if (request.getPosition() != null) {
            admin.setPosition(request.getPosition().trim());
        }
        if (request.getPermissions() != null) {
            admin.setPermissions(request.getPermissions());
        }

        PharmacyAdmin savedAdmin = pharmacyAdminRepository.save(admin);
        return mapper.convertToPharmacyAdminProfileDTO(savedAdmin);
    }

    @Override
    public PharmacyAdminProfileDTO updateProfilePicture(Long adminId, String imageUrl) {
        PharmacyAdmin admin = pharmacyAdminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Pharmacy Admin not found with ID: " + adminId));
        admin.setProfilePictureUrl(imageUrl);
        PharmacyAdmin savedAdmin = pharmacyAdminRepository.save(admin);
        return mapper.convertToPharmacyAdminProfileDTO(savedAdmin);
    }

    @Override
    public PharmacyAdminProfileDTO removeProfilePicture(Long adminId) {
        PharmacyAdmin admin = pharmacyAdminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Pharmacy Admin not found with ID: " + adminId));
        admin.setProfilePictureUrl(null);
        PharmacyAdmin savedAdmin = pharmacyAdminRepository.save(admin);
        return mapper.convertToPharmacyAdminProfileDTO(savedAdmin);
    }
}
