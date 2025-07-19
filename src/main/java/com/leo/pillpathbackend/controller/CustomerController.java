package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.CustomerDTO;
import com.leo.pillpathbackend.dto.CustomerLoginResponse;
import com.leo.pillpathbackend.dto.CustomerRegistrationRequest;
import com.leo.pillpathbackend.dto.CustomerRegistrationResponse;
import com.leo.pillpathbackend.dto.CustomerLoginRequest;
import com.leo.pillpathbackend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerRegistrationResponse> registerCustomer(@RequestBody CustomerRegistrationRequest request) {
        CustomerRegistrationResponse response = customerService.registerCustomer(request);

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping(value = "/check-email/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> checkEmailAvailability(@PathVariable String email) {
        boolean exists = customerService.existsByEmail(email);
        return ResponseEntity.ok(!exists); // Return true if available (not exists)
    }
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerLoginResponse> loginCustomer(@RequestBody CustomerLoginRequest request) {
        CustomerLoginResponse response = customerService.loginCustomer(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // ... rest of your existing methods remain the same
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerDTO> saveCustomer(@RequestBody CustomerDTO customerDTO) {
        CustomerDTO response = customerService.saveCustomer(customerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        List<CustomerDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.status(HttpStatus.OK).body(customers);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        CustomerDTO customer = customerService.getCustomerById(id);
        return ResponseEntity.status(HttpStatus.OK).body(customer);
    }

    @GetMapping(value = "/email/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerDTO> getCustomerByEmail(@PathVariable String email) {
        CustomerDTO customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.status(HttpStatus.OK).body(customer);
    }

    @GetMapping(value = "/username/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerDTO> getCustomerByUsername(@PathVariable String username) {
        CustomerDTO customer = customerService.getCustomerByUsername(username);
        return ResponseEntity.status(HttpStatus.OK).body(customer);
    }

    @GetMapping(value = "/insurance/{insuranceProvider}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CustomerDTO>> getCustomersByInsuranceProvider(@PathVariable String insuranceProvider) {
        List<CustomerDTO> customers = customerService.getCustomersByInsuranceProvider(insuranceProvider);
        return ResponseEntity.status(HttpStatus.OK).body(customers);
    }

    @GetMapping(value = "/pharmacy/{pharmacyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CustomerDTO>> getCustomersByPharmacy(@PathVariable Long pharmacyId) {
        List<CustomerDTO> customers = customerService.getCustomersByPharmacy(pharmacyId);
        return ResponseEntity.status(HttpStatus.OK).body(customers);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerDTO> updateCustomer(@RequestBody CustomerDTO customerDTO) {
        CustomerDTO updatedCustomer = customerService.updateCustomer(customerDTO);
        return ResponseEntity.status(HttpStatus.OK).body(updatedCustomer);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}