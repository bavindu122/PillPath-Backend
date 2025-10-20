package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.PrescriptionDTO;
import com.leo.pillpathbackend.dto.PrescriptionItemDTO;
import com.leo.pillpathbackend.dto.PrescriptionListItemDTO;
import com.leo.pillpathbackend.dto.activity.*;
import com.leo.pillpathbackend.dto.request.CreatePrescriptionRequest;
import com.leo.pillpathbackend.dto.PharmacistSubmissionItemsDTO;
import com.leo.pillpathbackend.dto.reroute.RerouteCandidatesResponse;
import com.leo.pillpathbackend.dto.reroute.RerouteRequest;
import com.leo.pillpathbackend.dto.reroute.RerouteResponse;
import com.leo.pillpathbackend.entity.*;
import com.leo.pillpathbackend.entity.enums.PrescriptionStatus;
import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
import com.leo.pillpathbackend.entity.enums.SubmissionSource;
import com.leo.pillpathbackend.repository.*;
import com.leo.pillpathbackend.service.CloudinaryService;
import com.leo.pillpathbackend.service.NotificationService;
import com.leo.pillpathbackend.service.PrescriptionService;
import com.leo.pillpathbackend.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
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
    private final PharmacyOrderRepository pharmacyOrderRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final NotificationService notificationService;

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

        // NOTIFICATION: Notify pharmacists about new prescription (Scenario 1)
        for (Long pid : targetPharmacyIds) {
            Pharmacy pharmacy = pharmacyRepository.findById(pid).orElse(null);
            if (pharmacy != null) {
                // Get all pharmacists for this pharmacy
                List<Long> pharmacistIds = userRepository.findPharmacistIdsByPharmacyId(pid);
                if (!pharmacistIds.isEmpty()) {
                    try {
                        notificationService.createPrescriptionSentNotification(
                            saved.getId(),
                            pid,
                            pharmacistIds,
                            customer.getFullName()
                        );
                    } catch (Exception e) {
                        log.error("Failed to send prescription notification for pharmacy {}: {}", pid, e.getMessage());
                    }
                }
            }
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
    public List<PrescriptionListItemDTO> getFamilyMemberPrescriptions(Long customerId, Long familyMemberId) {
        return prescriptionRepository.findByCustomerIdAndFamilyMemberIdOrderByCreatedAtDesc(customerId, familyMemberId)
                .stream().map(prescription -> {
                    PrescriptionListItemDTO dto = mapper.toPrescriptionListItemDTO(prescription);
                    // Try to find the associated order for navigation
                    customerOrderRepository.findByPrescriptionId(prescription.getId())
                            .ifPresent(order -> dto.setOrderCode(order.getOrderCode()));
                    return dto;
                }).toList();
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
        PrescriptionDTO dto = mapper.toPrescriptionDTO(p);
        
        // Include order information if prescription has been ordered
        customerOrderRepository.findByPrescriptionId(id).ifPresent(order -> {
            dto.setOrderCode(order.getOrderCode());
            
            // Set order totals
            dto.setOrderTotals(mapper.toOrderTotalsDTO(order));
            
            // Set pharmacy orders (the actual fulfilled items from pharmacies)
            if (order.getPharmacyOrders() != null && !order.getPharmacyOrders().isEmpty()) {
                dto.setPharmacyOrders(
                    order.getPharmacyOrders().stream()
                        .map(po -> mapper.toPharmacyOrderDTO(po, true))
                        .toList()
                );
            }
        });
        
        return dto;
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

        // Collect submission ids for order lookup
        List<Long> submissionIds = submissions.stream().map(PrescriptionSubmission::getId).toList();
        Map<Long, PharmacyOrderStatus> submissionOrderStatus;
        Map<Long, String> submissionOrderCode;
        if (submissionIds.isEmpty()) {
            submissionOrderStatus = Map.of();
            submissionOrderCode = Map.of();
        } else {
            List<PharmacyOrder> poList = pharmacyOrderRepository.findBySubmissionIdIn(submissionIds);
            // choose best order per submission: prefer active (not CANCELLED/HANDED_OVER), else latest by createdAt
            java.util.Map<Long, PharmacyOrder> best = new java.util.HashMap<>();
            for (PharmacyOrder po : poList) {
                Long sid = po.getSubmission() != null ? po.getSubmission().getId() : null;
                if (sid == null) continue;
                PharmacyOrder current = best.get(sid);
                boolean poActive = po.getStatus() != PharmacyOrderStatus.CANCELLED && po.getStatus() != PharmacyOrderStatus.HANDED_OVER;
                boolean curActive = current != null && current.getStatus() != PharmacyOrderStatus.CANCELLED && current.getStatus() != PharmacyOrderStatus.HANDED_OVER;
                if (current == null
                        || (poActive && !curActive)
                        || ((poActive == curActive) && (current.getCreatedAt() == null || (po.getCreatedAt() != null && po.getCreatedAt().isAfter(current.getCreatedAt()))))) {
                    best.put(sid, po);
                }
            }
            submissionOrderStatus = best.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, e -> e.getValue().getStatus()));
            submissionOrderCode = best.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, e -> e.getValue().getOrderCode()));
        }

        // group submissions by prescription id
        java.util.Map<Long, List<PrescriptionSubmission>> byPrescription = submissions.stream()
                .collect(java.util.stream.Collectors.groupingBy(s -> s.getPrescription().getId()));

        List<PrescriptionActivityDTO> items = pPage.getContent().stream()
                .map(p -> {
                    List<PrescriptionSubmission> subs = byPrescription.getOrDefault(p.getId(), List.of());

                    List<PharmacyActivityDTO> pharmacyActivities = subs.stream()
                            .map(s -> {
                                boolean hasItems = s.getItems() != null && !s.getItems().isEmpty();
                                ActivityStatus actStatus = toActivityStatus(s.getStatus());
                                PharmacyOrderStatus existingStatus = submissionOrderStatus.get(s.getId());
                                boolean activeOrderExists = existingStatus != null && existingStatus != PharmacyOrderStatus.CANCELLED && existingStatus != PharmacyOrderStatus.HANDED_OVER;
                                PharmacyActivityDTO.PharmacyActivityDTOBuilder builder = PharmacyActivityDTO.builder()
                                        .pharmacyId(String.valueOf(s.getPharmacy().getId()))
                                        .pharmacyName(s.getPharmacy().getName())
                                        .address(s.getPharmacy().getAddress())
                                        .status(actStatus)
                                        .accepted(existingStatus != null)
                                        .orderStatus(existingStatus != null ? existingStatus.name() : null)
                                        .pharmacyOrderCode(submissionOrderCode != null ? submissionOrderCode.get(s.getId()) : null)
                                        .actions(PharmacyActivityDTO.Actions.builder()
                                                .canViewOrderPreview(hasItems)
                                                .canProceedToPayment(hasItems && !activeOrderExists)
                                                .build());
                                if (hasItems) {
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
                            .prescriptionStatus(p.getStatus() != null ? p.getStatus().name() : null)
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

        // Exclude closed statuses from queue
        java.util.List<PrescriptionStatus> exclude = java.util.List.of(
                PrescriptionStatus.COMPLETED,
                PrescriptionStatus.REJECTED,
                PrescriptionStatus.CANCELLED
        );

        List<PrescriptionSubmission> submissions;
        if (status == null) {
            // All submissions except closed: include unassigned + those assigned to this pharmacist
            List<PrescriptionSubmission> unassigned = prescriptionSubmissionRepository.findQueueUnassignedExcluding(pharmacyId, exclude);
            List<PrescriptionSubmission> mine = prescriptionSubmissionRepository.findQueueAssignedToPharmacistExcluding(pharmacyId, pharmacistId, exclude);
            java.util.ArrayList<PrescriptionSubmission> combined = new java.util.ArrayList<>(unassigned);
            combined.addAll(mine);
            submissions = combined;
        } else {
            // If filter is one of excluded, return empty
            if (exclude.contains(status)) {
                submissions = java.util.List.of();
            } else {
                // Show all submissions at this pharmacy with that status
                submissions = prescriptionSubmissionRepository.findByPharmacyIdAndStatusOrderByCreatedAtAsc(pharmacyId, status);
            }
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
        
        // NOTIFICATION: Notify customer when order preview is ready (Scenario 2)
        // Send notification if this is the first item or if submission now has items with total > 0
        if (submission.getTotalPrice() != null && submission.getTotalPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            Prescription prescription = submission.getPrescription();
            if (prescription != null && prescription.getCustomer() != null) {
                try {
                    notificationService.createOrderPreviewReadyNotification(
                        submission.getId(),
                        prescription.getId(),
                        prescription.getCustomer().getId(),
                        submission.getPharmacy().getName()
                    );
                } catch (Exception e) {
                    log.error("Failed to send order preview notification: {}", e.getMessage());
                }
            }
        }
        
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
        String customerName = null;
        if (prescription != null && prescription.getCustomer() != null) {
            customerName = prescription.getCustomer().getFullName();
        }
        return com.leo.pillpathbackend.dto.PharmacistQueueItemDTO.builder()
                .submissionId(s.getId())
                .prescriptionCode(prescription != null ? prescription.getCode() : null)
                .uploadedAt(prescription != null && prescription.getCreatedAt() != null ? prescription.getCreatedAt().format(ISO_SECOND_FORMAT) : null)
                .status(s.getStatus())
                .imageUrl(prescription != null ? prescription.getImageUrl() : null)
                .note(prescription != null ? prescription.getNote() : null)
                .claimed(s.getAssignedPharmacist() != null)
                .assignedPharmacistId(s.getAssignedPharmacist() != null ? s.getAssignedPharmacist().getId() : null)
                .customerName(customerName)
                .build();
    }

    private String generateCode() {
        return "RX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Override
    @Transactional(readOnly = true)
    public com.leo.pillpathbackend.dto.orderpreview.OrderPreviewDTO getCustomerOrderPreview(Long customerId, String code, Long pharmacyId) {
        if (customerId == null || code == null || code.isBlank() || pharmacyId == null) {
            throw new IllegalArgumentException("customerId, code and pharmacyId are required");
        }
        // Repository method expected to join submission + prescription + pharmacy filtered by owner
        java.util.Optional<PrescriptionSubmission> opt = prescriptionSubmissionRepository.findForCustomerPreview(customerId, code, pharmacyId);
        PrescriptionSubmission submission = opt.orElseThrow(() -> new NoSuchElementException("Order preview not found"));
        Prescription prescription = submission.getPrescription();

        java.util.List<com.leo.pillpathbackend.dto.orderpreview.OrderPreviewItemDTO> items = submission.getItems() == null ? java.util.List.of() : submission.getItems().stream()
                .map(it -> com.leo.pillpathbackend.dto.orderpreview.OrderPreviewItemDTO.builder()
                        .id(it.getId())
                        .medicineName(it.getMedicineName())
                        .genericName(it.getGenericName())
                        .dosage(it.getDosage())
                        .quantity(it.getQuantity())
                        .unitPrice(it.getUnitPrice())
                        .totalPrice(it.getTotalPrice() != null ? it.getTotalPrice() : (it.getUnitPrice() != null && it.getQuantity() != null ? it.getUnitPrice().multiply(BigDecimal.valueOf(it.getQuantity())) : null))
                        .available(it.getAvailable())
                        .notes(it.getNotes())
                        .build())
                .toList();

        BigDecimal subtotal = items.stream()
                .map(com.leo.pillpathbackend.dto.orderpreview.OrderPreviewItemDTO::getTotalPrice)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        com.leo.pillpathbackend.dto.orderpreview.OrderPreviewTotalsDTO totals = com.leo.pillpathbackend.dto.orderpreview.OrderPreviewTotalsDTO.builder()
                .subtotal(subtotal)
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .shipping(BigDecimal.ZERO)
                .total(subtotal)
                .currency("LKR")
                .build();

        boolean hasItems = !items.isEmpty();
        com.leo.pillpathbackend.dto.orderpreview.OrderPreviewActionsDTO actions = com.leo.pillpathbackend.dto.orderpreview.OrderPreviewActionsDTO.builder()
                .canViewOrderPreview(hasItems)
                .canProceedToPayment(false)
                .build();

        java.util.List<String> unavailable = submission.getItems() == null ? java.util.List.of() : submission.getItems().stream()
                .filter(i -> Boolean.FALSE.equals(i.getAvailable()))
                .map(i -> i.getMedicineName() != null ? i.getMedicineName() : (i.getGenericName() != null ? i.getGenericName() : "Item"))
                .toList();

        return com.leo.pillpathbackend.dto.orderpreview.OrderPreviewDTO.builder()
                .code(prescription != null ? prescription.getCode() : code)
                .pharmacyId(submission.getPharmacy().getId())
                .pharmacyName(submission.getPharmacy().getName())
                .uploadedAt(prescription != null && prescription.getCreatedAt() != null ? prescription.getCreatedAt().format(ISO_SECOND_FORMAT) : null)
                .imageUrl(prescription != null ? prescription.getImageUrl() : null)
                .status(submission.getStatus())
                .items(items)
                .totals(totals)
                .actions(actions)
                .unavailableItems(unavailable)
                .build();
    }

    @Override
    public void updateSubmissionStatus(Long pharmacistId, Long submissionId, PrescriptionStatus status) {
        if (status == null) throw new IllegalArgumentException("status required");
        PrescriptionSubmission submission = loadAndAuthorizeSubmission(pharmacistId, submissionId, true);
        ensureEditable(pharmacistId, submission);
        // Update status directly
        submission.setStatus(status);
        prescriptionSubmissionRepository.save(submission);
    }

    @Override
    public void deleteSubmission(Long pharmacistId, Long submissionId) {
        PrescriptionSubmission submission = loadAndAuthorizeSubmission(pharmacistId, submissionId, true);
        ensureEditable(pharmacistId, submission);
        // Prevent deletion if tied to an active customer order (PENDING or PAID)
        boolean hasActiveOrder = pharmacyOrderRepository.existsBySubmissionIdAndCustomerOrder_StatusIn(
                submission.getId(), java.util.List.of(
                        com.leo.pillpathbackend.entity.enums.CustomerOrderStatus.PENDING,
                        com.leo.pillpathbackend.entity.enums.CustomerOrderStatus.PAID
                )
        );
        if (hasActiveOrder) {
            throw new IllegalStateException("Cannot delete a submission that is part of an active order");
        }
        prescriptionSubmissionRepository.delete(submission);
    }

    @Override
    public void assignPrescriptionToFamilyMember(Long prescriptionId, Long customerId, Long familyMemberId) {
        // Load the prescription
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found"));
        
        // Verify that the prescription belongs to this customer
        if (!prescription.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("You don't have permission to assign this prescription");
        }
        
        // Update the prescription's family member reference
        prescription.setFamilyMemberId(familyMemberId);
        prescriptionRepository.save(prescription);
        
        log.info("Prescription {} assigned to family member {} by customer {}", 
                prescriptionId, familyMemberId, customerId);
    }
    // New: Reroute candidates
    @Override
    @Transactional(readOnly = true)
    public RerouteCandidatesResponse listRerouteCandidates(Long customerId,
                                                           Long prescriptionId,
                                                           Long excludePharmacyId,
                                                           Double lat,
                                                           Double lng,
                                                           Double radiusKm,
                                                           Integer limit,
                                                           Integer offset) {
        if (customerId == null) throw new IllegalArgumentException("Unauthorized");
        Prescription p = prescriptionRepository.findByIdAndCustomerId(prescriptionId, customerId)
                .orElseThrow(() -> new NoSuchElementException("Prescription not found"));

        double useLat;
        double useLng;
        if (lat != null && lng != null) {
            useLat = lat;
            useLng = lng;
        } else if (p.getLatitude() != null && p.getLongitude() != null) {
            useLat = p.getLatitude().doubleValue();
            useLng = p.getLongitude().doubleValue();
        } else {
            // Fallback: no coordinates, return recent active pharmacies
            List<Pharmacy> all = pharmacyRepository.findTop10ByOrderByCreatedAtDesc();
            List<com.leo.pillpathbackend.dto.reroute.RerouteCandidateDTO> items = all.stream()
                    .filter(ph -> excludePharmacyId == null || !ph.getId().equals(excludePharmacyId))
                    .filter(ph -> Boolean.TRUE.equals(ph.getIsActive()) && Boolean.TRUE.equals(ph.getIsVerified()))
                    .map(ph -> com.leo.pillpathbackend.dto.reroute.RerouteCandidateDTO.builder()
                            .pharmacyId(ph.getId())
                            .name(ph.getName())
                            .address(ph.getAddress())
                            .distanceKm(null)
                            .rating(ph.getAverageRating())
                            .deliveryEtaMinutes("30-45")
                            .acceptingReroute(Boolean.TRUE)
                            .build())
                    .toList();
            return RerouteCandidatesResponse.builder().items(paginate(items, limit, offset)).build();
        }

        double rad = radiusKm != null && radiusKm > 0 ? radiusKm : 10.0; // default 10km
        List<Pharmacy> nearby = pharmacyRepository.findActivePharmaciesWithinRadius(useLat, useLng, rad);
        List<com.leo.pillpathbackend.dto.reroute.RerouteCandidateDTO> items = nearby.stream()
                .filter(ph -> excludePharmacyId == null || !ph.getId().equals(excludePharmacyId))
                .map(ph -> {
                    Double dist = null;
                    if (ph.getLatitude() != null && ph.getLongitude() != null) {
                        dist = haversineKm(useLat, useLng, ph.getLatitude().doubleValue(), ph.getLongitude().doubleValue());
                    }
                    return com.leo.pillpathbackend.dto.reroute.RerouteCandidateDTO.builder()
                            .pharmacyId(ph.getId())
                            .name(ph.getName())
                            .address(ph.getAddress())
                            .distanceKm(dist)
                            .rating(ph.getAverageRating())
                            .deliveryEtaMinutes(etaFromDistance(dist))
                            .acceptingReroute(Boolean.TRUE)
                            .build();
                })
                .sorted(Comparator.comparing(d -> Optional.ofNullable(d.getDistanceKm()).orElse(Double.MAX_VALUE)))
                .toList();

        return RerouteCandidatesResponse.builder().items(paginate(items, limit, offset)).build();
    }

    private <T> List<T> paginate(List<T> list, Integer limit, Integer offset) {
        int off = offset != null && offset > 0 ? offset : 0;
        int lim = limit != null && limit > 0 ? Math.min(limit, 100) : 20;
        if (off >= list.size()) return List.of();
        int toIndex = Math.min(off + lim, list.size());
        return list.subList(off, toIndex);
    }

    private String etaFromDistance(Double distKm) {
        if (distKm == null) return "30-45";
        if (distKm < 2) return "20-30";
        if (distKm < 5) return "30-45";
        return "45-60";
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // New: Reroute create
    @Override
    public RerouteResponse rerouteUnavailableItems(Long customerId, Long prescriptionId, RerouteRequest request, String idempotencyKey) {
        if (customerId == null) throw new IllegalArgumentException("Unauthorized");
        if (request == null) throw new IllegalArgumentException("Request required");
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("items cannot be empty");
        }
        // Resolve target IDs
        List<Long> targets = new ArrayList<>();
        if (request.getTargetPharmacyIds() != null) targets.addAll(request.getTargetPharmacyIds());
        if (request.getTargetPharmacyId() != null) targets.add(request.getTargetPharmacyId());
        // sanitize
        targets.removeIf(Objects::isNull);
        targets = new ArrayList<>(new LinkedHashSet<>(targets));
        if (targets.isEmpty()) throw new IllegalArgumentException("targetPharmacyId(s) required");
        if (request.getOriginalPharmacyId() != null && targets.contains(request.getOriginalPharmacyId())) {
            throw new IllegalArgumentException("target cannot include originalPharmacyId");
        }

        Prescription p = prescriptionRepository.findByIdAndCustomerId(prescriptionId, customerId)
                .orElseThrow(() -> new NoSuchElementException("Prescription not found"));

        String rrId = "RR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        List<RerouteResponse.Created> created = new ArrayList<>();
        List<RerouteResponse.Skipped> skipped = new ArrayList<>();

        for (Long pid : targets) {
            Optional<Pharmacy> phOpt = pharmacyRepository.findById(pid);
            if (phOpt.isEmpty()) {
                skipped.add(RerouteResponse.Skipped.builder().pharmacyId(pid).reason("NOT_FOUND").build());
                continue;
            }
            Pharmacy ph = phOpt.get();
            if (!Boolean.TRUE.equals(ph.getIsActive())) {
                skipped.add(RerouteResponse.Skipped.builder().pharmacyId(pid).reason("OUT_OF_SERVICE").build());
                continue;
            }
            // duplicate submission check
            Optional<PrescriptionSubmission> existing = prescriptionSubmissionRepository.findByPrescriptionIdAndPharmacyId(p.getId(), pid);
            if (existing.isPresent()) {
                skipped.add(RerouteResponse.Skipped.builder().pharmacyId(pid).reason("DUPLICATE").build());
                continue;
            }

            PrescriptionSubmission submission = PrescriptionSubmission.builder()
                    .prescription(p)
                    .pharmacy(ph)
                    .status(PrescriptionStatus.PENDING_REVIEW)
                    .source(SubmissionSource.REROUTE)
                    .parentPreviewId(request.getParentPreviewId())
                    .originalPharmacyId(request.getOriginalPharmacyId())
                    .note(request.getNote())
                    .build();
            submission = prescriptionSubmissionRepository.save(submission);

            for (RerouteRequest.RerouteItemRequest it : request.getItems()) {
                PrescriptionSubmissionItem entity = new PrescriptionSubmissionItem();
                entity.setSubmission(submission);
                entity.setMedicineName(it.getName());
                entity.setGenericName(it.getGenericName());
                entity.setDosage(it.getDosage());
                entity.setQuantity(it.getQuantity() != null ? it.getQuantity() : 1);
                entity.setAvailable(null); // unknown yet
                entity.setNotes(it.getNotes());
                prescriptionSubmissionItemRepository.save(entity);
                submission.getItems().add(entity);
            }
            recalcSubmissionTotals(submission);
            prescriptionSubmissionRepository.save(submission);

            // notify pharmacists at target pharmacy
            try {
                List<Long> pharmacistIds = userRepository.findPharmacistIdsByPharmacyId(pid);
                if (!pharmacistIds.isEmpty() && p.getCustomer() != null) {
                    notificationService.createPrescriptionSentNotification(
                            p.getId(),
                            pid,
                            pharmacistIds,
                            p.getCustomer().getFullName()
                    );
                }
            } catch (Exception ex) {
                log.error("Failed to send reroute notification for pharmacy {}: {}", pid, ex.getMessage());
            }

            created.add(RerouteResponse.Created.builder()
                    .pharmacyId(pid)
                    .submissionId(submission.getId())
                    .status(submission.getStatus().name())
                    .build());
        }

        return RerouteResponse.builder()
                .rerouteRequestId(rrId)
                .created(created)
                .skipped(skipped)
                .build();
    }
}
