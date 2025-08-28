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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Pattern;


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

    @Override
    public PharmacyDTO getPharmacyProfileByAdminId(Long adminId) {
        PharmacyAdmin admin = pharmacyAdminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Pharmacy admin not found"));

        Pharmacy pharmacy = admin.getPharmacy();
        if (pharmacy == null) {
            throw new RuntimeException("No pharmacy associated with this admin");
        }

        return mapper.convertToPharmacyDTO(pharmacy);
    }

    @Override
    @Transactional
    public PharmacyDTO updatePharmacyProfile(Long adminId, PharmacyDTO pharmacyDTO) {
        PharmacyAdmin admin = pharmacyAdminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Pharmacy admin not found"));

        Pharmacy pharmacy = admin.getPharmacy();
        if (pharmacy == null) {
            throw new RuntimeException("No pharmacy associated with this admin");
        }

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
    public PharmacyDTO getPharmacyProfile(Long pharmacyId) {
        // Try to find pharmacy with the new method, fallback to regular findById
        Pharmacy pharmacy;
        try {
            pharmacy = pharmacyRepository.findPharmacyForProfile(pharmacyId)
                    .orElse(null);
        } catch (Exception e) {
            // If the custom method doesn't exist, use regular findById
            System.out.println("Using fallback method to find pharmacy");
            pharmacy = pharmacyRepository.findById(pharmacyId)
                    .orElse(null);
        }

        if (pharmacy == null) {
            throw new RuntimeException("Pharmacy not found with id: " + pharmacyId);
        }

        PharmacyDTO profileDTO = new PharmacyDTO();

        // Map basic pharmacy data
        profileDTO.setId(pharmacy.getId());
        profileDTO.setName(pharmacy.getName());
        profileDTO.setAddress(pharmacy.getAddress());
        profileDTO.setLatitude(pharmacy.getLatitude());
        profileDTO.setLongitude(pharmacy.getLongitude());
        profileDTO.setPhoneNumber(pharmacy.getPhoneNumber());
        profileDTO.setEmail(pharmacy.getEmail());
        profileDTO.setLicenseNumber(pharmacy.getLicenseNumber());
        profileDTO.setLicenseExpiryDate(pharmacy.getLicenseExpiryDate());
        profileDTO.setLogoUrl(pharmacy.getLogoUrl());
        profileDTO.setLogoPublicId(pharmacy.getLogoPublicId());
        profileDTO.setBannerUrl(pharmacy.getBannerUrl());
        profileDTO.setBannerPublicId(pharmacy.getBannerPublicId());
        profileDTO.setOperatingHours(pharmacy.getOperatingHours());
        profileDTO.setServices(pharmacy.getServices());
        profileDTO.setIsVerified(pharmacy.getIsVerified());
        profileDTO.setIsActive(pharmacy.getIsActive());
        profileDTO.setDeliveryAvailable(pharmacy.getDeliveryAvailable());
        profileDTO.setDeliveryRadius(pharmacy.getDeliveryRadius());
        profileDTO.setAverageRating(pharmacy.getAverageRating());
        profileDTO.setTotalReviews(pharmacy.getTotalReviews());
        profileDTO.setCreatedAt(pharmacy.getCreatedAt());
        profileDTO.setUpdatedAt(pharmacy.getUpdatedAt());

        // Set computed fields
        profileDTO.setStatus(getPharmacyStatus(pharmacy));
        profileDTO.setHasDelivery(pharmacy.getDeliveryAvailable());
        profileDTO.setHas24HourService(checkIf24HourService(pharmacy.getOperatingHours()));
        profileDTO.setAcceptsInsurance(true); // Default or from settings
        profileDTO.setHasVaccinations(pharmacy.getServices() != null &&
                pharmacy.getServices().contains("Vaccination Services"));
        profileDTO.setCurrentStatus(getCurrentOperatingStatus(pharmacy.getOperatingHours()));

        // Add recent reviews and popular products
        profileDTO.setRecentReviews(getRecentReviews(pharmacyId));
        profileDTO.setPopularProducts(getPopularProducts(pharmacyId));

        return profileDTO;
    }

    @Override
    public List<OTCProductDTO> getPharmacyProducts(Long pharmacyId) {
        // For now, return mock data - implement with actual product repository later
        return getMockOTCProducts();
    }

    private String getPharmacyStatus(Pharmacy pharmacy) {
        if (!pharmacy.getIsActive() && !pharmacy.getIsVerified()) {
            return "Rejected";
        } else if (!pharmacy.getIsActive() && pharmacy.getIsVerified()) {
            return "Suspended";
        } else if (pharmacy.getIsActive() && !pharmacy.getIsVerified()) {
            return "Pending";
        } else {
            return "Active";
        }
    }

    private Boolean checkIf24HourService(Map<String, String> operatingHours) {
        if (operatingHours == null) return false;

        return operatingHours.values().stream()
                .anyMatch(hours -> hours != null &&
                        (hours.contains("24") || hours.contains("00:00-23:59")));
    }
    
    // ...existing code...

    private String getCurrentOperatingStatus(Map<String, String> operatingHours) {
        if (operatingHours == null) return "Hours not available";

        String[] days = {"sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};
        int today = java.time.LocalDate.now().getDayOfWeek().getValue() % 7;
        String todayKey = days[today];

        String todayHours = operatingHours.get(todayKey);
        if (todayHours == null || todayHours.trim().isEmpty()) {
            return "Closed today";
        }

        // Simple check - could be enhanced with actual time parsing
        if (todayHours.toLowerCase().contains("closed")) {
            return "Closed today";
        }

        // Extract closing time from hours string
        String closingTime = extractClosingTime(todayHours);
        if (closingTime != null) {
            return "Open today until " + closingTime;
        }

        // Fallback to showing full hours if we can't extract closing time
        return "Open today: " + todayHours;
    }

    private String extractClosingTime(String hoursString) {
        if (hoursString == null || hoursString.trim().isEmpty()) {
            return null;
        }

        try {
            // Handle different formats:
            // "8:00 AM - 9:00 PM"
            // "08:00-21:00" 
            // "8:00AM-9:00PM"
            // "8 AM - 9 PM"
            // "8:00-21:00"
            
            String[] parts;
            
            // Check for dash separator with spaces
            if (hoursString.contains(" - ")) {
                parts = hoursString.split(" - ");
            } 
            // Check for dash separator without spaces
            else if (hoursString.contains("-")) {
                parts = hoursString.split("-");
            } 
            // Check for "to" separator
            else if (hoursString.toLowerCase().contains(" to ")) {
                parts = hoursString.split("(?i) to ");
            }
            else {
                return null; // Can't parse format
            }
            
            if (parts.length >= 2) {
                String closingTime = parts[1].trim();
                
                // If it's 24-hour format (like "21:00"), convert to 12-hour
                if (closingTime.matches("\\d{1,2}:\\d{2}")) {
                    return convertTo12Hour(closingTime);
                }
                
                // If it already has AM/PM, return as is
                if (closingTime.toUpperCase().contains("AM") || closingTime.toUpperCase().contains("PM")) {
                    return closingTime;
                }
                
                // If it's just numbers like "21", assume it's hour in 24-hour format
                if (closingTime.matches("\\d{1,2}")) {
                    return convertTo12Hour(closingTime + ":00");
                }
                
                return closingTime;
            }
        } catch (Exception e) {
            System.err.println("Error parsing hours string: " + hoursString + ", error: " + e.getMessage());
            return null;
        }
        
        return null;
    }
    
    private String convertTo12Hour(String time24) {
        try {
            String[] parts = time24.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            
            String amPm = "AM";
            if (hour >= 12) {
                amPm = "PM";
                if (hour > 12) {
                    hour -= 12;
                }
            }
            if (hour == 0) {
                hour = 12;
            }
            
            // Format like "8:00 PM" or "10:30 AM"
            if (minute == 0) {
                return String.format("%d:00 %s", hour, amPm);
            } else {
                return String.format("%d:%02d %s", hour, minute, amPm);
            }
        } catch (Exception e) {
            System.err.println("Error converting time: " + time24 + ", error: " + e.getMessage());
            return time24; // Return original if conversion fails
        }
    }

// ...existing code...

    private List<ReviewDTO> getRecentReviews(Long pharmacyId) {
        // Mock data - implement with actual review repository later
        return List.of(
                new ReviewDTO(1L, "John Doe", 5, "Excellent service and fast delivery!", "2024-08-20"),
                new ReviewDTO(2L, "Jane Smith", 4, "Good pharmacy with helpful staff.", "2024-08-18"),
                new ReviewDTO(3L, "Mike Wilson", 5, "Very professional and quick service.", "2024-08-15"),
                new ReviewDTO(4L, "Sarah Johnson", 4, "Good selection of medicines.", "2024-08-12")
        );
    }

    private List<OTCProductDTO> getPopularProducts(Long pharmacyId) {
        // Mock data - implement with actual product repository later
        return getMockOTCProducts();
    }

    private List<OTCProductDTO> getMockOTCProducts() {
        return List.of(
                new OTCProductDTO(1L, "Panadol", "GSK", "Pain relief medication",
                        new BigDecimal("25.00"), true, null, 4.5, "Pain Relief", "500mg", 100),
                new OTCProductDTO(2L, "Paracetamol", "Generic", "Fever and pain relief",
                        new BigDecimal("15.00"), true, null, 4.2, "Pain Relief", "500mg", 150),
                new OTCProductDTO(3L, "Ibuprofen", "Brufen", "Anti-inflammatory",
                        new BigDecimal("30.00"), true, null, 4.3, "Pain Relief", "400mg", 80)
        );
    }
}