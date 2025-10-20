package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.PrescriptionDTO;
import com.leo.pillpathbackend.dto.PrescriptionItemDTO;
import com.leo.pillpathbackend.dto.PrescriptionListItemDTO;
import com.leo.pillpathbackend.dto.activity.PrescriptionActivityListResponse;
import com.leo.pillpathbackend.dto.request.CreatePrescriptionRequest;
import com.leo.pillpathbackend.dto.PharmacistQueueItemDTO;
import com.leo.pillpathbackend.dto.reroute.RerouteCandidatesResponse;
import com.leo.pillpathbackend.dto.reroute.RerouteRequest;
import com.leo.pillpathbackend.dto.reroute.RerouteResponse;
import com.leo.pillpathbackend.entity.enums.PrescriptionStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PrescriptionService {
    PrescriptionDTO uploadPrescription(Long customerId, MultipartFile file, CreatePrescriptionRequest request) throws IOException;

    List<PrescriptionListItemDTO> getCustomerPrescriptions(Long customerId);

    List<PrescriptionListItemDTO> getFamilyMemberPrescriptions(Long customerId, Long familyMemberId);

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

    // Pharmacist: update submission status directly from queue
    void updateSubmissionStatus(Long pharmacistId, Long submissionId, PrescriptionStatus status);

    // Pharmacist: delete a submission (only if no active order references it)
    void deleteSubmission(Long pharmacistId, Long submissionId);

    // Customer: assign prescription to a family member
    void assignPrescriptionToFamilyMember(Long prescriptionId, Long customerId, Long familyMemberId);


    // Reroute: list candidate pharmacies
    RerouteCandidatesResponse listRerouteCandidates(Long customerId,
                                                    Long prescriptionId,
                                                    Long excludePharmacyId,
                                                    Double lat,
                                                    Double lng,
                                                    Double radiusKm,
                                                    Integer limit,
                                                    Integer offset);

    // Reroute: create new submissions per target pharmacy with unavailable items
    RerouteResponse rerouteUnavailableItems(Long customerId,
                                            Long prescriptionId,
                                            RerouteRequest request,
                                            String idempotencyKey);
}
