package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.PrescriptionDTO;
import com.leo.pillpathbackend.dto.PrescriptionItemDTO;
import com.leo.pillpathbackend.dto.PrescriptionListItemDTO;
import com.leo.pillpathbackend.dto.activity.*;
import com.leo.pillpathbackend.dto.request.CreatePrescriptionRequest;
import com.leo.pillpathbackend.dto.PharmacistSubmissionItemsDTO;
import com.leo.pillpathbackend.entity.*;
import com.leo.pillpathbackend.entity.enums.PrescriptionStatus;
import com.leo.pillpathbackend.repository.*;
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
    private final PrescriptionSubmissionItemRepository prescriptionSubmissionItemRepository;

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

                    List<PharmacyActivityDTO> pharmacyActivities = subs.stream()
                            .map(s -> {
                                // Build actions based on status and presence of items
                                boolean hasItems = s.getItems() != null && !s.getItems().isEmpty();
                                ActivityStatus actStatus = toActivityStatus(s.getStatus());
                                PharmacyActivityDTO.PharmacyActivityDTOBuilder builder = PharmacyActivityDTO.builder()
                                        .pharmacyId(String.valueOf(s.getPharmacy().getId()))
                                        .pharmacyName(s.getPharmacy().getName())
                                        .address(s.getPharmacy().getAddress())
                                        .status(actStatus)
                                        .actions(PharmacyActivityDTO.Actions.builder()
                                                .canViewOrderPreview(hasItems)
                                                .canProceedToPayment(false)
                                                .build());
                                if (hasItems) {
                                    // Map items for customer view
                                    java.util.List<MedicationSummaryDTO> meds = s.getItems().stream().map(it -> MedicationSummaryDTO.builder()
                                            .medicationId(String.valueOf(it.getId()))
                                            .name(it.getMedicineName())
                                            .strength(it.getDosage())
                                            .quantity(it.getQuantity())
                                            .price(it.getTotalPrice() != null ? it.getTotalPrice() : (it.getUnitPrice() != null && it.getQuantity() != null ? it.getUnitPrice().multiply(java.math.BigDecimal.valueOf(it.getQuantity())) : null))
                                            .available(it.getAvailable())
                                            .notes(it.getNotes())
                                            .build()).toList();
                                    java.math.BigDecimal subtotal = meds.stream()
                                            .map(MedicationSummaryDTO::getPrice)
                                            .filter(java.util.Objects::nonNull)
                                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                                    TotalsDTO totals = TotalsDTO.builder()
                                            .subtotal(subtotal)
                                            .discount(java.math.BigDecimal.ZERO)
                                            .tax(java.math.BigDecimal.ZERO)
                                            .shipping(java.math.BigDecimal.ZERO)
                                            .total(subtotal)
                                            .currency(com.leo.pillpathbackend.dto.activity.Currency.LKR)
                                            .build();
                                    builder.medications(meds).totals(totals);
                                }
                                return builder.build();
                            })
                            .toList();

                    if (pharmacyActivities.isEmpty()) {
                        return null;
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

    private ActivityStatus toActivityStatus(PrescriptionStatus status) {
        if (status == null) return ActivityStatus.PENDING_REVIEW;
        try {
            return ActivityStatus.valueOf(status.name());
        } catch (Exception e) {
            return ActivityStatus.PENDING_REVIEW;
        }
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

        if (status == null) {
            List<PrescriptionSubmission> pending = prescriptionSubmissionRepository
                    .findByPharmacyIdAndStatusAndAssignedPharmacistIsNullOrderByCreatedAtAsc(pharmacyId, PrescriptionStatus.PENDING_REVIEW);
            List<PrescriptionSubmission> inProgress = prescriptionSubmissionRepository
                    .findByPharmacyIdAndAssignedPharmacistIdAndStatusOrderByCreatedAtAsc(pharmacyId, pharmacistId, PrescriptionStatus.IN_PROGRESS);
            java.util.ArrayList<PrescriptionSubmission> combined = new java.util.ArrayList<>();
            combined.addAll(pending);
            combined.addAll(inProgress);
            return combined.stream().map(this::toQueueDTO).toList();
        }

        List<PrescriptionSubmission> submissions;
        if (status == PrescriptionStatus.PENDING_REVIEW) {
            submissions = prescriptionSubmissionRepository
                    .findByPharmacyIdAndStatusAndAssignedPharmacistIsNullOrderByCreatedAtAsc(pharmacyId, PrescriptionStatus.PENDING_REVIEW);
        } else if (status == PrescriptionStatus.IN_PROGRESS) {
            submissions = prescriptionSubmissionRepository
                    .findByPharmacyIdAndAssignedPharmacistIdAndStatusOrderByCreatedAtAsc(pharmacyId, pharmacistId, PrescriptionStatus.IN_PROGRESS);
        } else {
            submissions = java.util.List.of();
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
        int updated = prescriptionSubmissionRepository.claim(submissionId, pharmacistId);
        if (updated == 0) {
            submission = prescriptionSubmissionRepository.findById(submissionId)
                    .orElseThrow(() -> new IllegalArgumentException("Submission not found after claim attempt"));
            if (submission.getAssignedPharmacist() != null) {
                throw new IllegalStateException("Already claimed");
            }
            throw new IllegalStateException("Cannot claim submission (invalid status)");
        }
        submission = prescriptionSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found after update"));
        return toQueueDTO(submission);
    }

    @Override
    public com.leo.pillpathbackend.dto.PharmacistSubmissionItemsDTO getSubmissionItems(Long pharmacistId, Long submissionId) {
        PrescriptionSubmission submission = loadAndAuthorizeSubmission(pharmacistId, submissionId, false);
        return buildSubmissionItemsDTO(submission, pharmacistId);
    }

    @Override
    public com.leo.pillpathbackend.dto.PharmacistSubmissionItemsDTO addSubmissionItem(Long pharmacistId, Long submissionId, com.leo.pillpathbackend.dto.PrescriptionItemDTO itemDTO) {
        PrescriptionSubmission submission = loadAndAuthorizeSubmission(pharmacistId, submissionId, true);
        if (submission.getAssignedPharmacist() == null && submission.getStatus() == com.leo.pillpathbackend.entity.enums.PrescriptionStatus.PENDING_REVIEW) {
            User pharmacist = userRepository.findById(pharmacistId).orElseThrow();
            submission.setAssignedPharmacist(pharmacist);
            submission.setStatus(com.leo.pillpathbackend.entity.enums.PrescriptionStatus.IN_PROGRESS);
        }
        ensureEditable(pharmacistId, submission);
        PrescriptionSubmissionItem item = toSubmissionItemEntity(itemDTO);
        item.setSubmission(submission);
        prescriptionSubmissionItemRepository.save(item);
        submission.getItems().add(item);
        recalcSubmissionTotals(submission);
        prescriptionSubmissionRepository.save(submission);
        return buildSubmissionItemsDTO(submission, pharmacistId);
    }

    @Override
    public com.leo.pillpathbackend.dto.PharmacistSubmissionItemsDTO updateSubmissionItem(Long pharmacistId, Long submissionId, Long itemId, com.leo.pillpathbackend.dto.PrescriptionItemDTO itemDTO) {
        PrescriptionSubmission submission = loadAndAuthorizeSubmission(pharmacistId, submissionId, true);
        ensureEditable(pharmacistId, submission);
        PrescriptionSubmissionItem existing = submission.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Item not found"));
        existing.setMedicineName(itemDTO.getMedicineName());
        existing.setGenericName(itemDTO.getGenericName());
        existing.setDosage(itemDTO.getDosage());
        existing.setQuantity(itemDTO.getQuantity());
        existing.setUnitPrice(itemDTO.getUnitPrice());
        existing.setAvailable(itemDTO.getAvailable());
        existing.setNotes(itemDTO.getNotes());
        if (itemDTO.getTotalPrice() != null) {
            existing.setTotalPrice(itemDTO.getTotalPrice());
        } else if (existing.getUnitPrice() != null && existing.getQuantity() != null) {
            existing.setTotalPrice(existing.getUnitPrice().multiply(java.math.BigDecimal.valueOf(existing.getQuantity())));
        } else {
            existing.setTotalPrice(null);
        }
        recalcSubmissionTotals(submission);
        prescriptionSubmissionRepository.save(submission);
        return buildSubmissionItemsDTO(submission, pharmacistId);
    }

    @Override
    public com.leo.pillpathbackend.dto.PharmacistSubmissionItemsDTO removeSubmissionItem(Long pharmacistId, Long submissionId, Long itemId) {
        PrescriptionSubmission submission = loadAndAuthorizeSubmission(pharmacistId, submissionId, true);
        ensureEditable(pharmacistId, submission);
        boolean removed = submission.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) throw new IllegalArgumentException("Item not found");
        recalcSubmissionTotals(submission);
        prescriptionSubmissionRepository.save(submission);
        return buildSubmissionItemsDTO(submission, pharmacistId);
    }

    private PrescriptionSubmission loadAndAuthorizeSubmission(Long pharmacistId, Long submissionId, boolean forModification) {
        User pharmacist = userRepository.findById(pharmacistId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacist not found"));
        if (!(pharmacist instanceof PharmacistUser phUser)) {
            throw new IllegalArgumentException("User is not a pharmacist");
        }
        Pharmacy pharmacy = phUser.getPharmacy();
        if (pharmacy == null) throw new IllegalArgumentException("Pharmacist not assigned to a pharmacy");
        PrescriptionSubmission submission = prescriptionSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        if (!submission.getPharmacy().getId().equals(pharmacy.getId())) {
            throw new IllegalArgumentException("Submission does not belong to pharmacist's pharmacy");
        }
        if (forModification) {
            if (submission.getStatus() != com.leo.pillpathbackend.entity.enums.PrescriptionStatus.PENDING_REVIEW && submission.getStatus() != com.leo.pillpathbackend.entity.enums.PrescriptionStatus.IN_PROGRESS) {
                throw new IllegalStateException("Cannot modify submission in current status");
            }
        }
        return submission;
    }

    private void ensureEditable(Long pharmacistId, PrescriptionSubmission submission) {
        if (submission.getAssignedPharmacist() != null && !submission.getAssignedPharmacist().getId().equals(pharmacistId)) {
            throw new IllegalStateException("Submission already claimed by another pharmacist");
        }
    }

    private void recalcSubmissionTotals(PrescriptionSubmission submission) {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (PrescriptionSubmissionItem it : submission.getItems()) {
            if (it.getTotalPrice() != null) {
                total = total.add(it.getTotalPrice());
            } else if (it.getUnitPrice() != null && it.getQuantity() != null) {
                java.math.BigDecimal line = it.getUnitPrice().multiply(java.math.BigDecimal.valueOf(it.getQuantity()));
                it.setTotalPrice(line);
                total = total.add(line);
            }
        }
        submission.setTotalPrice(total.compareTo(java.math.BigDecimal.ZERO) > 0 ? total : null);
    }

    private com.leo.pillpathbackend.dto.PharmacistSubmissionItemsDTO buildSubmissionItemsDTO(PrescriptionSubmission submission, Long pharmacistId) {
        boolean editable = (submission.getStatus() == com.leo.pillpathbackend.entity.enums.PrescriptionStatus.PENDING_REVIEW || submission.getStatus() == com.leo.pillpathbackend.entity.enums.PrescriptionStatus.IN_PROGRESS) &&
                (submission.getAssignedPharmacist() == null || submission.getAssignedPharmacist().getId().equals(pharmacistId));
        java.util.List<com.leo.pillpathbackend.dto.PrescriptionItemDTO> items = submission.getItems().stream().map(this::toItemDTO).toList();
        return com.leo.pillpathbackend.dto.PharmacistSubmissionItemsDTO.builder()
                .submissionId(submission.getId())
                .prescriptionCode(submission.getPrescription().getCode())
                .pharmacyId(submission.getPharmacy().getId())
                .status(submission.getStatus())
                .totalPrice(submission.getTotalPrice())
                .editable(editable)
                .items(items)
                .build();
    }

    private PrescriptionSubmissionItem toSubmissionItemEntity(com.leo.pillpathbackend.dto.PrescriptionItemDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Item data required");
        PrescriptionSubmissionItem entity = new PrescriptionSubmissionItem();
        entity.setMedicineName(dto.getMedicineName());
        entity.setGenericName(dto.getGenericName());
        entity.setDosage(dto.getDosage());
        entity.setQuantity(dto.getQuantity());
        entity.setUnitPrice(dto.getUnitPrice());
        entity.setAvailable(dto.getAvailable());
        entity.setNotes(dto.getNotes());
        if (dto.getTotalPrice() != null) {
            entity.setTotalPrice(dto.getTotalPrice());
        } else if (dto.getUnitPrice() != null && dto.getQuantity() != null) {
            entity.setTotalPrice(dto.getUnitPrice().multiply(java.math.BigDecimal.valueOf(dto.getQuantity())));
        }
        return entity;
    }

    private com.leo.pillpathbackend.dto.PrescriptionItemDTO toItemDTO(PrescriptionSubmissionItem entity) {
        return com.leo.pillpathbackend.dto.PrescriptionItemDTO.builder()
                .id(entity.getId())
                .medicineName(entity.getMedicineName())
                .genericName(entity.getGenericName())
                .dosage(entity.getDosage())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .totalPrice(entity.getTotalPrice())
                .available(entity.getAvailable())
                .notes(entity.getNotes())
                .build();
    }

    private com.leo.pillpathbackend.dto.PharmacistQueueItemDTO toQueueDTO(PrescriptionSubmission s) {
        Prescription prescription = s.getPrescription();
        return com.leo.pillpathbackend.dto.PharmacistQueueItemDTO.builder()
                .submissionId(s.getId())
                .prescriptionCode(prescription != null ? prescription.getCode() : null)
                .uploadedAt(prescription != null && prescription.getCreatedAt() != null ? prescription.getCreatedAt().format(ISO_SECOND_FORMAT) : null)
                .status(s.getStatus())
                .imageUrl(prescription != null ? prescription.getImageUrl() : null)
                .note(prescription != null ? prescription.getNote() : null)
                .claimed(s.getAssignedPharmacist() != null)
                .assignedPharmacistId(s.getAssignedPharmacist() != null ? s.getAssignedPharmacist().getId() : null)
                .build();
    }

    private String generateCode() {
        return "RX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
