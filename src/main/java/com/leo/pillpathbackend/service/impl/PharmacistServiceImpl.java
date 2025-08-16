package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.PharmacistCreateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistUpdateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistResponseDTO;
import com.leo.pillpathbackend.entity.Pharmacist;
import com.leo.pillpathbackend.entity.Pharmacy;
import com.leo.pillpathbackend.entity.enums.EmploymentStatus;
import com.leo.pillpathbackend.repository.PharmacistRepository;
import com.leo.pillpathbackend.repository.PharmacyRepository;
import com.leo.pillpathbackend.service.PharmacistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PharmacistServiceImpl implements PharmacistService {
    
    private final PharmacistRepository pharmacistRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public PharmacistResponseDTO createPharmacist(PharmacistCreateRequestDTO request) {
        // Check if email already exists
        if (pharmacistRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Check if license number already exists
        if (pharmacistRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new RuntimeException("License number already exists");
        }

        // Get pharmacy
        Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));

        // Create pharmacist
        Pharmacist pharmacist = new Pharmacist();
        pharmacist.setFullName(request.getFullName());
        pharmacist.setEmail(request.getEmail());
        pharmacist.setPassword(passwordEncoder.encode(request.getPassword()));
        pharmacist.setPhoneNumber(request.getPhoneNumber());
        pharmacist.setDateOfBirth(request.getDateOfBirth());
        pharmacist.setProfilePictureUrl(request.getProfilePictureUrl());
        pharmacist.setPharmacy(pharmacy);
        pharmacist.setLicenseNumber(request.getLicenseNumber());
        pharmacist.setLicenseExpiryDate(request.getLicenseExpiryDate());
        pharmacist.setSpecialization(request.getSpecialization());
        pharmacist.setYearsOfExperience(request.getYearsOfExperience());
        pharmacist.setHireDate(request.getHireDate() != null ? request.getHireDate() : LocalDate.now());
        pharmacist.setShiftSchedule(request.getShiftSchedule());
        pharmacist.setCertifications(request.getCertifications());
        pharmacist.setIsActive(true);
        pharmacist.setIsVerified(false);
        pharmacist.setEmploymentStatus(EmploymentStatus.ACTIVE);

        Pharmacist saved = pharmacistRepository.save(pharmacist);
        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public PharmacistResponseDTO updatePharmacist(Long pharmacistId, PharmacistUpdateRequestDTO request) {
        Pharmacist pharmacist = pharmacistRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found"));

        // Check if email is being changed and if it already exists
        if (!pharmacist.getEmail().equals(request.getEmail()) && 
            pharmacistRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Update fields
        pharmacist.setFullName(request.getFullName());
        pharmacist.setEmail(request.getEmail());
        pharmacist.setPhoneNumber(request.getPhoneNumber());
        pharmacist.setDateOfBirth(request.getDateOfBirth());
        pharmacist.setProfilePictureUrl(request.getProfilePictureUrl());
        pharmacist.setLicenseExpiryDate(request.getLicenseExpiryDate());
        pharmacist.setSpecialization(request.getSpecialization());
        pharmacist.setYearsOfExperience(request.getYearsOfExperience());
        pharmacist.setShiftSchedule(request.getShiftSchedule());
        pharmacist.setCertifications(request.getCertifications());

        Pharmacist updated = pharmacistRepository.save(pharmacist);
        return convertToResponse(updated);
    }

    @Override
    public PharmacistResponseDTO getPharmacistById(Long pharmacistId) {
        Pharmacist pharmacist = pharmacistRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found"));
        return convertToResponse(pharmacist);
    }

    @Override
    public List<PharmacistResponseDTO> getPharmacistsByPharmacyId(Long pharmacyId) {
        List<Pharmacist> pharmacists = pharmacistRepository.findByPharmacyIdAndIsActiveTrue(pharmacyId);
        return pharmacists.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PharmacistResponseDTO> getPharmacistsByPharmacy(Long pharmacyId) {
        return getPharmacistsByPharmacyId(pharmacyId);
    }

    @Override
    public List<PharmacistResponseDTO> searchPharmacistsByPharmacyId(Long pharmacyId, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getPharmacistsByPharmacyId(pharmacyId);
        }
        
        List<Pharmacist> pharmacists = pharmacistRepository.findByPharmacyIdAndIsActiveTrue(pharmacyId);
        return pharmacists.stream()
                .filter(pharmacist -> 
                    pharmacist.getFullName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    (pharmacist.getEmail() != null && pharmacist.getEmail().toLowerCase().contains(searchTerm.toLowerCase())) ||
                    (pharmacist.getLicenseNumber() != null && pharmacist.getLicenseNumber().toLowerCase().contains(searchTerm.toLowerCase())) ||
                    (pharmacist.getSpecialization() != null && pharmacist.getSpecialization().toLowerCase().contains(searchTerm.toLowerCase()))
                )
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePharmacist(Long pharmacistId) {
        Pharmacist pharmacist = pharmacistRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found"));
        
        // Soft delete by setting isActive to false
        pharmacist.setIsActive(false);
        pharmacist.setEmploymentStatus(EmploymentStatus.TERMINATED);
        pharmacistRepository.save(pharmacist);
    }

    @Override
    @Transactional
    public PharmacistResponseDTO togglePharmacistStatus(Long pharmacistId, Boolean isActive) {
        Pharmacist pharmacist = pharmacistRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found"));
        
        pharmacist.setIsActive(isActive);
        if (isActive) {
            pharmacist.setEmploymentStatus(EmploymentStatus.ACTIVE);
        } else {
            pharmacist.setEmploymentStatus(EmploymentStatus.INACTIVE);
        }
        
        Pharmacist updated = pharmacistRepository.save(pharmacist);
        return convertToResponse(updated);
    }

    @Override
    public long getPharmacistCountByPharmacyId(Long pharmacyId) {
        return pharmacistRepository.countByPharmacyIdAndIsActiveTrue(pharmacyId);
    }

    @Override
    public boolean isPharmacistInPharmacy(Long pharmacistId, Long pharmacyId) {
        return pharmacistRepository.existsByIdAndPharmacyIdAndIsActiveTrue(pharmacistId, pharmacyId);
    }

    private PharmacistResponseDTO convertToResponse(Pharmacist pharmacist) {
        PharmacistResponseDTO response = new PharmacistResponseDTO();
        response.setId(pharmacist.getId());
        response.setFullName(pharmacist.getFullName());
        response.setEmail(pharmacist.getEmail());
        response.setPhoneNumber(pharmacist.getPhoneNumber());
        response.setDateOfBirth(pharmacist.getDateOfBirth());
        response.setProfilePictureUrl(pharmacist.getProfilePictureUrl());
        response.setPharmacyId(pharmacist.getPharmacy().getId());
        response.setPharmacyName(pharmacist.getPharmacy().getName());
        response.setLicenseNumber(pharmacist.getLicenseNumber());
        response.setLicenseExpiryDate(pharmacist.getLicenseExpiryDate());
        response.setSpecialization(pharmacist.getSpecialization());
        response.setYearsOfExperience(pharmacist.getYearsOfExperience());
        response.setHireDate(pharmacist.getHireDate());
        response.setIsActive(pharmacist.getIsActive());
        response.setIsVerified(pharmacist.getIsVerified());
        response.setEmploymentStatus(pharmacist.getEmploymentStatus());
        response.setShiftSchedule(pharmacist.getShiftSchedule());
        response.setCertifications(pharmacist.getCertifications());
        response.setCreatedAt(pharmacist.getCreatedAt());
        response.setUpdatedAt(pharmacist.getUpdatedAt());
        return response;
    }
}