package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.CustomerDTO;
import com.leo.pillpathbackend.dto.CustomerRegistrationRequest;
import com.leo.pillpathbackend.dto.CustomerRegistrationResponse;
import com.leo.pillpathbackend.entity.Customer;
import com.leo.pillpathbackend.repository.CustomerRepository;
import com.leo.pillpathbackend.service.CustomerService;
import com.leo.pillpathbackend.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final Mapper mapper;

    @Override
    public CustomerRegistrationResponse registerCustomer(CustomerRegistrationRequest request) {
        // Validate input
        if (!isValidRegistrationRequest(request)) {
            return createErrorResponse("Invalid registration data");
        }

        // Check if passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return createErrorResponse("Passwords do not match");
        }

        // Check if email already exists
        if (existsByEmail(request.getEmail())) {
            return createErrorResponse("Email already exists");
        }

        // Check if terms are accepted
        if (!request.isTermsAccepted()) {
            return createErrorResponse("Terms and conditions must be accepted");
        }

        try {
            Customer customer = mapper.convertRegistrationRequestToEntity(request); // Remove "CustomerRegistrationRequest"
            customer.setCreatedAt(LocalDateTime.now());
            customer.setUpdatedAt(LocalDateTime.now());

            Customer savedCustomer = customerRepository.save(customer);
            return mapper.convertToRegistrationResponse(savedCustomer);
        } catch (Exception e) {
            return createErrorResponse("Registration failed: " + e.getMessage());
        }
    }

    private boolean isValidRegistrationRequest(CustomerRegistrationRequest request) {
        return request.getFirstName() != null && !request.getFirstName().trim().isEmpty() &&
                request.getLastName() != null && !request.getLastName().trim().isEmpty() &&
                request.getEmail() != null && !request.getEmail().trim().isEmpty() &&
                request.getPassword() != null && !request.getPassword().trim().isEmpty() &&
                request.getPhone() != null && !request.getPhone().trim().isEmpty();
    }

    private CustomerRegistrationResponse createErrorResponse(String message) {
        CustomerRegistrationResponse response = new CustomerRegistrationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    @Override
    public boolean existsByUsername(String username) {
        return customerRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    // ... rest of your existing methods remain the same
    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        Customer customer = mapper.convertToCustomerEntity(customerDTO);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        Customer savedCustomer = customerRepository.save(customer);
        return mapper.convertToCustomerDTO(savedCustomer);
    }

    @Override
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        return mapper.convertToCustomerDTO(customer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        Customer existingCustomer = customerRepository.findById(customerDTO.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerDTO.getId()));

        mapper.updateCustomerFromDTO(existingCustomer, customerDTO);
        existingCustomer.setUpdatedAt(LocalDateTime.now());

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return mapper.convertToCustomerDTO(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        customerRepository.delete(customer);
    }

    @Override
    public List<CustomerDTO> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return mapper.convertToCustomerDTOList(customers);
    }

    @Override
    public CustomerDTO getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
        return mapper.convertToCustomerDTO(customer);
    }

    @Override
    public CustomerDTO getCustomerByUsername(String username) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer not found with username: " + username));
        return mapper.convertToCustomerDTO(customer);
    }

    @Override
    public List<CustomerDTO> getCustomersByInsuranceProvider(String insuranceProvider) {
        List<Customer> customers = customerRepository.findByInsuranceProvider(insuranceProvider);
        return mapper.convertToCustomerDTOList(customers);
    }

    @Override
    public List<CustomerDTO> getCustomersByPharmacy(Long pharmacyId) {
        List<Customer> customers = customerRepository.findByPreferredPharmacyId(pharmacyId);
        return mapper.convertToCustomerDTOList(customers);
    }
}