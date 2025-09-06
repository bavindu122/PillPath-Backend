package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.PrescriptionDTO;
import com.leo.pillpathbackend.dto.PrescriptionItemDTO;
import com.leo.pillpathbackend.dto.PrescriptionListItemDTO;
import com.leo.pillpathbackend.dto.activity.PrescriptionActivityListResponse;
import com.leo.pillpathbackend.dto.request.CreatePrescriptionRequest;
import com.leo.pillpathbackend.dto.PharmacistQueueItemDTO;
import com.leo.pillpathbackend.entity.enums.PrescriptionStatus;
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

    // Pharmacist queue (multi-pharmacy submissions)
    List<PharmacistQueueItemDTO> getPharmacistQueue(Long pharmacistId, PrescriptionStatus status);

    PharmacistQueueItemDTO claimSubmission(Long pharmacistId, Long submissionId);

    // Pharmacist per-submission item management (order preview building)
    com.leo.pillpathbackend.dto.PharmacistSubmissionItemsDTO getSubmissionItems(Long pharmacistId, Long submissionId);

    com.leo.pillpathbackend.dto.PharmacistSubmissionItemsDTO addSubmissionItem(Long pharmacistId, Long submissionId, PrescriptionItemDTO itemDTO);

    com.leo.pillpathbackend.dto.PharmacistSubmissionItemsDTO updateSubmissionItem(Long pharmacistId, Long submissionId, Long itemId, PrescriptionItemDTO itemDTO);

    com.leo.pillpathbackend.dto.PharmacistSubmissionItemsDTO removeSubmissionItem(Long pharmacistId, Long submissionId, Long itemId);

    // Customer: fetch a single order preview (submission) by prescription code and pharmacyId
    com.leo.pillpathbackend.dto.orderpreview.OrderPreviewDTO getCustomerOrderPreview(Long customerId, String code, Long pharmacyId);
}
