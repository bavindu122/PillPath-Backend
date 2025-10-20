package com.leo.pillpathbackend.util;

import com.leo.pillpathbackend.dto.*;
import com.leo.pillpathbackend.dto.order.OrderTotalsDTO;
import com.leo.pillpathbackend.dto.order.PharmacyOrderDTO;
import com.leo.pillpathbackend.dto.order.PharmacyOrderItemDTO;
import com.leo.pillpathbackend.entity.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.modelmapper.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Mapper {

    private final ModelMapper modelMapper;

    public Review convertToReviewEntity(ReviewDTO review) {
        return modelMapper.map(review, Review.class);
    }

    public ReviewDTO convertToReviewDTO(Review review) {
        return modelMapper.map(review, ReviewDTO.class);
    }

    public List<ReviewDTO> convertToReviewDTOList(List<Review> reviews) {
        return reviews.stream()
                .map(this::convertToReviewDTO)
                .toList();
    }

    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPassword(user.getPassword());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setAddress(user.getAddress());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setIsActive(user.getIsActive());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setPhoneVerified(user.getPhoneVerified());
        dto.setUserType(user.getUserType().name());

        return dto;
    }

    public void updateUserFromDTO(User user, UserDTO dto) {
        if (dto.getUsername() != null) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null) {
            user.setPassword(dto.getPassword());
        }
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getDateOfBirth() != null) {
            user.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress());
        }
        if (dto.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(dto.getProfilePictureUrl());
        }
        if (dto.getIsActive() != null) {
            user.setIsActive(dto.getIsActive());
        }
        if (dto.getEmailVerified() != null) {
            user.setEmailVerified(dto.getEmailVerified());
        }
        if (dto.getPhoneVerified() != null) {
            user.setPhoneVerified(dto.getPhoneVerified());
        }
    }

    //Customer specific methods
    public Customer convertToCustomerEntity(CustomerDTO customerDTO) {
        return modelMapper.map(customerDTO, Customer.class);
    }
    public CustomerDTO convertToCustomerDTO(Customer customer) {
        return modelMapper.map(customer, CustomerDTO.class);
    }
    public List<CustomerDTO> convertToCustomerDTOList(List<Customer> customers) {
        return customers.stream()
                .map(this::convertToCustomerDTO)
                .toList();
    }
    public void updateCustomerFromDTO(Customer customer, CustomerDTO dto) {
        if (dto.getUsername() != null) {
            customer.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null) {
            customer.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null) {
            customer.setPassword(dto.getPassword());
        }
        if (dto.getFullName() != null) {
            customer.setFullName(dto.getFullName());
        }
        if (dto.getPhoneNumber() != null) {
            customer.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getDateOfBirth() != null) {
            customer.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getAddress() != null) {
            customer.setAddress(dto.getAddress());
        }
        if (dto.getProfilePictureUrl() != null) {
            customer.setProfilePictureUrl(dto.getProfilePictureUrl());
        }
        if (dto.getIsActive() != null) {
            customer.setIsActive(dto.getIsActive());
        }
        if (dto.getEmailVerified() != null) {
            customer.setEmailVerified(dto.getEmailVerified());
        }
        if (dto.getPhoneVerified() != null) {
            customer.setPhoneVerified(dto.getPhoneVerified());
        }
        // Customer specific fields
        if (dto.getInsuranceProvider() != null) {
            customer.setInsuranceProvider(dto.getInsuranceProvider());
        }
        if (dto.getInsuranceId() != null) {
            customer.setInsuranceId(dto.getInsuranceId());
        }
        if (dto.getAllergies() != null) {
            customer.setAllergies(dto.getAllergies());
        }
        if (dto.getMedicalConditions() != null) {
            customer.setMedicalConditions(dto.getMedicalConditions());
        }
        if (dto.getEmergencyContactName() != null) {
            customer.setEmergencyContactName(dto.getEmergencyContactName());
        }
        if (dto.getEmergencyContactPhone() != null) {
            customer.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        }
        if (dto.getPreferredPharmacyId() != null) {
            customer.setPreferredPharmacyId(dto.getPreferredPharmacyId());
        }
    }
    private final PasswordEncoder passwordEncoder;
    public Customer convertRegistrationRequestToEntity(CustomerRegistrationRequest request) {
        Customer customer = new Customer();

        // Generate username from first and last name
        String username = generateUsername(request.getFirstName(), request.getLastName());
        customer.setUsername(username);

        customer.setEmail(request.getEmail());
        // ENCRYPT PASSWORD HERE
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setFullName(request.getFirstName() + " " + request.getLastName());
        customer.setPhoneNumber(request.getPhone());
        customer.setDateOfBirth(request.getDateOfBirth());

        // Set default values
        customer.setIsActive(true);
        customer.setEmailVerified(false);
        customer.setPhoneVerified(false);
        customer.setAllergies(new ArrayList<>());
        customer.setMedicalConditions(new ArrayList<>());

        return customer;
    }
    private String generateUsername(String firstName, String lastName) {
        String baseUsername = (firstName + lastName).toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
        return baseUsername + System.currentTimeMillis() % 10000;
    }
    public CustomerRegistrationResponse convertToRegistrationResponse(Customer customer) {
        CustomerRegistrationResponse response = new CustomerRegistrationResponse();
        response.setId(customer.getId());
        response.setUsername(customer.getUsername());
        response.setEmail(customer.getEmail());
        response.setFullName(customer.getFullName());
        response.setSuccess(true);
        response.setMessage("Customer registered successfully");
        return response;
    }
    public CustomerLoginResponse convertToLoginResponse(Customer customer) {
        CustomerLoginResponse response = new CustomerLoginResponse();
        response.setId(customer.getId());
        response.setUsername(customer.getUsername());
        response.setEmail(customer.getEmail());
        response.setFullName(customer.getFullName());
        response.setSuccess(true);
        response.setMessage("Login successful");
        // Token will be set by the Service layer using JwtService
        // response.setToken(...);
        response.setUser(convertToProfileDTO(customer)); // Safe profile data
        return response;
    }
    public CustomerProfileDTO convertToProfileDTO(Customer customer) {
        CustomerProfileDTO dto = new CustomerProfileDTO();
        dto.setId(customer.getId());
        dto.setUsername(customer.getUsername());
        dto.setEmail(customer.getEmail());
        dto.setFullName(customer.getFullName());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setDateOfBirth(customer.getDateOfBirth());
        dto.setAddress(customer.getAddress());
        dto.setProfilePictureUrl(customer.getProfilePictureUrl());
        dto.setEmailVerified(customer.getEmailVerified());
        dto.setPhoneVerified(customer.getPhoneVerified());
        dto.setInsuranceProvider(customer.getInsuranceProvider());
        dto.setInsuranceId(customer.getInsuranceId());
        dto.setAllergies(customer.getAllergies());
        dto.setMedicalConditions(customer.getMedicalConditions());
        dto.setEmergencyContactName(customer.getEmergencyContactName());
        dto.setEmergencyContactPhone(customer.getEmergencyContactPhone());
        dto.setPreferredPharmacyId(customer.getPreferredPharmacyId());
        return dto;
    }

    //Pharmacy && PharmacyAdmin specific methods
    // Convert registration request to Pharmacy entity
    public Pharmacy convertToPharmacyEntity(PharmacyRegistrationRequest request) {
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setName(request.getName());
        pharmacy.setAddress(request.getAddress());
        pharmacy.setPhoneNumber(request.getPhoneNumber());
        pharmacy.setEmail(request.getEmail());
        pharmacy.setLicenseNumber(request.getLicenseNumber());
        pharmacy.setLicenseExpiryDate(request.getLicenseExpiryDate());
        // Ensure operatingHours is never null - use empty map if null
        pharmacy.setOperatingHours(request.getOperatingHours() != null ? request.getOperatingHours() : new java.util.HashMap<>());
        // Ensure services is never null - use empty list if null
        pharmacy.setServices(request.getServices() != null ? request.getServices() : new java.util.ArrayList<>());
        pharmacy.setDeliveryAvailable(request.getDeliveryAvailable());
        pharmacy.setDeliveryRadius(request.getDeliveryRadius());
        pharmacy.setIsVerified(false);

        // Add location mapping
        pharmacy.setLatitude(request.getLatitude());
        pharmacy.setLongitude(request.getLongitude());

        pharmacy.setIsVerified(false);
        pharmacy.setIsActive(true);

        return pharmacy;
    }

    // Convert registration request to PharmacyAdmin entity
    public PharmacyAdmin convertToPharmacyAdminEntity(PharmacyRegistrationRequest request, Pharmacy pharmacy) {
        PharmacyAdmin admin = new PharmacyAdmin();

        // Generate username from first and last name
        String username = generateUsername(request.getAdminFirstName(), request.getAdminLastName());
        admin.setUsername(username);

        admin.setEmail(request.getAdminEmail());
        admin.setPassword(passwordEncoder.encode(request.getAdminPassword()));
        admin.setFullName(request.getAdminFirstName() + " " + request.getAdminLastName());
        admin.setPhoneNumber(request.getAdminPhoneNumber());

        // Set PharmacyAdmin specific fields
        admin.setPharmacy(pharmacy);
        admin.setPosition(request.getAdminPosition());
        admin.setLicenseNumber(request.getAdminLicenseNumber());
        admin.setHireDate(LocalDate.now());
        admin.setIsPrimaryAdmin(true); // First admin is primary
        admin.setPermissions(List.of("FULL_ACCESS")); // Default permissions

        // Set default User values
        admin.setIsActive(true);
        admin.setEmailVerified(false);
        admin.setPhoneVerified(false);

        return admin;
    }

    // Convert to registration response
    public PharmacyRegistrationResponse convertToPharmacyRegistrationResponse(Pharmacy pharmacy, PharmacyAdmin admin) {
        PharmacyRegistrationResponse response = new PharmacyRegistrationResponse();
        response.setPharmacyId(pharmacy.getId());
        response.setAdminId(admin.getId());
        response.setPharmacyName(pharmacy.getName());
        response.setAdminUsername(admin.getUsername());
        response.setAdminEmail(admin.getEmail());
        response.setSuccess(true);
        response.setMessage("Pharmacy registration submitted successfully. Pending approval.");
        return response;
    }

    // Convert to login response
    // Add these methods to your Mapper class

    public PharmacyAdminLoginResponse convertToPharmacyAdminLoginResponse(PharmacyAdmin admin) {
        PharmacyAdminLoginResponse response = new PharmacyAdminLoginResponse();

        response.setAdminId(admin.getId());
        response.setPharmacyId(admin.getPharmacy().getId());
        response.setUsername(admin.getUsername());
        response.setEmail(admin.getEmail());
        response.setFullName(admin.getFullName());
        response.setSuccess(true);
        response.setMessage("Login successful");
        // Note: You should implement JWT token generation here
        response.setToken("jwt_token_here"); // TODO: Implement JWT

        // Set user profile
        PharmacyAdminProfileDTO userProfile = convertToPharmacyAdminProfileDTO(admin);
        response.setUser(userProfile);

        return response;
    }

    public PharmacyAdminProfileDTO convertToPharmacyAdminProfileDTO(PharmacyAdmin admin) {
        PharmacyAdminProfileDTO dto = new PharmacyAdminProfileDTO();

        dto.setId(admin.getId());
        dto.setUsername(admin.getUsername());
        dto.setEmail(admin.getEmail());
        dto.setFullName(admin.getFullName());
        dto.setPhoneNumber(admin.getPhoneNumber());
        dto.setDateOfBirth(admin.getDateOfBirth());
        dto.setAddress(admin.getAddress());
        dto.setProfilePictureUrl(admin.getProfilePictureUrl());
        dto.setEmailVerified(admin.getEmailVerified());
        dto.setPhoneVerified(admin.getPhoneVerified());

        // PharmacyAdmin specific fields
        dto.setPharmacyId(admin.getPharmacy().getId());
        dto.setPharmacyName(admin.getPharmacy().getName());
        dto.setPosition(admin.getPosition());
        dto.setLicenseNumber(admin.getLicenseNumber());
        dto.setHireDate(admin.getHireDate());
        dto.setIsPrimaryAdmin(admin.getIsPrimaryAdmin());
        dto.setPermissions(admin.getPermissions());

        return dto;
    }
    // Add these methods to your existing Mapper class

    public PharmacyDTO convertToPharmacyDTO(Pharmacy pharmacy) {
        PharmacyDTO dto = modelMapper.map(pharmacy, PharmacyDTO.class);

        // Ensure operatingHours is never null
        if (dto.getOperatingHours() == null) {
            dto.setOperatingHours(new java.util.HashMap<>());
        }
        
        // Ensure services is never null
        if (dto.getServices() == null) {
            dto.setServices(new java.util.ArrayList<>());
        }

        // Set derived status field
        if (pharmacy.getIsActive() && pharmacy.getIsVerified()) {
            dto.setStatus("Active");
        } else if (!pharmacy.getIsVerified() && pharmacy.getIsActive()) {
            dto.setStatus("Pending");
        } else if (!pharmacy.getIsActive() && pharmacy.getIsVerified()) {
            dto.setStatus("Suspended");
        } else {
            dto.setStatus("Rejected");
        }

        return dto;
    }

    public void updatePharmacyFromDTO(Pharmacy pharmacy, PharmacyDTO pharmacyDTO) {
        if (pharmacyDTO.getName() != null) {
            pharmacy.setName(pharmacyDTO.getName());
        }
        if (pharmacyDTO.getAddress() != null) {
            pharmacy.setAddress(pharmacyDTO.getAddress());
        }
        if (pharmacyDTO.getLatitude() != null) {
            pharmacy.setLatitude(pharmacyDTO.getLatitude());
        }
        if (pharmacyDTO.getLongitude() != null) {
            pharmacy.setLongitude(pharmacyDTO.getLongitude());
        }
        if (pharmacyDTO.getPhoneNumber() != null) {
            pharmacy.setPhoneNumber(pharmacyDTO.getPhoneNumber());
        }
        if (pharmacyDTO.getEmail() != null) {
            pharmacy.setEmail(pharmacyDTO.getEmail());
        }
        if (pharmacyDTO.getLicenseNumber() != null) {
            pharmacy.setLicenseNumber(pharmacyDTO.getLicenseNumber());
        }
        if (pharmacyDTO.getLicenseExpiryDate() != null) {
            pharmacy.setLicenseExpiryDate(pharmacyDTO.getLicenseExpiryDate());
        }
        if (pharmacyDTO.getLogoUrl() != null) {
            pharmacy.setLogoUrl(pharmacyDTO.getLogoUrl());
        }
        if (pharmacyDTO.getLogoPublicId() != null) {
            pharmacy.setLogoPublicId(pharmacyDTO.getLogoPublicId());
        }
        if (pharmacyDTO.getBannerUrl() != null) {
            pharmacy.setBannerUrl(pharmacyDTO.getBannerUrl());
        }
        if (pharmacyDTO.getBannerPublicId() != null) {
            pharmacy.setBannerPublicId(pharmacyDTO.getBannerPublicId());
        }
        if (pharmacyDTO.getOperatingHours() != null) {
            pharmacy.setOperatingHours(pharmacyDTO.getOperatingHours());
        }
        if (pharmacyDTO.getServices() != null) {
            pharmacy.setServices(pharmacyDTO.getServices());
        }
        if (pharmacyDTO.getDeliveryAvailable() != null) {
            pharmacy.setDeliveryAvailable(pharmacyDTO.getDeliveryAvailable());
        }
        if (pharmacyDTO.getDeliveryRadius() != null) {
            pharmacy.setDeliveryRadius(pharmacyDTO.getDeliveryRadius());
        }
    }

    public PharmacistProfileDTO convertToPharmacistProfileDTO(PharmacistUser pharmacist) {
        if (pharmacist == null) {
            return null;
        }
        PharmacistProfileDTO dto = new PharmacistProfileDTO();
        dto.setId(pharmacist.getId());
        dto.setEmail(pharmacist.getEmail());
        dto.setFullName(pharmacist.getFullName());
        dto.setPhoneNumber(pharmacist.getPhoneNumber());
        dto.setDateOfBirth(pharmacist.getDateOfBirth());
        dto.setProfilePictureUrl(pharmacist.getProfilePictureUrl());
        if (pharmacist.getPharmacy() != null) {
            dto.setPharmacyId(pharmacist.getPharmacy().getId());
            dto.setPharmacyName(pharmacist.getPharmacy().getName());
        }
        dto.setLicenseNumber(pharmacist.getLicenseNumber());
        dto.setLicenseExpiryDate(pharmacist.getLicenseExpiryDate());
        dto.setSpecialization(pharmacist.getSpecialization());
        dto.setYearsOfExperience(pharmacist.getYearsOfExperience());
        dto.setHireDate(pharmacist.getHireDate());
        dto.setShiftSchedule(pharmacist.getShiftSchedule());
        dto.setCertifications(pharmacist.getCertifications());
        dto.setIsVerified(pharmacist.getIsVerified());
        dto.setIsActive(pharmacist.getIsActive());
        return dto;
    }

    // Prescription mappings
    public PrescriptionDTO toPrescriptionDTO(Prescription p) {
        if (p == null) return null;
        PrescriptionDTO dto = new PrescriptionDTO();
        dto.setId(p.getId());
        dto.setCode(p.getCode());
        if (p.getCustomer() != null) {
            dto.setCustomerId(p.getCustomer().getId());
            dto.setCustomerName(p.getCustomer().getFullName());
        }
        if (p.getPharmacy() != null) {
            dto.setPharmacyId(p.getPharmacy().getId());
            dto.setPharmacyName(p.getPharmacy().getName());
        }
        dto.setStatus(p.getStatus());
        dto.setImageUrl(p.getImageUrl());
        dto.setNote(p.getNote());
        dto.setDeliveryPreference(p.getDeliveryPreference());
        dto.setDeliveryAddress(p.getDeliveryAddress());
        dto.setLatitude(p.getLatitude());
        dto.setLongitude(p.getLongitude());
        dto.setTotalPrice(p.getTotalPrice());
        if (p.getItems() != null) {
            dto.setItems(p.getItems().stream().map(this::toPrescriptionItemDTO).toList());
        }
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }

    public PrescriptionListItemDTO toPrescriptionListItemDTO(Prescription p) {
        if (p == null) return null;
        return PrescriptionListItemDTO.builder()
                .id(p.getId())
                .code(p.getCode())
                .pharmacyName(p.getPharmacy() != null ? p.getPharmacy().getName() : null)
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public PrescriptionItemDTO toPrescriptionItemDTO(PrescriptionItem item) {
        if (item == null) return null;
        return PrescriptionItemDTO.builder()
                .id(item.getId())
                .medicineName(item.getMedicineName())
                .genericName(item.getGenericName())
                .dosage(item.getDosage())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .available(item.getAvailable())
                .notes(item.getNotes())
                .build();
    }

    public PrescriptionItem toPrescriptionItemEntity(PrescriptionItemDTO dto) {
        if (dto == null) return null;
        return PrescriptionItem.builder()
                .id(dto.getId())
                .medicineName(dto.getMedicineName())
                .genericName(dto.getGenericName())
                .dosage(dto.getDosage())
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .totalPrice(dto.getTotalPrice())
                .available(dto.getAvailable())
                .notes(dto.getNotes())
                .build();
    }

    public OrderTotalsDTO toOrderTotalsDTO(CustomerOrder order) {
        if (order == null) return null;
        return OrderTotalsDTO.builder()
                .subtotal(order.getSubtotal())
                .discount(order.getDiscount())
                .tax(order.getTax())
                .shipping(order.getShipping())
                .total(order.getTotal())
                .currency(order.getCurrency())
                .build();
    }

    public PharmacyOrderDTO toPharmacyOrderDTO(PharmacyOrder po, boolean includeItems) {
        if (po == null) return null;
        
        List<PharmacyOrderItemDTO> items = null;
        if (includeItems && po.getItems() != null) {
            items = po.getItems().stream()
                    .map(item -> PharmacyOrderItemDTO.builder()
                            .itemId(item.getId())
                            .medicineName(item.getMedicineName())
                            .genericName(item.getGenericName())
                            .dosage(item.getDosage())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(item.getTotalPrice())
                            .build())
                    .toList();
        }

        CustomerOrder parent = po.getCustomerOrder();
        
        return PharmacyOrderDTO.builder()
                .pharmacyOrderId(po.getId())
                .pharmacyId(po.getPharmacy() != null ? po.getPharmacy().getId() : null)
                .pharmacyName(po.getPharmacy() != null ? po.getPharmacy().getName() : null)
                .status(po.getStatus())
                .pickupCode(po.getPickupCode())
                .pickupLocation(po.getPickupLocation())
                .pickupLat(po.getPickupLat())
                .pickupLng(po.getPickupLng())
                .customerNote(po.getCustomerNote())
                .pharmacistNote(po.getPharmacistNote())
                .createdAt(po.getCreatedAt() != null ? po.getCreatedAt().toString() : null)
                .updatedAt(po.getUpdatedAt() != null ? po.getUpdatedAt().toString() : null)
                .orderCode(parent != null ? parent.getOrderCode() : null)
                .items(items)
                .totals(OrderTotalsDTO.builder()
                        .subtotal(po.getSubtotal())
                        .discount(po.getDiscount())
                        .tax(po.getTax())
                        .shipping(po.getShipping())
                        .total(po.getTotal())
                        .currency(parent != null ? parent.getCurrency() : "LKR")
                        .build())
                .build();
    }
}