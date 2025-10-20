// java
package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.*;
import com.leo.pillpathbackend.dto.request.SocialLoginRequest;
import com.leo.pillpathbackend.entity.Customer;
import com.leo.pillpathbackend.entity.OAuthAccount;
import com.leo.pillpathbackend.entity.User;
import com.leo.pillpathbackend.entity.enums.OAuthProvider;
import com.leo.pillpathbackend.repository.CustomerRepository;
import com.leo.pillpathbackend.repository.OAuthAccountRepository;
import com.leo.pillpathbackend.repository.UserRepository;
import com.leo.pillpathbackend.security.google.GoogleProfile;
import com.leo.pillpathbackend.security.google.GoogleTokenVerifier;
import com.leo.pillpathbackend.service.CustomerService;
import com.leo.pillpathbackend.util.JwtService;
import com.leo.pillpathbackend.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final OAuthAccountRepository oauthAccountRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Override
    public CustomerLoginResponse loginWithGoogle(SocialLoginRequest request) {
        if (request == null || request.getIdToken() == null || request.getIdToken().isBlank()) {
            return createLoginErrorResponse("idToken is required");
        }
        if (!"google".equalsIgnoreCase(request.getProvider())) {
            return createLoginErrorResponse("Unsupported provider");
        }

        GoogleProfile profile;
        try {
            profile = googleTokenVerifier.verify(request.getIdToken());
        } catch (IllegalArgumentException ex) {
            return createLoginErrorResponse(ex.getMessage());
        }

        if (profile.email() == null || profile.email().isBlank()) {
            return createLoginErrorResponse("Google account has no email");
        }
        if (!profile.emailVerified()) {
            return createLoginErrorResponse("Email is not verified by Google");
        }

        Optional<OAuthAccount> linkOpt =
                oauthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, profile.sub());

        Customer customer;
        if (linkOpt.isPresent()) {
            OAuthAccount link = linkOpt.get();
            User linked = link.getUser();
            if (linked instanceof Customer) {
                customer = (Customer) linked;
            } else {
                // If the existing link points to a non-customer user, re-link it to a Customer account
                // 1) try to find existing customer by email
                Optional<Customer> byEmail = customerRepository.findByEmail(profile.email().trim().toLowerCase());
                if (byEmail.isPresent()) {
                    customer = byEmail.get();
                } else {
                    // 2) create new customer
                    customer = new Customer();
                    String email = profile.email().trim().toLowerCase();
                    customer.setEmail(email);
                    customer.setUsername(generateUniqueUsername(email));
                    customer.setFullName(profile.name());
                    customer.setPassword(passwordEncoder.encode("GOOGLE:" + UUID.randomUUID()));
                    customer.setEmailVerified(true);
                    customer.setIsActive(true);
                    customer.setCreatedAt(LocalDateTime.now());
                    customer.setUpdatedAt(LocalDateTime.now());
                    customer = customerRepository.save(customer);
                }
                // 3) reassign the oauth link to the customer
                link.setUser(customer);
                oauthAccountRepository.save(link);
            }
        } else {
            Optional<Customer> byEmail = customerRepository.findByEmail(profile.email().trim().toLowerCase());
            if (byEmail.isPresent()) {
                customer = byEmail.get();
            } else {
                customer = new Customer();
                String email = profile.email().trim().toLowerCase();
                customer.setEmail(email);
                customer.setUsername(generateUniqueUsername(email));
                customer.setFullName(profile.name());
                customer.setPassword(passwordEncoder.encode("GOOGLE:" + UUID.randomUUID()));
                customer.setEmailVerified(true);
                customer.setIsActive(true);
                customer.setCreatedAt(LocalDateTime.now());
                customer.setUpdatedAt(LocalDateTime.now());
                customer = customerRepository.save(customer);
            }

            OAuthAccount link = OAuthAccount.builder()
                    .provider(OAuthProvider.GOOGLE)
                    .providerSubject(profile.sub())
                    .email(profile.email().trim().toLowerCase())
                    .user(customer)
                    .build();
            oauthAccountRepository.save(link);
        }

        CustomerLoginResponse resp = mapper.convertToLoginResponse(customer);
        resp.setToken(jwtService.generateToken(customer.getId(), "CUSTOMER"));
        resp.setSuccess(true);
        resp.setMessage("Login successful");
        return resp;
    }

    private String generateUniqueUsername(String email) {
        String base = email.substring(0, email.indexOf('@')).replaceAll("[^a-zA-Z0-9._-]", "");
        if (base.isBlank()) base = "user";
        String candidate = base;
        int i = 1;
        while (customerRepository.existsByUsername(candidate)) {
            candidate = base + i;
            i++;
        }
        return candidate;
    }

    private CustomerLoginResponse createLoginErrorResponse(String message) {
        CustomerLoginResponse response = new CustomerLoginResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    @Override
    public CustomerRegistrationResponse registerCustomer(CustomerRegistrationRequest request) {
        if (!isValidRegistrationRequest(request)) {
            return createErrorResponse("Invalid registration data");
        }

        if (existsByEmail(request.getEmail())) {
            return createErrorResponse("Email already exists");
        }

        if (!request.isTermsAccepted()) {
            return createErrorResponse("Terms and conditions must be accepted");
        }

        try {
            Customer customer = mapper.convertRegistrationRequestToEntity(request);
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

    @Override
    public CustomerLoginResponse loginCustomer(CustomerLoginRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return createLoginErrorResponse("Email is required");
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return createLoginErrorResponse("Password is required");
            }

            Optional<Customer> customerOpt = customerRepository.findByEmail(request.getEmail().trim().toLowerCase());

            if (customerOpt.isEmpty()) {
                return createLoginErrorResponse("Invalid email or password");
            }

            Customer customer = customerOpt.get();

            if (!customer.getIsActive()) {
                return createLoginErrorResponse("Account is deactivated");
            }

            if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
                return createLoginErrorResponse("Invalid email or password");
            }

            CustomerLoginResponse resp = mapper.convertToLoginResponse(customer);
            resp.setToken(jwtService.generateToken(customer.getId(), "CUSTOMER"));
            return resp;

        } catch (Exception e) {
            return createLoginErrorResponse("Login failed: " + e.getMessage());
        }
    }

    private CustomerRegistrationResponse createErrorResponse(String message) {
        CustomerRegistrationResponse response = new CustomerRegistrationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    @Override
    public CustomerDTO getCustomerProfile(Long customerId) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

            if (!customer.getIsActive()) {
                throw new RuntimeException("Customer account is deactivated");
            }

            return mapper.convertToCustomerDTO(customer);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load customer profile: " + e.getMessage());
        }
    }

    @Override
    public CustomerProfileDTO getCustomerProfileById(Long customerId) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

            if (!customer.getIsActive()) {
                throw new RuntimeException("Customer account is deactivated");
            }

            return mapper.convertToProfileDTO(customer);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load customer profile: " + e.getMessage());
        }
    }

    @Override
    public CustomerProfileDTO getCustomerProfileByEmail(String email) {
        try {
            Customer customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

            if (!customer.getIsActive()) {
                throw new RuntimeException("Customer account is deactivated");
            }

            return mapper.convertToProfileDTO(customer);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load customer profile: " + e.getMessage());
        }
    }

    @Override
    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
    }

    @Deprecated
    @Override
    public void updateProfilePicture(Long customerId, String imageUrl) {
        updateProfilePicture(customerId, imageUrl, null);
    }

    @Override
    public CustomerProfileDTO updateCustomerProfile(Long customerId, CustomerProfileDTO profileDTO) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setFullName(profileDTO.getFullName());
        customer.setPhoneNumber(profileDTO.getPhoneNumber());
        customer.setDateOfBirth(profileDTO.getDateOfBirth());
        customer.setAddress(profileDTO.getAddress());
        customer.setInsuranceProvider(profileDTO.getInsuranceProvider());
        customer.setInsuranceId(profileDTO.getInsuranceId());
        customer.setAllergies(profileDTO.getAllergies());
        customer.setMedicalConditions(profileDTO.getMedicalConditions());
        customer.setEmergencyContactName(profileDTO.getEmergencyContactName());
        customer.setEmergencyContactPhone(profileDTO.getEmergencyContactPhone());
        customer.setPreferredPharmacyId(profileDTO.getPreferredPharmacyId());
        customer.setUpdatedAt(LocalDateTime.now());

        Customer updatedCustomer = customerRepository.save(customer);
        return mapper.convertToProfileDTO(updatedCustomer);
    }

    @Override
    public void updateProfilePicture(Long customerId, String imageUrl, String publicId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setProfilePictureUrl(imageUrl);
        customer.setProfilePicturePublicId(publicId);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }

    @Override
    public String getProfilePicturePublicId(Long customerId) {
        return customerRepository.findById(customerId)
                .map(Customer::getProfilePicturePublicId)
                .orElse(null);
    }

    @Override
    public boolean existsByUsername(String username) {
        return customerRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

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

