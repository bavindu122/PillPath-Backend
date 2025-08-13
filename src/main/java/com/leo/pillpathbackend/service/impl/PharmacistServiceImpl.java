package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.PharmacistCreateRequest;
import com.leo.pillpathbackend.dto.PharmacistUpdateRequest;
import com.leo.pillpathbackend.dto.PharmacistResponseDTO;
import com.leo.pillpathbackend.entity.Pharmacist;
import com.leo.pillpathbackend.entity.Pharmacy;
import com.leo.pillpathbackend.entity.enums.EmploymentStatus;
import com.leo.pillpathbackend.repository.PharmacistRepository;
import com.leo.pillpathbackend.repository.PharmacyRepository;
import com.leo.pillpathbackend.service.PharmacistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacistServiceImpl implements PharmacistService {

    private final PharmacistRepository pharmacistRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PharmacistResponseDTO createPharmacist(PharmacistCreateRequest request) {
        log.info("Creating new pharmacist with email: {}", request.getEmail());
        
        // Validate input
        validateCreateRequest(request);
        
        // Check if email already exists
        if (pharmacistRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        
        // Check if license number already exists
        if (pharmacistRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new RuntimeException("License number already exists: " + request.getLicenseNumber());
        }
        
        // Get pharmacy
        Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                .orElseThrow(() -> new RuntimeException("Pharmacy not found with ID: " + request.getPharmacyId()));
        
        // Create pharmacist entity
        Pharmacist pharmacist = new Pharmacist();
        pharmacist.setEmail(request.getEmail());
        pharmacist.setPassword(passwordEncoder.encode(request.getPassword()));
        pharmacist.setFullName(request.getFullName());
        pharmacist.setPhoneNumber(request.getPhoneNumber());
        pharmacist.setProfilePictureUrl(request.getProfilePictureUrl());
        pharmacist.setLicenseNumber(request.getLicenseNumber());
        pharmacist.setSpecialization(request.getSpecialization());
        pharmacist.setYearsOfExperience(request.getYearsOfExperience());
        pharmacist.setShiftSchedule(request.getShiftSchedule());
        pharmacist.setPharmacy(pharmacy);
        pharmacist.setHireDate(LocalDate.now());
        pharmacist.setIsActive(true);
        pharmacist.setIsVerified(false);
        pharmacist.setEmploymentStatus(EmploymentStatus.ACTIVE);
        pharmacist.setCreatedAt(LocalDateTime.now());
        pharmacist.setUpdatedAt(LocalDateTime.now());
        
        Pharmacist savedPharmacist = pharmacistRepository.save(pharmacist);
        log.info("Successfully created pharmacist with ID: {}", savedPharmacist.getId());
        
        return convertToResponseDTO(savedPharmacist);
    }

    @Override
    public PharmacistResponseDTO updatePharmacist(Long pharmacistId, PharmacistUpdateRequest request) {
        log.info("Updating pharmacist with ID: {}", pharmacistId);
        
        Pharmacist pharmacist = pharmacistRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));
        
        // Check if email is being changed and if it already exists
        if (!pharmacist.getEmail().equals(request.getEmail()) && 
            pharmacistRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        
        // Update fields
        pharmacist.setEmail(request.getEmail());
        pharmacist.setFullName(request.getFullName());
        pharmacist.setPhoneNumber(request.getPhoneNumber());
        pharmacist.setSpecialization(request.getSpecialization());
        pharmacist.setYearsOfExperience(request.getYearsOfExperience());
        pharmacist.setShiftSchedule(request.getShiftSchedule());
        pharmacist.setProfilePictureUrl(request.getProfilePictureUrl());
        
        if (request.getIsActive() != null) {
            pharmacist.setIsActive(request.getIsActive());
            pharmacist.setEmploymentStatus(request.getIsActive() ? 
                EmploymentStatus.ACTIVE : EmploymentStatus.INACTIVE);
        }
        
        pharmacist.setUpdatedAt(LocalDateTime.now());
        
        Pharmacist updatedPharmacist = pharmacistRepository.save(pharmacist);
        log.info("Successfully updated pharmacist with ID: {}", pharmacistId);
        
        return convertToResponseDTO(updatedPharmacist);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacistResponseDTO getPharmacistById(Long pharmacistId) {
        Pharmacist pharmacist = pharmacistRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));
        
        return convertToResponseDTO(pharmacist);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacistResponseDTO> getPharmacistsByPharmacyId(Long pharmacyId) {
        log.info("Fetching pharmacists for pharmacy ID: {}", pharmacyId);
        
        List<Pharmacist> pharmacists = pharmacistRepository.findByPharmacyId(pharmacyId);
        
        return pharmacists.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacistResponseDTO> searchPharmacistsByPharmacyId(Long pharmacyId, String searchTerm) {
        log.info("Searching pharmacists for pharmacy ID: {} with term: {}", pharmacyId, searchTerm);
        
        List<Pharmacist> pharmacists;
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            pharmacists = pharmacistRepository.findByPharmacyId(pharmacyId);
        } else {
            pharmacists = pharmacistRepository.findByPharmacyIdAndSearchTerm(pharmacyId, searchTerm.trim());
        }
        
        return pharmacists.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePharmacist(Long pharmacistId) {
        log.info("Deleting pharmacist with ID: {}", pharmacistId);
        
        Pharmacist pharmacist = pharmacistRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));
        
        // Soft delete by setting isActive to false
        pharmacist.setIsActive(false);
        pharmacist.setEmploymentStatus(EmploymentStatus.TERMINATED);
        pharmacist.setUpdatedAt(LocalDateTime.now());
        
        pharmacistRepository.save(pharmacist);
        log.info("Successfully deleted (deactivated) pharmacist with ID: {}", pharmacistId);
    }

    @Override
    public PharmacistResponseDTO togglePharmacistStatus(Long pharmacistId, Boolean isActive) {
        log.info("Toggling status for pharmacist ID: {} to {}", pharmacistId, isActive);
        
        Pharmacist pharmacist = pharmacistRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));
        
        pharmacist.setIsActive(isActive);
        pharmacist.setEmploymentStatus(isActive ? EmploymentStatus.ACTIVE : EmploymentStatus.INACTIVE);
        pharmacist.setUpdatedAt(LocalDateTime.now());
        
        Pharmacist updatedPharmacist = pharmacistRepository.save(pharmacist);
        
        return convertToResponseDTO(updatedPharmacist);
    }

    @Override
    @Transactional(readOnly = true)
    public long getPharmacistCountByPharmacyId(Long pharmacyId) {
        return pharmacistRepository.countByPharmacyId(pharmacyId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPharmacistInPharmacy(Long pharmacistId, Long pharmacyId) {
        return pharmacistRepository.findById(pharmacistId)
                .map(pharmacist -> pharmacist.getPharmacy().getId().equals(pharmacyId))
                .orElse(false);
    }

    private void validateCreateRequest(PharmacistCreateRequest request) {
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (request.getLicenseNumber() == null || request.getLicenseNumber().trim().isEmpty()) {
            throw new RuntimeException("License number is required");
        }
        if (request.getPharmacyId() == null) {
            throw new RuntimeException("Pharmacy ID is required");
        }
    }

    private PharmacistResponseDTO convertToResponseDTO(Pharmacist pharmacist) {
        PharmacistResponseDTO dto = new PharmacistResponseDTO();
        dto.setId(pharmacist.getId());
        dto.setEmail(pharmacist.getEmail());
        dto.setFullName(pharmacist.getFullName());
        dto.setPhoneNumber(pharmacist.getPhoneNumber());
        dto.setDateOfBirth(pharmacist.getDateOfBirth());
        dto.setProfilePictureUrl(pharmacist.getProfilePictureUrl());
        dto.setPharmacyId(pharmacist.getPharmacy().getId());
        dto.setPharmacyName(pharmacist.getPharmacy().getName());
        dto.setLicenseNumber(pharmacist.getLicenseNumber());
        dto.setLicenseExpiryDate(pharmacist.getLicenseExpiryDate());
        dto.setSpecialization(pharmacist.getSpecialization());
        dto.setYearsOfExperience(pharmacist.getYearsOfExperience());
        dto.setHireDate(pharmacist.getHireDate());
        dto.setShiftSchedule(pharmacist.getShiftSchedule());
        dto.setCertifications(pharmacist.getCertifications());
        dto.setIsVerified(pharmacist.getIsVerified());
        dto.setIsActive(pharmacist.getIsActive());
        dto.setEmploymentStatus(pharmacist.getEmploymentStatus());
        dto.setCreatedAt(pharmacist.getCreatedAt());
        dto.setUpdatedAt(pharmacist.getUpdatedAt());
        
        return dto;
    }
}
