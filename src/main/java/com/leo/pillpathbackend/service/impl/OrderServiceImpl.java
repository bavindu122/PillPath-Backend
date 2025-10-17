package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.order.*;
import com.leo.pillpathbackend.entity.*;
import com.leo.pillpathbackend.entity.enums.*;
import com.leo.pillpathbackend.repository.*;
import com.leo.pillpathbackend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionSubmissionRepository submissionRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final PharmacyOrderRepository pharmacyOrderRepository;
    private final UserRepository userRepository;

    @Override
    public CustomerOrderDTO placeOrder(Long customerId, PlaceOrderRequestDTO request) {
        if (request == null) throw new IllegalArgumentException("Request is null");
        if (request.getPrescriptionCode() == null || request.getPrescriptionCode().isBlank()) {
            throw new IllegalArgumentException("prescriptionCode required");
        }
        if (request.getPharmacies() == null || request.getPharmacies().isEmpty()) {
            throw new IllegalArgumentException("At least one pharmacy selection required");
        }

        Prescription prescription = prescriptionRepository.findByCode(request.getPrescriptionCode())
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found"));
        if (prescription.getCustomer() == null || !Objects.equals(prescription.getCustomer().getId(), customerId)) {
            throw new IllegalArgumentException("Prescription does not belong to customer");
        }

        boolean hasActiveOrder = customerOrderRepository.existsByPrescriptionIdAndCustomerIdAndStatusIn(
                prescription.getId(), customerId,
                List.of(CustomerOrderStatus.PENDING, CustomerOrderStatus.PAID)
        );
        if (hasActiveOrder) {
            throw new IllegalStateException("An active order already exists for this prescription");
        }

        Customer cust = resolveCustomer(prescription.getCustomer());

        CustomerOrder order = CustomerOrder.builder()
                .orderCode(generateOrderCode())
                .customer(cust)
                .prescription(prescription)
                .status(CustomerOrderStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .currency("LKR")
                .paymentReference(null)
                .build();
        if (order.getPharmacyOrders() == null) order.setPharmacyOrders(new ArrayList<>());

        // Load all submissions for this prescription and index by pharmacyId
        List<PrescriptionSubmission> allSubs = submissionRepository.findByPrescriptionId(prescription.getId());
        Map<Long, PrescriptionSubmission> subByPharmacy = allSubs.stream()
                .collect(Collectors.toMap(s -> s.getPharmacy().getId(), s -> s));

        BigDecimal overallSubtotal = BigDecimal.ZERO;
        int idx = 0;
        for (PharmacyOrderSelectionDTO sel : request.getPharmacies()) {
            final int selectionIndex = idx++;
            if (sel.getPharmacyId() == null) {
                throw new IllegalArgumentException("Selection[" + selectionIndex + "]: pharmacyId required");
            }
            PrescriptionSubmission submission = subByPharmacy.get(sel.getPharmacyId());
            if (submission == null) {
                throw new IllegalArgumentException("Selection[" + selectionIndex + "]: no submission found for pharmacy " + sel.getPharmacyId());
            }
            if (sel.getItems() == null || sel.getItems().isEmpty()) {
                throw new IllegalArgumentException("Selection[" + selectionIndex + "]: at least one item required");
            }

            Map<Long, PrescriptionSubmissionItem> itemsMap = submission.getItems().stream()
                    .collect(Collectors.toMap(PrescriptionSubmissionItem::getId, i -> i));

            BigDecimal subSubtotal = BigDecimal.ZERO;
            PharmacyOrder pOrder = PharmacyOrder.builder()
                    .customerOrder(order)
                    .pharmacy(submission.getPharmacy())
                    .submission(submission)
                    .status(PharmacyOrderStatus.RECEIVED)
                    // set unique pharmacy-specific order code
                    .orderCode(generatePharmacyOrderCode(submission.getPharmacy().getId()))
                    .pickupCode(generatePickupCode(submission.getPharmacy().getId()))
                    .pickupLocation(submission.getPharmacy().getAddress())
                    .customerNote(sel.getNote())
                    .build();

            List<PharmacyOrderItem> orderItems = new ArrayList<>();
            for (PharmacyOrderItemSelectionDTO itemSel : sel.getItems()) {
                if (itemSel.getSubmissionId() == null) {
                    throw new IllegalArgumentException("Selection[" + selectionIndex + "]: item missing submissionId (item id)");
                }
                PrescriptionSubmissionItem psi = itemsMap.get(itemSel.getSubmissionId());
                if (psi == null) {
                    throw new IllegalArgumentException("Selection[" + selectionIndex + "]: item id " + itemSel.getSubmissionId() + " not part of submission");
                }
                int qty = itemSel.getQuantity() != null && itemSel.getQuantity() > 0
                        ? itemSel.getQuantity()
                        : (psi.getQuantity() != null ? psi.getQuantity() : 0);
                if (qty <= 0) {
                    throw new IllegalArgumentException("Selection[" + selectionIndex + "]: item id " + itemSel.getSubmissionId() + " has zero quantity");
                }
                BigDecimal unit = psi.getUnitPrice() != null ? psi.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(qty));
                subSubtotal = subSubtotal.add(lineTotal);

                PharmacyOrderItem poi = PharmacyOrderItem.builder()
                        .pharmacyOrder(pOrder)
                        .submissionItem(psi)
                        .medicineName(psi.getMedicineName())
                        .genericName(psi.getGenericName())
                        .dosage(psi.getDosage())
                        .quantity(qty)
                        .unitPrice(unit)
                        .totalPrice(lineTotal)
                        .build();
                orderItems.add(poi);
            }

            if (orderItems.isEmpty()) {
                throw new IllegalArgumentException("Selection[" + selectionIndex + "]: no valid items selected");
            }

            pOrder.setItems(orderItems);
            pOrder.setSubtotal(subSubtotal);
            pOrder.setTotal(subSubtotal);
            overallSubtotal = overallSubtotal.add(subSubtotal);
            order.getPharmacyOrders().add(pOrder);

            // Mark selected submission as PREPARING_ORDER only if it's still in an initial state
            PrescriptionStatus current = submission.getStatus();
            if (current == PrescriptionStatus.PENDING_REVIEW || current == PrescriptionStatus.PENDING) {
                submission.setStatus(PrescriptionStatus.PREPARING_ORDER);
                submissionRepository.save(submission);
            }
        }

        // Optionally delete unselected submissions that never sent any items (no preview)
        Set<Long> selectedPharmacyIds = request.getPharmacies().stream()
                .map(PharmacyOrderSelectionDTO::getPharmacyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<PrescriptionSubmission> toDelete = new ArrayList<>();
        for (PrescriptionSubmission sub : allSubs) {
            Long pid = sub.getPharmacy().getId();
            if (!selectedPharmacyIds.contains(pid)) {
                if (sub.getItems() == null || sub.getItems().isEmpty()) {
                    toDelete.add(sub);
                }
            }
        }
        if (!toDelete.isEmpty()) {
            submissionRepository.deleteAll(toDelete);
        }

        // After attaching all PharmacyOrders, mark their submissions as ORDER_PLACED
        if (order.getPharmacyOrders() != null) {
            for (PharmacyOrder po : order.getPharmacyOrders()) {
                PrescriptionSubmission sub = po.getSubmission();
                if (sub != null && sub.getStatus() != PrescriptionStatus.ORDER_PLACED) {
                    sub.setStatus(PrescriptionStatus.ORDER_PLACED);
                    submissionRepository.save(sub);
                }
            }
        }

        // Update parent prescription status
        // depending on your desired flow. Example: only set if still pending.
        PrescriptionStatus ps = prescription.getStatus();
        if (ps == PrescriptionStatus.PENDING_REVIEW || ps == PrescriptionStatus.PENDING) {
            prescription.setStatus(PrescriptionStatus.ORDER_PLACED);
            prescriptionRepository.save(prescription);
        }

        order.setSubtotal(overallSubtotal);
        order.setTotal(overallSubtotal);

        CustomerOrder saved = customerOrderRepository.save(order);
        return toCustomerDTO(saved, true);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerOrderDTO getCustomerOrder(Long customerId, String orderCode) {
        CustomerOrder order = customerOrderRepository.findByOrderCodeAndCustomerId(orderCode, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return toCustomerDTO(order, true);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacyOrderDTO getPharmacyOrder(Long pharmacistId, Long pharmacyOrderId) {
        User user = userRepository.findById(pharmacistId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacist not found"));
        if (!(user instanceof PharmacistUser phUser) || phUser.getPharmacy() == null) {
            throw new IllegalArgumentException("Pharmacist not assigned to pharmacy");
        }
        PharmacyOrder po = pharmacyOrderRepository.findByIdAndPharmacyId(pharmacyOrderId, phUser.getPharmacy().getId())
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy order not found"));
        return toPharmacyDTO(po, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyOrderDTO> listPharmacyOrders(Long pharmacistId, PharmacyOrderStatus status) {
        User user = userRepository.findById(pharmacistId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacist not found"));
        if (!(user instanceof PharmacistUser phUser) || phUser.getPharmacy() == null) {
            throw new IllegalArgumentException("Pharmacist not assigned to pharmacy");
        }
        Long pharmacyId = phUser.getPharmacy().getId();
        List<PharmacyOrder> list = (status == null)
                ? pharmacyOrderRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacyId)
                : pharmacyOrderRepository.findByPharmacyIdAndStatusOrderByCreatedAtDesc(pharmacyId, status);
        return list.stream().map(o -> toPharmacyDTO(o, false)).toList();
    }

    @Override
    public PharmacyOrderDTO updatePharmacyOrderStatus(Long pharmacistId, Long pharmacyOrderId, PharmacyOrderStatus status) {
        if (status == null) throw new IllegalArgumentException("status required");
        User user = userRepository.findById(pharmacistId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacist not found"));
        if (!(user instanceof PharmacistUser phUser) || phUser.getPharmacy() == null) {
            throw new IllegalArgumentException("Pharmacist not assigned to pharmacy");
        }
        PharmacyOrder po = pharmacyOrderRepository.findByIdAndPharmacyId(pharmacyOrderId, phUser.getPharmacy().getId())
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy order not found"));
        if (!isValidTransition(po.getStatus(), status)) {
            throw new IllegalStateException("Invalid status transition");
        }

        // 1) Update slice and persist (ensures updatedAt for completedDate)
        po.setStatus(status);
        po = pharmacyOrderRepository.save(po);

        // 2) Update linked submission status (resolve if missing) - restrict to statuses allowed by DB constraint
        PrescriptionSubmission sub = po.getSubmission();
        if (sub != null) {
            switch (status) {
                case RECEIVED, PREPARING -> sub.setStatus(PrescriptionStatus.PREPARING_ORDER);
                case READY_FOR_PICKUP -> sub.setStatus(PrescriptionStatus.READY_FOR_PICKUP);
                case HANDED_OVER      -> sub.setStatus(PrescriptionStatus.COMPLETED);
                case CANCELLED        -> sub.setStatus(PrescriptionStatus.CANCELLED);
            }
            submissionRepository.save(sub);
        }

        // 3) Roll up to parent order + prescription from fresh DB slices
        CustomerOrder parent = po.getCustomerOrder();
        if (parent != null) {
            List<PharmacyOrder> slices = pharmacyOrderRepository.findByCustomerOrderId(parent.getId());
            boolean hasSlices = !slices.isEmpty();
            boolean allHandedOver = hasSlices && slices.stream().allMatch(s -> s.getStatus() == PharmacyOrderStatus.HANDED_OVER);
            boolean allCancelled = hasSlices && slices.stream().allMatch(s -> s.getStatus() == PharmacyOrderStatus.CANCELLED);
            boolean anyReady = slices.stream().anyMatch(s -> s.getStatus() == PharmacyOrderStatus.READY_FOR_PICKUP);
            boolean anyPreparing = slices.stream().anyMatch(s -> s.getStatus() == PharmacyOrderStatus.PREPARING);

            if (allHandedOver) {
                parent.setStatus(CustomerOrderStatus.COMPLETED);
            } else if (allCancelled) {
                parent.setStatus(CustomerOrderStatus.CANCELLED);
            }
            customerOrderRepository.save(parent);

            Prescription pres = parent.getPrescription();
            if (pres != null) {
                if (allHandedOver) {
                    pres.setStatus(PrescriptionStatus.COMPLETED);
                } else if (allCancelled) {
                    pres.setStatus(PrescriptionStatus.CANCELLED);
                } else if (anyReady) {
                    pres.setStatus(PrescriptionStatus.READY_FOR_PICKUP);
                } else if (anyPreparing) {
                    pres.setStatus(PrescriptionStatus.IN_PROGRESS);
                }
                prescriptionRepository.save(pres);
            }
        }

        return toPharmacyDTO(po, true);
    }

    @Override
    public CustomerOrderDTO payOrder(Long customerId, String orderCode, PayOrderRequestDTO request) {
        CustomerOrder order = customerOrderRepository.findByOrderCodeAndCustomerId(orderCode, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Order already paid");
        }
        if (order.getStatus() == CustomerOrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is cancelled");
        }

        if (request != null) {
            if (request.getPaymentMethod() != null) {
                order.setPaymentMethod(request.getPaymentMethod());
            }
            if (request.getReference() != null && !request.getReference().isBlank()) {
                order.setPaymentReference(request.getReference());
            }
        }

        // Mark paid and advance pharmacy slices from RECEIVED -> PREPARING
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(CustomerOrderStatus.PAID);

        if (order.getPharmacyOrders() != null) {
            for (PharmacyOrder po : order.getPharmacyOrders()) {
                if (po.getStatus() == PharmacyOrderStatus.RECEIVED) {
                    po.setStatus(PharmacyOrderStatus.PREPARING);
                }
            }
        }

        // Nudge prescription to IN_PROGRESS if still pending/review
        Prescription prescription = order.getPrescription();
        if (prescription != null) {
            PrescriptionStatus pstatus = prescription.getStatus();
            if (pstatus == PrescriptionStatus.PENDING_REVIEW || pstatus == PrescriptionStatus.PENDING) {
                prescription.setStatus(PrescriptionStatus.IN_PROGRESS);
                prescriptionRepository.save(prescription);
            }
        }

        CustomerOrder saved = customerOrderRepository.save(order);
        return toCustomerDTO(saved, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerOrderDTO> listCustomerOrders(Long customerId, boolean includeItems) {
        List<CustomerOrder> orders = customerOrderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return orders.stream().map(o -> toCustomerDTO(o, includeItems)).toList();
    }

    private boolean isValidTransition(PharmacyOrderStatus from, PharmacyOrderStatus to) {
        return switch (from) {
            case RECEIVED -> (to == PharmacyOrderStatus.PREPARING || to == PharmacyOrderStatus.CANCELLED);
            case PREPARING -> (to == PharmacyOrderStatus.READY_FOR_PICKUP || to == PharmacyOrderStatus.CANCELLED);
            case READY_FOR_PICKUP -> (to == PharmacyOrderStatus.HANDED_OVER || to == PharmacyOrderStatus.CANCELLED);
            case HANDED_OVER, CANCELLED -> false;
        };
    }

    private CustomerOrderDTO toCustomerDTO(CustomerOrder order, boolean includeItems) {
        List<PharmacyOrder> poList = order.getPharmacyOrders() != null ? order.getPharmacyOrders() : Collections.emptyList();
        List<PharmacyOrderDTO> slices = poList.stream()
                .map(po -> toPharmacyDTO(po, includeItems))
                .toList();
        return CustomerOrderDTO.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .createdAt(formatTime(order.getCreatedAt()))
                .updatedAt(formatTime(order.getUpdatedAt()))
                .prescriptionId(order.getPrescription() != null ? order.getPrescription().getId() : null)
                .prescriptionCode(order.getPrescription() != null ? order.getPrescription().getCode() : null)
                .status(order.getStatus())
                .payment(PaymentDTO.builder()
                        .method(order.getPaymentMethod())
                        .status(order.getPaymentStatus())
                        .amount(order.getTotal())
                        .currency(order.getCurrency())
                        .reference(order.getPaymentReference())
                        .build())
                .totals(OrderTotalsDTO.builder()
                        .subtotal(order.getSubtotal())
                        .discount(order.getDiscount())
                        .tax(order.getTax())
                        .shipping(order.getShipping())
                        .total(order.getTotal())
                        .currency(order.getCurrency())
                        .build())
                .currency(order.getCurrency())
                .pharmacyOrders(slices)
                .build();
    }

    private PharmacyOrderDTO toPharmacyDTO(PharmacyOrder po, boolean includeItems) {
        List<PharmacyOrderItemDTO> items = includeItems ?
                (Optional.ofNullable(po.getItems()).orElse(Collections.emptyList())).stream().map(it -> PharmacyOrderItemDTO.builder()
                        .itemId(it.getId())
                        .previewItemId(it.getSubmissionItem() != null ? it.getSubmissionItem().getId() : null)
                        .medicineName(it.getMedicineName())
                        .genericName(it.getGenericName())
                        .dosage(it.getDosage())
                        .quantity(it.getQuantity())
                        .unitPrice(it.getUnitPrice())
                        .totalPrice(it.getTotalPrice())
                        .notes(it.getSubmissionItem() != null ? it.getSubmissionItem().getNotes() : null)
                        .build()).toList()
                : null;

        CustomerOrder parent = po.getCustomerOrder();
        Prescription pres = parent != null ? parent.getPrescription() : null;
        Customer cust = (parent != null && parent.getCustomer() instanceof Customer c) ? c : null;

        String completedAt = (po.getStatus() == PharmacyOrderStatus.HANDED_OVER && po.getUpdatedAt() != null)
                ? formatTime(po.getUpdatedAt()) : null;

        PaymentDTO payment = PaymentDTO.builder()
                .method(parent != null ? parent.getPaymentMethod() : null)
                .status(parent != null ? parent.getPaymentStatus() : null)
                .amount(po.getTotal())
                .currency(parent != null ? parent.getCurrency() : "LKR")
                .reference(parent != null ? parent.getPaymentReference() : null)
                .build();

        return PharmacyOrderDTO.builder()
                .pharmacyOrderId(po.getId())
                .pharmacyId(po.getPharmacy().getId())
                .pharmacyName(po.getPharmacy().getName())
                .status(po.getStatus())
                .pickupCode(po.getPickupCode())
                .pickupLocation(po.getPickupLocation())
                .pickupLat(po.getPickupLat())
                .pickupLng(po.getPickupLng())
                .customerNote(po.getCustomerNote())
                .pharmacistNote(po.getPharmacistNote())
                .createdAt(formatTime(po.getCreatedAt()))
                .updatedAt(formatTime(po.getUpdatedAt()))
                .completedDate(completedAt)
                .orderCode(po.getOrderCode() != null ? po.getOrderCode() : (parent != null ? parent.getOrderCode() : null))
                .payment(payment)
                .items(items)
                .totals(OrderTotalsDTO.builder()
                        .subtotal(po.getSubtotal())
                        .discount(po.getDiscount())
                        .tax(po.getTax())
                        .shipping(po.getShipping())
                        .total(po.getTotal())
                        .currency(parent != null ? parent.getCurrency() : "LKR")
                        .build())
                .customerName(cust != null ? cust.getFullName() : null)
                .patientEmail(cust != null ? cust.getEmail() : null)
                .patientPhone(cust != null ? cust.getPhoneNumber() : null)
                .patientAddress(pres != null && pres.getDeliveryAddress() != null && !pres.getDeliveryAddress().isBlank()
                        ? pres.getDeliveryAddress() : (cust != null ? cust.getAddress() : null))
                .prescriptionId(pres != null ? pres.getId() : null)
                .prescriptionCode(pres != null ? pres.getCode() : null)
                .prescriptionImageUrl(pres != null ? pres.getImageUrl() : null)
                .build();
    }

    private String generateOrderCode() {
        return "ORD-" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generatePickupCode(Long pharmacyId) {
        return "PU-" + pharmacyId + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    // generate a unique pharmacy-specific order code
    private String generatePharmacyOrderCode(Long pharmacyId) {
        return "PORD-" + pharmacyId + "-" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private Customer resolveCustomer(User user) {
        if (user == null) throw new IllegalStateException("User null");
        if (user instanceof Customer c) return c;
        if (user.getUserType() == UserType.CUSTOMER) {
            Object unproxied = Hibernate.unproxy(user);
            if (unproxied instanceof Customer cu) return cu;
            User reloaded = userRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalStateException("Customer user disappeared"));
            if (reloaded instanceof Customer cr) return cr;
            throw new IllegalStateException("User with CUSTOMER type not mapped to Customer subclass");
        }
        throw new IllegalStateException("Prescription owner userType=" + user.getUserType() + " is not CUSTOMER");
    }

    private String formatTime(java.time.LocalDateTime dt) {
        return dt == null ? null : dt.toString();
    }
}
