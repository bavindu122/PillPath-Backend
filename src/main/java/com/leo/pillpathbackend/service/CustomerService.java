package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.*;
import com.leo.pillpathbackend.dto.request.SocialLoginRequest;
import com.leo.pillpathbackend.entity.Customer;

import java.util.List;

public interface CustomerService {
    CustomerRegistrationResponse registerCustomer(CustomerRegistrationRequest request);
    CustomerLoginResponse loginCustomer(CustomerLoginRequest request);


    // NEW: Google signup/signin for customers
    CustomerLoginResponse loginWithGoogle(SocialLoginRequest request);

    CustomerDTO saveCustomer(CustomerDTO customerDTO);
    CustomerDTO getCustomerById(Long id);
    CustomerDTO updateCustomer(CustomerDTO customerDTO);
    void deleteCustomer(Long id);
    List<CustomerDTO> getAllCustomers();
    CustomerDTO getCustomerByEmail(String email);
    CustomerDTO getCustomerByUsername(String username);
    List<CustomerDTO> getCustomersByInsuranceProvider(String insuranceProvider);
    List<CustomerDTO> getCustomersByPharmacy(Long pharmacyId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    CustomerDTO getCustomerProfile(Long customerId);
    CustomerProfileDTO getCustomerProfileById(Long customerId);
    CustomerProfileDTO getCustomerProfileByEmail(String email);
    Customer findByEmail(String email);
    void updateProfilePicture(Long customerId, String imageUrl);
    CustomerProfileDTO updateCustomerProfile(Long customerId, CustomerProfileDTO profileDTO);
    void updateProfilePicture(Long customerId, String imageUrl, String publicId);
    String getProfilePicturePublicId(Long customerId);

}