package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.PharmacistCreateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistUpdateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistResponseDTO;
import com.leo.pillpathbackend.entity.PharmacistUser;
import com.leo.pillpathbackend.entity.Pharmacy;
import com.leo.pillpathbackend.entity.enums.EmploymentStatus;
import com.leo.pillpathbackend.repository.PharmacistUserRepository;
import com.leo.pillpathbackend.repository.PharmacyRepository;
import com.leo.pillpathbackend.repository.UserRepository;
import com.leo.pillpathbackend.service.CloudinaryService;
import com.leo.pillpathbackend.service.PharmacistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacistServiceImpl implements PharmacistService {

    private final PharmacistUserRepository pharmacistUserRepository;
    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository; // for email existence
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @Override
    public PharmacistResponseDTO createPharmacist(PharmacistCreateRequestDTO request) {
        log.info("Creating new pharmacist (user) with email: {}", request.getEmail());

        // Basic validation
        if (request.getEmail() == null || request.getEmail().isBlank()) throw new RuntimeException("Email is required");
        if (request.getFullName() == null || request.getFullName().isBlank()) throw new RuntimeException("Full name is required");
        if (request.getLicenseNumber() == null || request.getLicenseNumber().isBlank()) throw new RuntimeException("License number is required");
        if (request.getPassword() == null || request.getPassword().isBlank()) throw new RuntimeException("Password is required");
        if (request.getPharmacyId() == null) throw new RuntimeException("Pharmacy ID is required");

        if (userRepository.existsByEmail(request.getEmail().trim())) throw new RuntimeException("Email already exists: " + request.getEmail());
        if (pharmacistUserRepository.existsByLicenseNumber(request.getLicenseNumber().trim())) throw new RuntimeException("License number already exists: " + request.getLicenseNumber());

        Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                .orElseThrow(() -> new RuntimeException("Pharmacy not found with ID: " + request.getPharmacyId()));

        PharmacistUser user = new PharmacistUser();
        user.setEmail(request.getEmail().trim());
        user.setUsername(request.getEmail().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);
        user.setDateOfBirth(request.getDateOfBirth());
        user.setProfilePictureUrl(request.getProfilePictureUrl());
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        user.setIsVerified(request.getIsVerified() != null ? request.getIsVerified() : false);
        user.setPharmacy(pharmacy);
        user.setLicenseNumber(request.getLicenseNumber().trim());
        user.setLicenseExpiryDate(request.getLicenseExpiryDate());
        user.setSpecialization(request.getSpecialization());
        user.setYearsOfExperience(request.getYearsOfExperience());
        user.setHireDate(request.getHireDate() != null ? request.getHireDate() : LocalDate.now());
        user.setShiftSchedule(request.getShiftSchedule());
        user.setCertifications(request.getCertifications());
        user.setEmploymentStatus(request.getEmploymentStatus() != null ? request.getEmploymentStatus() : EmploymentStatus.ACTIVE);

        PharmacistUser saved = pharmacistUserRepository.save(user);
        return PharmacistResponseDTO.fromEntity(saved);
    }

    @Override
    public PharmacistResponseDTO updatePharmacist(Long pharmacistId, PharmacistUpdateRequestDTO request) {
        PharmacistUser user = pharmacistUserRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));

        if (request.getFullName() != null && !request.getFullName().isBlank()) user.setFullName(request.getFullName().trim());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber().trim());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getProfilePictureUrl() != null) user.setProfilePictureUrl(request.getProfilePictureUrl());
        if (request.getLicenseExpiryDate() != null) user.setLicenseExpiryDate(request.getLicenseExpiryDate());
        if (request.getSpecialization() != null) user.setSpecialization(request.getSpecialization());
        if (request.getYearsOfExperience() != null) user.setYearsOfExperience(request.getYearsOfExperience());
        if (request.getShiftSchedule() != null) user.setShiftSchedule(request.getShiftSchedule());
        if (request.getCertifications() != null) user.setCertifications(request.getCertifications());

        PharmacistUser saved = pharmacistUserRepository.save(user);
        return PharmacistResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacistResponseDTO getPharmacistById(Long pharmacistId) {
        return pharmacistUserRepository.findById(pharmacistId)
                .map(PharmacistResponseDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacistResponseDTO> getPharmacistsByPharmacyId(Long pharmacyId) {
        return pharmacistUserRepository.findByPharmacyIdAndIsActiveTrue(pharmacyId)
                .stream().map(PharmacistResponseDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacistResponseDTO> getPharmacistsByPharmacy(Long pharmacyId) {
        return getPharmacistsByPharmacyId(pharmacyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacistResponseDTO> searchPharmacistsByPharmacyId(Long pharmacyId, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) return getPharmacistsByPharmacyId(pharmacyId);
        return pharmacistUserRepository.search(pharmacyId, searchTerm.trim())
                .stream().map(PharmacistResponseDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public void deletePharmacist(Long pharmacistId) {
        PharmacistUser user = pharmacistUserRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));
        // soft delete
        user.setIsActive(false);
        user.setEmploymentStatus(EmploymentStatus.TERMINATED);
        pharmacistUserRepository.save(user);
    }

    @Override
    public PharmacistResponseDTO togglePharmacistStatus(Long pharmacistId, Boolean isActive) {
        PharmacistUser user = pharmacistUserRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));
        user.setIsActive(isActive);
        user.setEmploymentStatus(Boolean.TRUE.equals(isActive) ? EmploymentStatus.ACTIVE : EmploymentStatus.INACTIVE);
        PharmacistUser saved = pharmacistUserRepository.save(user);
        return PharmacistResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public long getPharmacistCountByPharmacyId(Long pharmacyId) {
        return pharmacistUserRepository.countByPharmacyIdAndIsActiveTrue(pharmacyId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPharmacistInPharmacy(Long pharmacistId, Long pharmacyId) {
        return pharmacistUserRepository.existsByIdAndPharmacyIdAndIsActiveTrue(pharmacistId, pharmacyId);
    }

    @Override
    public PharmacistResponseDTO updateProfilePicture(Long pharmacistId, String imageUrl) {
        PharmacistUser user = pharmacistUserRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));
        user.setProfilePictureUrl(imageUrl);
        PharmacistUser saved = pharmacistUserRepository.save(user);
        return PharmacistResponseDTO.fromEntity(saved);
    }

    @Override
    public PharmacistResponseDTO removeProfilePicture(Long pharmacistId) {
        PharmacistUser user = pharmacistUserRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));
        user.setProfilePictureUrl(null);
        PharmacistUser saved = pharmacistUserRepository.save(user);
        return PharmacistResponseDTO.fromEntity(saved);
    }
}