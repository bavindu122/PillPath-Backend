package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.PrescriptionDTO;
import com.leo.pillpathbackend.dto.PrescriptionItemDTO;
import com.leo.pillpathbackend.dto.PrescriptionListItemDTO;
import com.leo.pillpathbackend.dto.activity.*;
import com.leo.pillpathbackend.dto.request.CreatePrescriptionRequest;
import com.leo.pillpathbackend.entity.*;
import com.leo.pillpathbackend.entity.enums.PrescriptionStatus;
import com.leo.pillpathbackend.repository.CustomerRepository;
import com.leo.pillpathbackend.repository.PharmacyRepository;
import com.leo.pillpathbackend.repository.PrescriptionRepository;
import com.leo.pillpathbackend.repository.PrescriptionSubmissionRepository;
import com.leo.pillpathbackend.repository.UserRepository;
import com.leo.pillpathbackend.service.CloudinaryService;
import com.leo.pillpathbackend.service.PrescriptionService;
import com.leo.pillpathbackend.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final CustomerRepository customerRepository;
    private final PharmacyRepository pharmacyRepository;
    private final CloudinaryService cloudinaryService;
    private final Mapper mapper;
    private final PrescriptionSubmissionRepository prescriptionSubmissionRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter ISO_SECOND_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public PrescriptionDTO uploadPrescription(Long customerId, MultipartFile file, CreatePrescriptionRequest request) throws IOException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Determine target pharmacies (multi support)
        List<Long> targetPharmacyIds;
        if (request.getPharmacyIds() != null && !request.getPharmacyIds().isEmpty()) {
            targetPharmacyIds = request.getPharmacyIds();
        } else if (request.getPharmacyId() != null) {
            targetPharmacyIds = List.of(request.getPharmacyId());
        } else {
            throw new IllegalArgumentException("At least one pharmacyId is required");
        }

        // Fetch first pharmacy for legacy association
        Pharmacy primaryPharmacy = pharmacyRepository.findById(targetPharmacyIds.get(0))
                .orElseThrow(() -> new IllegalArgumentException("Primary pharmacy not found"));

        Map<String, Object> upload = cloudinaryService.uploadPrescriptionImage(file, customerId, primaryPharmacy.getId());
        String imageUrl = Objects.toString(upload.get("secure_url"), null);
        String publicId = Objects.toString(upload.get("public_id"), null);

        Prescription p = Prescription.builder()
                .code(generateCode())
                .customer(customer)
                .pharmacy(primaryPharmacy) // legacy single link (will deprecate)
                .status(PrescriptionStatus.PENDING_REVIEW)
                .imageUrl(imageUrl)
                .imagePublicId(publicId)
                .note(request.getNote())
                .deliveryPreference(request.getDeliveryPreference())
                .deliveryAddress(request.getDeliveryAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .totalPrice(BigDecimal.ZERO)
                .build();

        Prescription saved = prescriptionRepository.save(p);

        // Create submissions per pharmacy
        for (Long pid : targetPharmacyIds) {
            Pharmacy pharmacy = (pid.equals(primaryPharmacy.getId())) ? primaryPharmacy :
                    pharmacyRepository.findById(pid).orElseThrow(() -> new IllegalArgumentException("Pharmacy not found: " + pid));
            PrescriptionSubmission submission = PrescriptionSubmission.builder()
                    .prescription(saved)
                    .pharmacy(pharmacy)
                    .status(PrescriptionStatus.PENDING_REVIEW)
                    .totalPrice(null)
                    .build();
            prescriptionSubmissionRepository.save(submission);
        }

        return mapper.toPrescriptionDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionListItemDTO> getCustomerPrescriptions(Long customerId) {
        return prescriptionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(mapper::toPrescriptionListItemDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionListItemDTO> getPharmacyPrescriptions(Long pharmacyId) {
        return prescriptionRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacyId)
                .stream().map(mapper::toPrescriptionListItemDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionDTO getCustomerPrescription(Long id, Long customerId) {
        Prescription p = prescriptionRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new NoSuchElementException("Prescription not found"));
        return mapper.toPrescriptionDTO(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionDTO getPharmacyPrescription(Long id, Long pharmacyId) {
        Prescription p = prescriptionRepository.findByIdAndPharmacyId(id, pharmacyId)
                .orElseThrow(() -> new NoSuchElementException("Prescription not found"));
        return mapper.toPrescriptionDTO(p);
    }

    @Override
    public PrescriptionDTO replaceItemsForPharmacy(Long pharmacyId, Long prescriptionId, List<PrescriptionItemDTO> items) {
        Prescription p = prescriptionRepository.findByIdAndPharmacyId(prescriptionId, pharmacyId)
                .orElseThrow(() -> new NoSuchElementException("Prescription not found"));

        // Clear and replace items
        p.getItems().clear();
        BigDecimal total = BigDecimal.ZERO;
        for (PrescriptionItemDTO itemDTO : items) {
            PrescriptionItem item = mapper.toPrescriptionItemEntity(itemDTO);
            item.setPrescription(p);
            p.getItems().add(item);
            if (item.getTotalPrice() != null) {
                total = total.add(item.getTotalPrice());
            } else if (item.getUnitPrice() != null && item.getQuantity() != null) {
                total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        p.setTotalPrice(total);
        p.setStatus(PrescriptionStatus.IN_PROGRESS);
        p.setUpdatedAt(LocalDateTime.now());

        Prescription saved = prescriptionRepository.save(p);
        return mapper.toPrescriptionDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionActivityListResponse getCustomerActivities(Long customerId, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        Page<Prescription> pPage = prescriptionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, PageRequest.of(page, size));

        List<Long> prescriptionIds = pPage.getContent().stream().map(Prescription::getId).toList();
        List<PrescriptionSubmission> submissions = prescriptionIds.isEmpty() ? List.of() :
                prescriptionSubmissionRepository.findWithPharmacyByPrescriptionIdIn(prescriptionIds);

        // group submissions by prescription id
        java.util.Map<Long, List<PrescriptionSubmission>> byPrescription = submissions.stream()
                .collect(java.util.stream.Collectors.groupingBy(s -> s.getPrescription().getId()));

        List<PrescriptionActivityDTO> items = pPage.getContent().stream()
                .map(p -> {
                    List<PrescriptionSubmission> subs = byPrescription.getOrDefault(p.getId(), List.of());
                    // Only keep pending review for now
                    List<PharmacyActivityDTO> pharmacyActivities = subs.stream()
                            .filter(s -> s.getStatus() == PrescriptionStatus.PENDING_REVIEW)
                            .map(s -> PharmacyActivityDTO.builder()
                                    .pharmacyId(String.valueOf(s.getPharmacy().getId()))
                                    .pharmacyName(s.getPharmacy().getName())
                                    .address(s.getPharmacy().getAddress())
                                    .status(ActivityStatus.PENDING_REVIEW)
                                    .actions(PharmacyActivityDTO.Actions.builder()
                                            .canViewOrderPreview(false)
                                            .canProceedToPayment(false)
                                            .build())
                                    .build())
                            .toList();
                    if (pharmacyActivities.isEmpty()) {
                        return null; // skip if no pending submissions right now
                    }
                    return PrescriptionActivityDTO.builder()
                            .code(p.getCode() != null ? p.getCode() : String.valueOf(p.getId()))
                            .uploadedAt(p.getCreatedAt() != null ? p.getCreatedAt().format(ISO_SECOND_FORMAT) : null)
                            .imageUrl(p.getImageUrl())
                            .pharmacies(pharmacyActivities)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        return PrescriptionActivityListResponse.builder()
                .items(items)
                .page(PrescriptionActivityListResponse.PageMeta.builder()
                        .page(page)
                        .size(size)
                        .total(items.size())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.leo.pillpathbackend.dto.PharmacistQueueItemDTO> getPharmacistQueue(Long pharmacistId, PrescriptionStatus status) {
        User pharmacist = userRepository.findById(pharmacistId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacist not found"));
        if (!(pharmacist instanceof PharmacistUser phUser)) {
            throw new IllegalArgumentException("User is not a pharmacist");
        }
        Pharmacy pharmacy = phUser.getPharmacy();
        if (pharmacy == null) {
            throw new IllegalArgumentException("Pharmacist not assigned to a pharmacy");
        }
        Long pharmacyId = pharmacy.getId();
        PrescriptionStatus effectiveStatus = (status == null) ? PrescriptionStatus.PENDING_REVIEW : status;
        List<PrescriptionSubmission> submissions;
        if (effectiveStatus == PrescriptionStatus.PENDING_REVIEW) {
            submissions = prescriptionSubmissionRepository
                    .findByPharmacyIdAndStatusAndAssignedPharmacistIsNullOrderByCreatedAtAsc(pharmacyId, PrescriptionStatus.PENDING_REVIEW);
        } else if (effectiveStatus == PrescriptionStatus.IN_PROGRESS) {
            submissions = prescriptionSubmissionRepository
                    .findByPharmacyIdAndAssignedPharmacistIdAndStatusOrderByCreatedAtAsc(pharmacyId, pharmacistId, PrescriptionStatus.IN_PROGRESS);
        } else {
            // For now only allow the two statuses
            submissions = List.of();
        }
        return submissions.stream().map(this::toQueueDTO).toList();
    }

    @Override
    public com.leo.pillpathbackend.dto.PharmacistQueueItemDTO claimSubmission(Long pharmacistId, Long submissionId) {
        User pharmacist = userRepository.findById(pharmacistId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacist not found"));
        if (!(pharmacist instanceof PharmacistUser phUser)) {
            throw new IllegalArgumentException("User is not a pharmacist");
        }
        Pharmacy pharmacy = phUser.getPharmacy();
        if (pharmacy == null) {
            throw new IllegalArgumentException("Pharmacist not assigned to a pharmacy");
        }
        PrescriptionSubmission submission = prescriptionSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        if (!submission.getPharmacy().getId().equals(pharmacy.getId())) {
            throw new IllegalArgumentException("Submission does not belong to pharmacist's pharmacy");
        }
        // Attempt atomic claim
        int updated = prescriptionSubmissionRepository.claim(submissionId, pharmacistId);
        if (updated == 0) {
            // Refresh entity to reveal current state
            submission = prescriptionSubmissionRepository.findById(submissionId)
                    .orElseThrow(() -> new IllegalArgumentException("Submission not found after claim attempt"));
            if (submission.getAssignedPharmacist() != null) {
                throw new IllegalStateException("Already claimed");
            }
            throw new IllegalStateException("Cannot claim submission (invalid status) ");
        }
        // Reload updated submission
        submission = prescriptionSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found after update"));
        return toQueueDTO(submission);
    }

    private com.leo.pillpathbackend.dto.PharmacistQueueItemDTO toQueueDTO(PrescriptionSubmission s) {
        Prescription prescription = s.getPrescription();
        return com.leo.pillpathbackend.dto.PharmacistQueueItemDTO.builder()
                .submissionId(s.getId())
                .prescriptionCode(prescription.getCode())
                .uploadedAt(prescription.getCreatedAt() != null ? prescription.getCreatedAt().format(ISO_SECOND_FORMAT) : null)
                .status(s.getStatus())
                .imageUrl(prescription.getImageUrl())
                .note(prescription.getNote())
                .claimed(s.getAssignedPharmacist() != null)
                .assignedPharmacistId(s.getAssignedPharmacist() != null ? s.getAssignedPharmacist().getId() : null)
                .build();
    }

    private String generateCode() {
        return "RX-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
