package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.PrescriptionDTO;
import com.leo.pillpathbackend.dto.PrescriptionItemDTO;
import com.leo.pillpathbackend.dto.PrescriptionListItemDTO;
import com.leo.pillpathbackend.dto.activity.PrescriptionActivityListResponse;
import com.leo.pillpathbackend.dto.request.CreatePrescriptionRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PrescriptionService {
    PrescriptionDTO uploadPrescription(Long customerId, MultipartFile file, CreatePrescriptionRequest request) throws IOException;

    List<PrescriptionListItemDTO> getCustomerPrescriptions(Long customerId);

    List<PrescriptionListItemDTO> getPharmacyPrescriptions(Long pharmacyId);

    PrescriptionDTO getCustomerPrescription(Long id, Long customerId);

    PrescriptionDTO getPharmacyPrescription(Long id, Long pharmacyId);

    PrescriptionDTO replaceItemsForPharmacy(Long pharmacyId, Long prescriptionId, List<PrescriptionItemDTO> items);

    PrescriptionActivityListResponse getCustomerActivities(Long customerId, int page, int size);
}
