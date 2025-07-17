package com.leo.pillpathbackend.util;

import com.leo.pillpathbackend.entity.User;
import com.leo.pillpathbackend.dto.UserDTO;
import com.leo.pillpathbackend.dto.CustomerRegistrationRequest;
import com.leo.pillpathbackend.dto.CustomerRegistrationResponse;
import com.leo.pillpathbackend.entity.Review;
import com.leo.pillpathbackend.dto.ReviewDTO;
import com.leo.pillpathbackend.entity.Customer;
import com.leo.pillpathbackend.dto.CustomerDTO;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.modelmapper.*;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
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
}
