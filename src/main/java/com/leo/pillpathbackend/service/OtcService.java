package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.OtcDTO;
import com.leo.pillpathbackend.dto.OtcStockAlertDTO;
import com.leo.pillpathbackend.dto.PharmacyWithProductDTO;

import java.util.List;
import java.util.Map;

public interface OtcService {

    OtcDTO createOtcForPharmacy(Long pharmacyId, OtcDTO otcDTO);

    OtcDTO getOtcById(Long otcId);

    List<OtcDTO> getAllOtcs();

    List<OtcDTO> getOtcsByPharmacy(Long pharmacyId);

    OtcDTO updateOtc(Long otcId, OtcDTO otcDTO);

    void deleteOtc(Long otcId);

    List<PharmacyWithProductDTO> getPharmaciesByProductName(String productName);

    
    // Stock alert methods
    List<OtcStockAlertDTO> getStockAlerts(Long pharmacyId);

    Map<String, Object> getStockStatistics(Long pharmacyId);
}



