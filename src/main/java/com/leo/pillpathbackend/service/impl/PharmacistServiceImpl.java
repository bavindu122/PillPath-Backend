package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.PharmacistCreateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistUpdateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistResponseDTO;
import com.leo.pillpathbackend.entity.Pharmacist;
import com.leo.pillpathbackend.entity.PharmacistUser;
import com.leo.pillpathbackend.entity.Pharmacy;
import com.leo.pillpathbackend.entity.enums.EmploymentStatus;
import com.leo.pillpathbackend.repository.PharmacistRepository;
import com.leo.pillpathbackend.repository.PharmacyRepository;
import com.leo.pillpathbackend.repository.UserRepository;
import com.leo.pillpathbackend.service.PharmacistService;
import com.leo.pillpathbackend.service.CloudinaryService;
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
    
    private final PharmacistRepository pharmacistRepository;
    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public PharmacistResponseDTO createPharmacist(PharmacistCreateRequestDTO request) {
        log.info("Creating new pharmacist with email: {}", request.getEmail());
        log.info("Full request: {}", request);
        
        // Validate required fields
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        if (request.getLicenseNumber() == null || request.getLicenseNumber().trim().isEmpty()) {
            throw new RuntimeException("License number is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (request.getPharmacyId() == null) {
            throw new RuntimeException("Pharmacy ID is required");
        }
        
        // Validate email uniqueness in User table
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Validate license number uniqueness
        if (pharmacistRepository.existsByLicenseNumber(request.getLicenseNumber().trim())) {
            throw new RuntimeException("License number already exists: " + request.getLicenseNumber());
        }

        // Get pharmacy
        Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                .orElseThrow(() -> new RuntimeException("Pharmacy not found with ID: " + request.getPharmacyId()));

        // Create PharmacistUser first (for login capability)
        PharmacistUser user = new PharmacistUser();
        user.setEmail(request.getEmail().trim());
        user.setUsername(request.getEmail().trim()); // Use email as username
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);
        user.setDateOfBirth(request.getDateOfBirth());
        user.setProfilePictureUrl(request.getProfilePictureUrl());
        user.setIsActive(true);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        // Save user first
        PharmacistUser savedUser = (PharmacistUser) userRepository.save(user);
        log.info("Created user with ID: {}", savedUser.getId());

        // Create Pharmacist entity
        Pharmacist pharmacist = new Pharmacist();
        
        // Set BOTH the direct fields (required by entity) AND the relationship
        // Direct fields (to satisfy nullable = false constraints)
        pharmacist.setEmail(request.getEmail().trim());
        pharmacist.setPassword(passwordEncoder.encode(request.getPassword()));
        pharmacist.setFullName(request.getFullName().trim());
        pharmacist.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);
        pharmacist.setDateOfBirth(request.getDateOfBirth());
        pharmacist.setProfilePictureUrl(request.getProfilePictureUrl());
        
        // Set the PharmacistUser relationship
        pharmacist.setPharmacistUser(savedUser);
        
        // Set pharmacy relationship
        pharmacist.setPharmacy(pharmacy);
        
        // Set pharmacist-specific fields
        pharmacist.setLicenseNumber(request.getLicenseNumber().trim());
        pharmacist.setLicenseExpiryDate(request.getLicenseExpiryDate());
        pharmacist.setSpecialization(request.getSpecialization() != null ? request.getSpecialization().trim() : null);
        pharmacist.setYearsOfExperience(request.getYearsOfExperience());
        pharmacist.setHireDate(request.getHireDate() != null ? request.getHireDate() : LocalDate.now());
        pharmacist.setShiftSchedule(request.getShiftSchedule());
        pharmacist.setCertifications(request.getCertifications());
        pharmacist.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        pharmacist.setIsVerified(request.getIsVerified() != null ? request.getIsVerified() : false);
        pharmacist.setEmploymentStatus(request.getEmploymentStatus() != null ? request.getEmploymentStatus() : EmploymentStatus.ACTIVE);

        Pharmacist savedPharmacist = pharmacistRepository.save(pharmacist);
        log.info("Created pharmacist with ID: {}", savedPharmacist.getId());

        return convertToResponse(savedPharmacist);
    }

    @Override
    @Transactional
    public PharmacistResponseDTO updatePharmacist(Long pharmacistId, PharmacistUpdateRequestDTO request) {
        log.info("Updating pharmacist with ID: {}", pharmacistId);
        
        Pharmacist pharmacist = pharmacistRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));

        // Update BOTH direct fields and PharmacistUser fields
        PharmacistUser user = pharmacist.getPharmacistUser();
        
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            // Update both direct field and user field
            pharmacist.setFullName(request.getFullName().trim());
            if (user != null) {
                user.setFullName(request.getFullName().trim());
            }
        }
        
        if (request.getPhoneNumber() != null) {
            // Update both direct field and user field
            pharmacist.setPhoneNumber(request.getPhoneNumber().trim());
            if (user != null) {
                user.setPhoneNumber(request.getPhoneNumber().trim());
            }
        }
        
        if (request.getDateOfBirth() != null) {
            // Update both direct field and user field
            pharmacist.setDateOfBirth(request.getDateOfBirth());
            if (user != null) {
                user.setDateOfBirth(request.getDateOfBirth());
            }
        }
        
        if (request.getProfilePictureUrl() != null) {
            // Update both direct field and user field
            pharmacist.setProfilePictureUrl(request.getProfilePictureUrl());
            if (user != null) {
                user.setProfilePictureUrl(request.getProfilePictureUrl());
            }
        }
        
        // Update pharmacist-specific data
        if (request.getLicenseExpiryDate() != null) {
            pharmacist.setLicenseExpiryDate(request.getLicenseExpiryDate());
        }
        if (request.getSpecialization() != null) {
            pharmacist.setSpecialization(request.getSpecialization().trim());
        }
        if (request.getYearsOfExperience() != null) {
            pharmacist.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getShiftSchedule() != null) {
            pharmacist.setShiftSchedule(request.getShiftSchedule());
        }
        if (request.getCertifications() != null) {
            pharmacist.setCertifications(request.getCertifications());
        }

        // Save the updated user first if it exists
        if (user != null) {
            userRepository.save(user);
        }

        Pharmacist savedPharmacist = pharmacistRepository.save(pharmacist);
        log.info("Updated pharmacist with ID: {}", savedPharmacist.getId());

        return convertToResponse(savedPharmacist);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacistResponseDTO getPharmacistById(Long pharmacistId) {
        log.info("Fetching pharmacist with ID: {}", pharmacistId);
        
        Pharmacist pharmacist = pharmacistRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));
        
        return convertToResponse(pharmacist);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacistResponseDTO> getPharmacistsByPharmacyId(Long pharmacyId) {
        log.info("Fetching pharmacists for pharmacy ID: {}", pharmacyId);
        
        List<Pharmacist> pharmacists = pharmacistRepository.findByPharmacyIdAndIsActiveTrue(pharmacyId);
        return pharmacists.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacistResponseDTO> getPharmacistsByPharmacy(Long pharmacyId) {
        return getPharmacistsByPharmacyId(pharmacyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacistResponseDTO> searchPharmacistsByPharmacyId(Long pharmacyId, String searchTerm) {
        log.info("Searching pharmacists for pharmacy ID: {} with term: {}", pharmacyId, searchTerm);
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getPharmacistsByPharmacyId(pharmacyId);
        }
        
        List<Pharmacist> pharmacists = pharmacistRepository.searchByPharmacyIdAndTerm(pharmacyId, searchTerm.trim());
        return pharmacists.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePharmacist(Long pharmacistId) {
        log.info("Deleting pharmacist with ID: {}", pharmacistId);
        
        Pharmacist pharmacist = pharmacistRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));
        
        // Soft delete pharmacist
        pharmacist.setIsActive(false);
        pharmacist.setEmploymentStatus(EmploymentStatus.TERMINATED);
        
        // Also deactivate the associated user
        PharmacistUser user = pharmacist.getPharmacistUser();
        if (user != null) {
            user.setIsActive(false);
            userRepository.save(user);
        }
        
        pharmacistRepository.save(pharmacist);
        log.info("Soft deleted pharmacist with ID: {}", pharmacistId);
    }

    @Override
    @Transactional
    public PharmacistResponseDTO togglePharmacistStatus(Long pharmacistId, Boolean isActive) {
        log.info("Toggling status for pharmacist ID: {} to: {}", pharmacistId, isActive);
        
        Pharmacist pharmacist = pharmacistRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));
        
        // Update pharmacist status
        pharmacist.setIsActive(isActive);
        if (isActive) {
            pharmacist.setEmploymentStatus(EmploymentStatus.ACTIVE);
        } else {
            pharmacist.setEmploymentStatus(EmploymentStatus.INACTIVE);
        }
        
        // Update associated user status
        PharmacistUser user = pharmacist.getPharmacistUser();
        if (user != null) {
            user.setIsActive(isActive);
            userRepository.save(user);
        }
        
        Pharmacist savedPharmacist = pharmacistRepository.save(pharmacist);
        log.info("Updated pharmacist status with ID: {}", savedPharmacist.getId());
        
        return convertToResponse(savedPharmacist);
    }

    @Override
    @Transactional(readOnly = true)
    public long getPharmacistCountByPharmacyId(Long pharmacyId) {
        log.info("Getting pharmacist count for pharmacy ID: {}", pharmacyId);
        return pharmacistRepository.countByPharmacyIdAndIsActiveTrue(pharmacyId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPharmacistInPharmacy(Long pharmacistId, Long pharmacyId) {
        log.info("Verifying if pharmacist {} belongs to pharmacy {}", pharmacistId, pharmacyId);
        return pharmacistRepository.existsByIdAndPharmacyIdAndIsActiveTrue(pharmacistId, pharmacyId);
    }

    // Helper method to convert Pharmacist entity to PharmacistResponseDTO
    private PharmacistResponseDTO convertToResponse(Pharmacist pharmacist) {
        PharmacistResponseDTO response = new PharmacistResponseDTO();
        response.setId(pharmacist.getId());
        
        // Use direct fields for now (since they're required by the entity)
        response.setFullName(pharmacist.getFullName());
        response.setEmail(pharmacist.getEmail());
        response.setPhoneNumber(pharmacist.getPhoneNumber());
        response.setDateOfBirth(pharmacist.getDateOfBirth());
        response.setProfilePictureUrl(pharmacist.getProfilePictureUrl());
        
        // Set pharmacy data
        if (pharmacist.getPharmacy() != null) {
            response.setPharmacyId(pharmacist.getPharmacy().getId());
            response.setPharmacyName(pharmacist.getPharmacy().getName());
        }
        
        // Set pharmacist-specific data
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

    @Override
    @Transactional
    public PharmacistResponseDTO updateProfilePicture(Long pharmacistId, String imageUrl) {
        log.info("Updating profile picture for pharmacist ID: {}", pharmacistId);

        Pharmacist pharmacist = pharmacistRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));

        // Delete old profile picture from Cloudinary if exists
        String oldProfilePictureUrl = pharmacist.getProfilePictureUrl();
        if (oldProfilePictureUrl != null && !oldProfilePictureUrl.isEmpty()) {
            try {
                String publicId = cloudinaryService.extractPublicIdFromUrl(oldProfilePictureUrl);
                if (publicId != null) {
                    cloudinaryService.deleteImage(publicId);
                }
            } catch (Exception e) {
                log.warn("Failed to delete old profile picture: {}", e.getMessage());
                // Don't fail the operation if old image deletion fails
            }
        }

        // Update both direct field and user field
        pharmacist.setProfilePictureUrl(imageUrl);

        PharmacistUser user = pharmacist.getPharmacistUser();
        if (user != null) {
            user.setProfilePictureUrl(imageUrl);
            userRepository.save(user);
        }

        Pharmacist savedPharmacist = pharmacistRepository.save(pharmacist);
        log.info("Updated profile picture for pharmacist ID: {}", pharmacistId);

        return convertToResponse(savedPharmacist);
    }

    @Override
    @Transactional
    public PharmacistResponseDTO removeProfilePicture(Long pharmacistId) {
        log.info("Removing profile picture for pharmacist ID: {}", pharmacistId);

        Pharmacist pharmacist = pharmacistRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with ID: " + pharmacistId));

        // Delete profile picture from Cloudinary if exists
        String profilePictureUrl = pharmacist.getProfilePictureUrl();
        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            try {
                String publicId = cloudinaryService.extractPublicIdFromUrl(profilePictureUrl);
                if (publicId != null) {
                    cloudinaryService.deleteImage(publicId);
                }
            } catch (Exception e) {
                log.warn("Failed to delete profile picture from Cloudinary: {}", e.getMessage());
                // Don't fail the operation if image deletion fails
            }
        }

        // Remove profile picture URLs
        pharmacist.setProfilePictureUrl(null);

        PharmacistUser user = pharmacist.getPharmacistUser();
        if (user != null) {
            user.setProfilePictureUrl(null);
            userRepository.save(user);
        }

        Pharmacist savedPharmacist = pharmacistRepository.save(pharmacist);
        log.info("Removed profile picture for pharmacist ID: {}", pharmacistId);

        return convertToResponse(savedPharmacist);
    }
}