package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.CustomerDTO;
import com.leo.pillpathbackend.dto.CustomerRegistrationRequest;
import com.leo.pillpathbackend.dto.CustomerRegistrationResponse;

import java.util.List;

public interface CustomerService {
    CustomerRegistrationResponse registerCustomer(CustomerRegistrationRequest request);
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
}