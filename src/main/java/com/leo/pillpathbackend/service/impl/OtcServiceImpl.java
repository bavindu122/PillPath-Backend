//package com.leo.pillpathbackend.service.impl;
//
//public class OtcServiceImpl {
//}


package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.OtcDTO;
import com.leo.pillpathbackend.entity.Otc;
import com.leo.pillpathbackend.repository.OtcRepository;
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

    @Override
    public OtcDTO createOtc(OtcDTO otcDTO) {
        Otc otc = new Otc();
        otc.setName(otcDTO.getName());
        otc.setDescription(otcDTO.getDescription());
        otc.setPrice(otcDTO.getPrice());
        otc.setStock(otcDTO.getStock());
        otc.setImageUrl(otcDTO.getImageUrl());
        otc.setAddedToStore(true);
        otc.setStatus(calculateStatus(otcDTO.getStock()));

        Otc savedOtc = otcRepository.save(otc);
        return mapToOtcDTO(savedOtc);
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
        existingOtc.setImageUrl(updatedOtcDto.getImageUrl());
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