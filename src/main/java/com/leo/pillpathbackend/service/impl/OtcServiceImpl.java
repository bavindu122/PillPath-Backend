package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.OtcDTO;
import com.leo.pillpathbackend.entity.Otc;
import com.leo.pillpathbackend.entity.Pharmacy;
import com.leo.pillpathbackend.repository.OtcRepository;
import com.leo.pillpathbackend.repository.PharmacyRepository;
import com.leo.pillpathbackend.service.OtcService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OtcServiceImpl implements OtcService {

    private final OtcRepository otcRepository;
    private final PharmacyRepository pharmacyRepository;

    @Override
    public OtcDTO createOtcForPharmacy(Long pharmacyId, OtcDTO otcDTO) {
        // Find the pharmacy first to ensure it exists
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy not found with id: " + pharmacyId));

        Otc otc = new Otc();
        otc.setName(otcDTO.getName());
        otc.setDescription(otcDTO.getDescription());
        otc.setPrice(otcDTO.getPrice());
        otc.setStock(otcDTO.getStock());
        otc.setImageUrl(otcDTO.getImageUrl());
        otc.setImagePublicId(otcDTO.getImagePublicId()); // Add this line
        otc.setPharmacy(pharmacy); // Set both pharmacyId and pharmacy relationship
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
        // existingOtc.setImageUrl(updatedOtcDto.getImageUrl());
            // Update image fields if provided
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

    private OtcDTO mapToOtcDTO(Otc otc) {
        OtcDTO otcDto = new OtcDTO();
        otcDto.setId(otc.getId());
        otcDto.setName(otc.getName());
        otcDto.setDescription(otc.getDescription());
        otcDto.setPrice(otc.getPrice());
        otcDto.setStock(otc.getStock());
        otcDto.setImageUrl(otc.getImageUrl());
        otcDto.setImagePublicId(otc.getImagePublicId()); // Add this line
        otcDto.setStatus(otc.getStatus());
        otcDto.setPharmacyId(otc.getPharmacyId());
                return otcDto;
    }

    private String calculateStatus(Integer stock) {
        if (stock == 0) {
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
// import com.leo.pillpathbackend.entity.Otc;
// import com.leo.pillpathbackend.entity.Pharmacy;
// import com.leo.pillpathbackend.repository.OtcRepository;
// import com.leo.pillpathbackend.repository.PharmacyRepository; // Add this import
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
//     private final PharmacyRepository pharmacyRepository; // Inject PharmacyRepository

//     // @Override
//     // public OtcDTO createOtcForPharmacy(Long pharmacyId, OtcDTO otcDTO) {
//     //     // Find the pharmacy first to ensure it exists
//     //     Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
//     //             .orElseThrow(() -> new EntityNotFoundException("Pharmacy not found with id: " + pharmacyId));

//     //     Otc otc = new Otc();
//     //     otc.setName(otcDTO.getName());
//     //     otc.setDescription(otcDTO.getDescription());
//     //     otc.setPrice(otcDTO.getPrice());
//     //     otc.setStock(otcDTO.getStock());
//     //     otc.setImageUrl(otcDTO.getImageUrl());
//     //     otc.setAddedToStore(true);
//     //     otc.setStatus(calculateStatus(otcDTO.getStock()));
//     //     otc.setPharmacy(pharmacy); // Associate the OTC with the pharmacy

//     //     Otc savedOtc = otcRepository.save(otc);
//     //     return mapToOtcDTO(savedOtc);
//     // }

//     @Override
// public List<OtcDTO> getOtcsByPharmacy(Long pharmacyId) {
//     // Assuming you have a pharmacyId field in your Otc entity
//     return otcRepository.findByPharmacyId(pharmacyId).stream()
//             .map(this::mapToOtcDTO)
//             .collect(Collectors.toList());
// }

// @Override
// public OtcDTO createOtcForPharmacy(Long pharmacyId, OtcDTO otcDTO) {
//     Otc otc = new Otc();
//     otc.setName(otcDTO.getName());
//     otc.setDescription(otcDTO.getDescription());
//     otc.setPrice(otcDTO.getPrice());
//     otc.setStock(otcDTO.getStock());
//     otc.setImageUrl(otcDTO.getImageUrl());
//     otc.setPharmacyId(pharmacyId); // Set the pharmacy ID
//     otc.setAddedToStore(true);
//     otc.setStatus(calculateStatus(otcDTO.getStock()));

//     Otc savedOtc = otcRepository.save(otc);
//     return mapToOtcDTO(savedOtc);
    
//     // The previous createOtc method is now replaced by createOtcForPharmacy
//     // The other methods (get, update, delete) will remain the same but will now be implicitly tied to a pharmacy.

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
//         existingOtc.setImageUrl(updatedOtcDto.getImageUrl());
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

//     private OtcDTO mapToOtcDTO(Otc otc) {
//         OtcDTO otcDto = new OtcDTO();
//         otcDto.setId(otc.getId());
//         otcDto.setName(otc.getName());
//         otcDto.setDescription(otc.getDescription());
//         otcDto.setPrice(otc.getPrice());
//         otcDto.setStock(otc.getStock());
//         otcDto.setImageUrl(otc.getImageUrl());
//         otcDto.setStatus(otc.getStatus());
//         return otcDto;
//     }

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






















// package com.leo.pillpathbackend.service.impl;

// import com.leo.pillpathbackend.dto.OtcDTO;
// import com.leo.pillpathbackend.entity.Otc;
// import com.leo.pillpathbackend.repository.OtcRepository;
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

//     @Override
//     public OtcDTO createOtc(OtcDTO otcDTO) {
//         Otc otc = new Otc();
//         otc.setName(otcDTO.getName());
//         otc.setDescription(otcDTO.getDescription());
//         otc.setPrice(otcDTO.getPrice());
//         otc.setStock(otcDTO.getStock());
//         otc.setImageUrl(otcDTO.getImageUrl());
//         otc.setAddedToStore(true);
//         otc.setStatus(calculateStatus(otcDTO.getStock()));

//         Otc savedOtc = otcRepository.save(otc);
//         return mapToOtcDTO(savedOtc);
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
//         existingOtc.setImageUrl(updatedOtcDto.getImageUrl());
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

//     private OtcDTO mapToOtcDTO(Otc otc) {
//         OtcDTO otcDto = new OtcDTO();
//         otcDto.setId(otc.getId());
//         otcDto.setName(otc.getName());
//         otcDto.setDescription(otc.getDescription());
//         otcDto.setPrice(otc.getPrice());
//         otcDto.setStock(otc.getStock());
//         otcDto.setImageUrl(otc.getImageUrl());
//         return otcDto;
//     }

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