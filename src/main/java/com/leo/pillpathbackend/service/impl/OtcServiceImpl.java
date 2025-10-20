package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.OtcDTO;
import com.leo.pillpathbackend.dto.OtcStockAlertDTO;
import com.leo.pillpathbackend.dto.PharmacyWithProductDTO;
import com.leo.pillpathbackend.entity.Otc;
import com.leo.pillpathbackend.entity.Pharmacy;
import com.leo.pillpathbackend.repository.OtcRepository;
import com.leo.pillpathbackend.repository.PharmacyRepository;
import com.leo.pillpathbackend.service.OtcService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OtcServiceImpl implements OtcService {

    private final OtcRepository otcRepository;
    private final PharmacyRepository pharmacyRepository;

    @Override
    public OtcDTO createOtcForPharmacy(Long pharmacyId, OtcDTO otcDTO) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy not found with id: " + pharmacyId));

        Otc otc = new Otc();
        otc.setName(otcDTO.getName());
        otc.setDescription(otcDTO.getDescription());
        otc.setPrice(otcDTO.getPrice());
        otc.setStock(otcDTO.getStock());
        otc.setImageUrl(otcDTO.getImageUrl());
        otc.setImagePublicId(otcDTO.getImagePublicId());
        otc.setCategory(otcDTO.getCategory());
        otc.setDosage(otcDTO.getDosage());
        otc.setManufacturer(otcDTO.getManufacturer());
        otc.setPackSize(otcDTO.getPackSize());
        otc.setPharmacy(pharmacy);
        otc.setAddedToStore(true);
        otc.setStatus(calculateStatus(otcDTO.getStock()));

        Otc savedOtc = otcRepository.save(otc);
        return mapToOtcDTO(savedOtc);
    }

    @Override
    public List<OtcDTO> getOtcsByPharmacy(Long pharmacyId) {
        return otcRepository.findByPharmacyId(pharmacyId).stream()
                .map(this::mapToOtcDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OtcDTO getOtcById(Long otcId) {
        Otc otc = otcRepository.findById(otcId)
                .orElseThrow(() -> new EntityNotFoundException("OTC item not found with id: " + otcId));
        return mapToOtcDTO(otc);
    }

    @Override
    public List<OtcDTO> getAllOtcs() {
        return otcRepository.findAll().stream()
                .map(this::mapToOtcDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OtcDTO updateOtc(Long otcId, OtcDTO updatedOtcDto) {
        Otc existingOtc = otcRepository.findById(otcId)
                .orElseThrow(() -> new EntityNotFoundException("OTC item not found with id: " + otcId));

        existingOtc.setName(updatedOtcDto.getName());
        existingOtc.setDescription(updatedOtcDto.getDescription());
        existingOtc.setPrice(updatedOtcDto.getPrice());
        existingOtc.setStock(updatedOtcDto.getStock());
        existingOtc.setCategory(updatedOtcDto.getCategory());
        existingOtc.setDosage(updatedOtcDto.getDosage());
        existingOtc.setManufacturer(updatedOtcDto.getManufacturer());
        existingOtc.setPackSize(updatedOtcDto.getPackSize());
        
        if (updatedOtcDto.getImageUrl() != null) {
            existingOtc.setImageUrl(updatedOtcDto.getImageUrl());
            existingOtc.setImagePublicId(updatedOtcDto.getImagePublicId());
        }

        existingOtc.setStatus(calculateStatus(updatedOtcDto.getStock()));

        Otc updatedOtc = otcRepository.save(existingOtc);
        return mapToOtcDTO(updatedOtc);
    }

    @Override
    public void deleteOtc(Long otcId) {
        if (!otcRepository.existsById(otcId)) {
            throw new EntityNotFoundException("OTC item not found with id: " + otcId);
        }
        otcRepository.deleteById(otcId);
    }

    @Override
    public List<PharmacyWithProductDTO> getPharmaciesByProductName(String productName) {
        List<Otc> matchingProducts = otcRepository.findByNameContainingIgnoreCase(productName);
        
        if (matchingProducts.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, List<Otc>> productsByPharmacy = matchingProducts.stream()
                .collect(Collectors.groupingBy(Otc::getPharmacyId));

        List<PharmacyWithProductDTO> result = new ArrayList<>();
        
        for (Map.Entry<Long, List<Otc>> entry : productsByPharmacy.entrySet()) {
            Long pharmacyId = entry.getKey();
            List<Otc> products = entry.getValue();
            
            Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId).orElse(null);
            
            if (pharmacy != null) {
                PharmacyWithProductDTO dto = new PharmacyWithProductDTO();
                dto.setPharmacyId(pharmacyId);
                dto.setPharmacyName(pharmacy.getName());
                dto.setAddress(pharmacy.getAddress());
                dto.setPhoneNumber(pharmacy.getPhoneNumber());
                dto.setEmail(pharmacy.getEmail());
                
                List<OtcDTO> productDTOs = products.stream()
                        .map(this::mapToOtcDTO)
                        .collect(Collectors.toList());
                dto.setProducts(productDTOs);
                
                result.add(dto);
            }
        }
        
        return result;
    }

    @Override
    public List<OtcStockAlertDTO> getStockAlerts(Long pharmacyId) {
        List<Otc> products = otcRepository.findStockAlertsByPharmacyId(pharmacyId);
        
        return products.stream()
                .map(this::convertToStockAlertDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getStockStatistics(Long pharmacyId) {
        Long lowStockCount = otcRepository.countLowStockByPharmacyId(pharmacyId);
        Long outOfStockCount = otcRepository.countByPharmacyIdAndStock(pharmacyId, 0);
        
        return Map.of(
                "lowStockCount", lowStockCount,
                "outOfStockCount", outOfStockCount,
                "totalAlerts", lowStockCount + outOfStockCount
        );
    }

    private OtcDTO mapToOtcDTO(Otc otc) {
        OtcDTO otcDto = new OtcDTO();
        otcDto.setId(otc.getId());
        otcDto.setName(otc.getName());
        otcDto.setDescription(otc.getDescription());
        otcDto.setPrice(otc.getPrice());
        otcDto.setStock(otc.getStock());
        otcDto.setImageUrl(otc.getImageUrl());
        otcDto.setImagePublicId(otc.getImagePublicId());
        otcDto.setStatus(otc.getStatus());
        otcDto.setPharmacyId(otc.getPharmacyId());
        otcDto.setCategory(otc.getCategory());
        otcDto.setDosage(otc.getDosage());
        otcDto.setManufacturer(otc.getManufacturer());
        otcDto.setPackSize(otc.getPackSize());
        return otcDto;
    }

    private OtcStockAlertDTO convertToStockAlertDTO(Otc otc) {
        String stockStatus;
        if (otc.getStock() == null || otc.getStock() == 0) {
            stockStatus = "OUT_OF_STOCK";
        } else if (otc.getStock() <= 10) {
            stockStatus = "LOW_STOCK";
        } else {
            stockStatus = "IN_STOCK";
        }

        return OtcStockAlertDTO.builder()
                .id(otc.getId())
                .name(otc.getName())
                .description(otc.getDescription())
                .price(otc.getPrice())
                .stock(otc.getStock())
                .status(stockStatus)
                .imageUrl(otc.getImageUrl())
                .category(otc.getCategory())
                .dosage(otc.getDosage())
                .manufacturer(otc.getManufacturer())
                .packSize(otc.getPackSize())
                .pharmacyId(otc.getPharmacy() != null ? otc.getPharmacy().getId() : null)
                .pharmacyName(otc.getPharmacy() != null ? otc.getPharmacy().getName() : null)
                .updatedAt(otc.getUpdatedAt())
                .build();
    }

    private String calculateStatus(Integer stock) {
        if (stock == null || stock == 0) {
            return "Out of Stock";
        } else if (stock <= 10) {
            return "Low Stock";
        } else {
            return "In Stock";
        }
    }
}








































// package com.leo.pillpathbackend.service.impl;

// import com.leo.pillpathbackend.dto.OtcDTO;
// import com.leo.pillpathbackend.dto.PharmacyWithProductDTO;
// import com.leo.pillpathbackend.entity.Otc;
// import com.leo.pillpathbackend.entity.Pharmacy;
// import com.leo.pillpathbackend.repository.OtcRepository;
// import com.leo.pillpathbackend.repository.PharmacyRepository;
// import com.leo.pillpathbackend.service.OtcService;
// import jakarta.persistence.EntityNotFoundException;
// import lombok.AllArgsConstructor;
// import org.springframework.stereotype.Service;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;

// @Service
// @AllArgsConstructor
// public class OtcServiceImpl implements OtcService {

//     private final OtcRepository otcRepository;
//     private final PharmacyRepository pharmacyRepository;

//     @Override
//     public OtcDTO createOtcForPharmacy(Long pharmacyId, OtcDTO otcDTO) {
//         Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
//                 .orElseThrow(() -> new EntityNotFoundException("Pharmacy not found with id: " + pharmacyId));

//         Otc otc = new Otc();
//         otc.setName(otcDTO.getName());
//         otc.setDescription(otcDTO.getDescription());
//         otc.setPrice(otcDTO.getPrice());
//         otc.setStock(otcDTO.getStock());
//         otc.setImageUrl(otcDTO.getImageUrl());
//         otc.setImagePublicId(otcDTO.getImagePublicId());
//         otc.setCategory(otcDTO.getCategory());
//         otc.setDosage(otcDTO.getDosage());
//         otc.setManufacturer(otcDTO.getManufacturer());
//         otc.setPackSize(otcDTO.getPackSize());
//         otc.setPharmacy(pharmacy);
//         otc.setAddedToStore(true);
//         otc.setStatus(calculateStatus(otcDTO.getStock()));

//         Otc savedOtc = otcRepository.save(otc);
//         return mapToOtcDTO(savedOtc);
//     }

//     @Override
//     public List<OtcDTO> getOtcsByPharmacy(Long pharmacyId) {
//         return otcRepository.findByPharmacyId(pharmacyId).stream()
//                 .map(this::mapToOtcDTO)
//                 .collect(Collectors.toList());
//     }

//     @Override
//     public OtcDTO getOtcById(Long otcId) {
//         Otc otc = otcRepository.findById(otcId)
//                 .orElseThrow(() -> new EntityNotFoundException("OTC item not found with id: " + otcId));
//         return mapToOtcDTO(otc);
//     }

//     @Override
//     public List<OtcDTO> getAllOtcs() {
//         return otcRepository.findAll().stream()
//                 .map(this::mapToOtcDTO)
//                 .collect(Collectors.toList());
//     }

//     @Override
//     public OtcDTO updateOtc(Long otcId, OtcDTO updatedOtcDto) {
//         Otc existingOtc = otcRepository.findById(otcId)
//                 .orElseThrow(() -> new EntityNotFoundException("OTC item not found with id: " + otcId));

//         existingOtc.setName(updatedOtcDto.getName());
//         existingOtc.setDescription(updatedOtcDto.getDescription());
//         existingOtc.setPrice(updatedOtcDto.getPrice());
//         existingOtc.setStock(updatedOtcDto.getStock());
//         existingOtc.setCategory(updatedOtcDto.getCategory());
//         existingOtc.setDosage(updatedOtcDto.getDosage());
//         existingOtc.setManufacturer(updatedOtcDto.getManufacturer());
//         existingOtc.setPackSize(updatedOtcDto.getPackSize());
        
//         if (updatedOtcDto.getImageUrl() != null) {
//             existingOtc.setImageUrl(updatedOtcDto.getImageUrl());
//             existingOtc.setImagePublicId(updatedOtcDto.getImagePublicId());
//         }

//         existingOtc.setStatus(calculateStatus(updatedOtcDto.getStock()));

//         Otc updatedOtc = otcRepository.save(existingOtc);
//         return mapToOtcDTO(updatedOtc);
//     }

//     @Override
//     public void deleteOtc(Long otcId) {
//         if (!otcRepository.existsById(otcId)) {
//             throw new EntityNotFoundException("OTC item not found with id: " + otcId);
//         }
//         otcRepository.deleteById(otcId);
//     }

//     @Override
//     public List<PharmacyWithProductDTO> getPharmaciesByProductName(String productName) {
//         // Find all products matching the name (case-insensitive, partial match)
//         List<Otc> matchingProducts = otcRepository.findByNameContainingIgnoreCase(productName);
        
//         if (matchingProducts.isEmpty()) {
//             return new ArrayList<>();
//         }

//         // Group products by pharmacy ID
//         Map<Long, List<Otc>> productsByPharmacy = matchingProducts.stream()
//                 .collect(Collectors.groupingBy(Otc::getPharmacyId));

//         // Build the response with pharmacy details
//         List<PharmacyWithProductDTO> result = new ArrayList<>();
        
//         for (Map.Entry<Long, List<Otc>> entry : productsByPharmacy.entrySet()) {
//             Long pharmacyId = entry.getKey();
//             List<Otc> products = entry.getValue();
            
//             // Fetch pharmacy details
//             Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId).orElse(null);
            
//             if (pharmacy != null) {
//                 PharmacyWithProductDTO dto = new PharmacyWithProductDTO();
//                 dto.setPharmacyId(pharmacyId);
//                 dto.setPharmacyName(pharmacy.getName());
//                 dto.setAddress(pharmacy.getAddress());
//                 dto.setPhoneNumber(pharmacy.getPhoneNumber());
//                 dto.setEmail(pharmacy.getEmail());
                
//                 // Convert products to DTOs
//                 List<OtcDTO> productDTOs = products.stream()
//                         .map(this::mapToOtcDTO)
//                         .collect(Collectors.toList());
//                 dto.setProducts(productDTOs);
                
//                 result.add(dto);
//             }
//         }
        
//         return result;
//     }

//     private OtcDTO mapToOtcDTO(Otc otc) {
//         OtcDTO otcDto = new OtcDTO();
//         otcDto.setId(otc.getId());
//         otcDto.setName(otc.getName());
//         otcDto.setDescription(otc.getDescription());
//         otcDto.setPrice(otc.getPrice());
//         otcDto.setStock(otc.getStock());
//         otcDto.setImageUrl(otc.getImageUrl());
//         otcDto.setImagePublicId(otc.getImagePublicId());
//         otcDto.setStatus(otc.getStatus());
//         otcDto.setPharmacyId(otc.getPharmacyId());
//         otcDto.setCategory(otc.getCategory());
//         otcDto.setDosage(otc.getDosage());
//         otcDto.setManufacturer(otc.getManufacturer());
//         otcDto.setPackSize(otc.getPackSize());
//         return otcDto;
//     }

//     private String calculateStatus(Integer stock) {
//         if (stock == null || stock == 0) {
//             return "Out of Stock";
//         } else if (stock <= 10) {
//             return "Low Stock";
//         } else {
//             return "In Stock";
//         }
//     }
// }





























// package com.leo.pillpathbackend.service.impl;

// import com.leo.pillpathbackend.dto.OtcDTO;
// import com.leo.pillpathbackend.entity.Otc;
// import com.leo.pillpathbackend.entity.Pharmacy;
// import com.leo.pillpathbackend.repository.OtcRepository;
// import com.leo.pillpathbackend.repository.PharmacyRepository;
// import com.leo.pillpathbackend.service.OtcService;
// import jakarta.persistence.EntityNotFoundException;
// import lombok.AllArgsConstructor;
// import org.springframework.stereotype.Service;

// import java.util.List;
// import java.util.stream.Collectors;

// @Service
// @AllArgsConstructor
// public class OtcServiceImpl implements OtcService {

//     private final OtcRepository otcRepository;
//     private final PharmacyRepository pharmacyRepository;

// @Override
// public OtcDTO createOtcForPharmacy(Long pharmacyId, OtcDTO otcDTO) {
//     Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
//             .orElseThrow(() -> new EntityNotFoundException("Pharmacy not found with id: " + pharmacyId));

//     Otc otc = new Otc();
//     otc.setName(otcDTO.getName());
//     otc.setDescription(otcDTO.getDescription());
//     otc.setPrice(otcDTO.getPrice());
//     otc.setStock(otcDTO.getStock());
//     otc.setImageUrl(otcDTO.getImageUrl());
//     otc.setImagePublicId(otcDTO.getImagePublicId());
//     otc.setCategory(otcDTO.getCategory());
//     otc.setDosage(otcDTO.getDosage());
//     otc.setManufacturer(otcDTO.getManufacturer());
//     otc.setPackSize(otcDTO.getPackSize());
//     otc.setPharmacy(pharmacy);
//     otc.setAddedToStore(true);
//     otc.setStatus(calculateStatus(otcDTO.getStock()));

//     Otc savedOtc = otcRepository.save(otc);
//     return mapToOtcDTO(savedOtc);
// }

//     @Override
//     public List<OtcDTO> getOtcsByPharmacy(Long pharmacyId) {
//         return otcRepository.findByPharmacyId(pharmacyId).stream()
//                 .map(this::mapToOtcDTO)
//                 .collect(Collectors.toList());
//     }

//     @Override
//     public OtcDTO getOtcById(Long otcId) {
//         Otc otc = otcRepository.findById(otcId)
//                 .orElseThrow(() -> new EntityNotFoundException("OTC item not found with id: " + otcId));
//         return mapToOtcDTO(otc);
//     }

//     @Override
//     public List<OtcDTO> getAllOtcs() {
//         return otcRepository.findAll().stream()
//                 .map(this::mapToOtcDTO)
//                 .collect(Collectors.toList());
//     }

// @Override
// public OtcDTO updateOtc(Long otcId, OtcDTO updatedOtcDto) {
//     Otc existingOtc = otcRepository.findById(otcId)
//             .orElseThrow(() -> new EntityNotFoundException("OTC item not found with id: " + otcId));

//     existingOtc.setName(updatedOtcDto.getName());
//     existingOtc.setDescription(updatedOtcDto.getDescription());
//     existingOtc.setPrice(updatedOtcDto.getPrice());
//     existingOtc.setStock(updatedOtcDto.getStock());
//     existingOtc.setCategory(updatedOtcDto.getCategory());
//     existingOtc.setDosage(updatedOtcDto.getDosage());
//     existingOtc.setManufacturer(updatedOtcDto.getManufacturer());
//     existingOtc.setPackSize(updatedOtcDto.getPackSize());
    
//     if (updatedOtcDto.getImageUrl() != null) {
//         existingOtc.setImageUrl(updatedOtcDto.getImageUrl());
//         existingOtc.setImagePublicId(updatedOtcDto.getImagePublicId());
//     }

//     existingOtc.setStatus(calculateStatus(updatedOtcDto.getStock()));

//     Otc updatedOtc = otcRepository.save(existingOtc);
//     return mapToOtcDTO(updatedOtc);
// }

//     @Override
//     public void deleteOtc(Long otcId) {
//         if (!otcRepository.existsById(otcId)) {
//             throw new EntityNotFoundException("OTC item not found with id: " + otcId);
//         }
//         otcRepository.deleteById(otcId);
//     }

// private OtcDTO mapToOtcDTO(Otc otc) {
//     OtcDTO otcDto = new OtcDTO();
//     otcDto.setId(otc.getId());
//     otcDto.setName(otc.getName());
//     otcDto.setDescription(otc.getDescription());
//     otcDto.setPrice(otc.getPrice());
//     otcDto.setStock(otc.getStock());
//     otcDto.setImageUrl(otc.getImageUrl());
//     otcDto.setImagePublicId(otc.getImagePublicId());
//     otcDto.setStatus(otc.getStatus());
//     otcDto.setPharmacyId(otc.getPharmacyId());
//     otcDto.setCategory(otc.getCategory());
//     otcDto.setDosage(otc.getDosage());
//     otcDto.setManufacturer(otc.getManufacturer());
//     otcDto.setPackSize(otc.getPackSize());
//     return otcDto;
// }

//     private String calculateStatus(Integer stock) {
//         if (stock == 0) {
//             return "Out of Stock";
//         } else if (stock <= 10) {
//             return "Low Stock";
//         } else {
//             return "In Stock";
//         }
//     }
// }











