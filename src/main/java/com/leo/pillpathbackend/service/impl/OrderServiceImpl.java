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
        Customer cust = resolveCustomer(prescription.getCustomer());

        CustomerOrder order = CustomerOrder.builder()
                .orderCode(generateOrderCode())
                .customer(cust)
                .prescription(prescription)
                .status(CustomerOrderStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .currency("LKR")
                .build();
        if (order.getPharmacyOrders() == null) order.setPharmacyOrders(new ArrayList<>());

        BigDecimal overallSubtotal = BigDecimal.ZERO;
        int idx = 0;
        for (PharmacyOrderSelectionDTO sel : request.getPharmacies()) {
            final int selectionIndex = idx++;
            Long selPharmacyId = sel.getPharmacyId();
            Long selSubmissionId = sel.getSubmissionId();

            if (selPharmacyId == null && selSubmissionId == null) {
                throw new IllegalArgumentException("Selection[" + selectionIndex + "]: pharmacyId or submissionId required");
            }

            PrescriptionSubmission submission = null;
            if (selSubmissionId != null) {
                submission = submissionRepository.findById(selSubmissionId)
                        .orElseThrow(() -> new IllegalArgumentException("Selection[" + selectionIndex + "]: submission not found: " + selSubmissionId));
                if (!Objects.equals(submission.getPrescription().getId(), prescription.getId())) {
                    throw new IllegalArgumentException("Selection[" + selectionIndex + "]: submission does not belong to prescription");
                }
                if (selPharmacyId == null) {
                    selPharmacyId = submission.getPharmacy().getId();
                    sel.setPharmacyId(selPharmacyId);
                }
            }

            final Long effectivePharmacyId;
            if (submission == null) {
                effectivePharmacyId = selPharmacyId;
                List<PrescriptionSubmission> subsForPharmacy = submissionRepository.findByPrescriptionId(prescription.getId())
                        .stream().filter(s -> Objects.equals(s.getPharmacy().getId(), effectivePharmacyId)).toList();
                if (subsForPharmacy.isEmpty()) {
                    throw new IllegalArgumentException("Selection[" + selectionIndex + "]: no submissions for given pharmacy");
                }
                if (subsForPharmacy.size() > 1) {
                    throw new IllegalArgumentException("Selection[" + selectionIndex + "]: multiple submissions found; provide submissionId explicitly");
                }
                submission = subsForPharmacy.get(0);
                sel.setSubmissionId(submission.getId());
                if (selPharmacyId == null) {
                    selPharmacyId = submission.getPharmacy().getId();
                    sel.setPharmacyId(selPharmacyId);
                }
            } else {
                effectivePharmacyId = selPharmacyId;
            }

            if (!Objects.equals(submission.getPharmacy().getId(), effectivePharmacyId)) {
                throw new IllegalArgumentException("Selection[" + selectionIndex + "]: submission does not belong to pharmacy");
            }

            List<Long> desiredItemIds = sel.getItemIds() == null ? List.of() : sel.getItemIds();
            List<PrescriptionSubmissionItem> chosenItems = submission.getItems().stream()
                    .filter(i -> desiredItemIds.isEmpty() || desiredItemIds.contains(i.getId()))
                    .collect(Collectors.toList());
            if (!desiredItemIds.isEmpty() && chosenItems.isEmpty()) {
                throw new IllegalArgumentException("Selection[" + selectionIndex + "]: none of the specified itemIds found in submission");
            }

            BigDecimal subSubtotal = BigDecimal.ZERO;
            PharmacyOrder pOrder = PharmacyOrder.builder()
                    .customerOrder(order)
                    .pharmacy(submission.getPharmacy())
                    .submission(submission)
                    .status(PharmacyOrderStatus.RECEIVED)
                    .pickupCode(generatePickupCode(submission.getPharmacy().getId()))
                    .pickupLocation(submission.getPharmacy().getAddress())
                    .customerNote(sel.getCustomerNote())
                    .build();

            List<PharmacyOrderItem> items = new ArrayList<>();
            for (PrescriptionSubmissionItem psi : chosenItems) {
                BigDecimal unit = psi.getUnitPrice() != null ? psi.getUnitPrice() : BigDecimal.ZERO;
                int qty = psi.getQuantity() != null ? psi.getQuantity() : 0;
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
                items.add(poi);
            }
            pOrder.setItems(items);
            pOrder.setSubtotal(subSubtotal);
            pOrder.setTotal(subSubtotal);
            overallSubtotal = overallSubtotal.add(subSubtotal);
            order.getPharmacyOrders().add(pOrder);
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
        po.setStatus(status);
        return toPharmacyDTO(po, true);
    }

    private boolean isValidTransition(PharmacyOrderStatus from, PharmacyOrderStatus to) {
        if (from == to) return true;
        return switch (from) {
            case RECEIVED -> (to == PharmacyOrderStatus.PREPARING || to == PharmacyOrderStatus.CANCELLED);
            case PREPARING -> (to == PharmacyOrderStatus.READY_FOR_PICKUP || to == PharmacyOrderStatus.CANCELLED);
            case READY_FOR_PICKUP -> (to == PharmacyOrderStatus.HANDED_OVER || to == PharmacyOrderStatus.CANCELLED);
            case HANDED_OVER, CANCELLED -> false;
        };
    }

    private CustomerOrderDTO toCustomerDTO(CustomerOrder order, boolean includeItems) {
        List<PharmacyOrderDTO> slices = order.getPharmacyOrders().stream()
                .map(po -> toPharmacyDTO(po, includeItems))
                .toList();
        return CustomerOrderDTO.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .createdAt(formatTime(order.getCreatedAt()))
                .updatedAt(formatTime(order.getUpdatedAt()))
                .prescriptionCode(order.getPrescription() != null ? order.getPrescription().getCode() : null)
                .status(order.getStatus())
                .payment(PaymentDTO.builder()
                        .method(order.getPaymentMethod())
                        .status(order.getPaymentStatus())
                        .amount(order.getTotal())
                        .currency(order.getCurrency())
                        .reference(null)
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
        List<PharmacyOrderItemDTO> items = includeItems ? po.getItems().stream().map(it -> PharmacyOrderItemDTO.builder()
                .itemId(it.getId())
                .previewItemId(it.getSubmissionItem() != null ? it.getSubmissionItem().getId() : null)
                .medicineName(it.getMedicineName())
                .genericName(it.getGenericName())
                .dosage(it.getDosage())
                .quantity(it.getQuantity())
                .unitPrice(it.getUnitPrice())
                .totalPrice(it.getTotalPrice())
                .build()).toList() : null;
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
                .items(items)
                .totals(OrderTotalsDTO.builder()
                        .subtotal(po.getSubtotal())
                        .discount(po.getDiscount())
                        .tax(po.getTax())
                        .shipping(po.getShipping())
                        .total(po.getTotal())
                        .currency("LKR")
                        .build())
                .customerName(po.getCustomerOrder() != null && po.getCustomerOrder().getCustomer() != null ? po.getCustomerOrder().getCustomer().getFullName() : null)
                .prescriptionCode(po.getCustomerOrder() != null && po.getCustomerOrder().getPrescription() != null ? po.getCustomerOrder().getPrescription().getCode() : null)
                .build();
    }

    private String generateOrderCode() {
        return "ORD-" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0,6).toUpperCase();
    }

    private String generatePickupCode(Long pharmacyId) {
        return "PU-" + pharmacyId + "-" + UUID.randomUUID().toString().substring(0,4).toUpperCase();
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

    private String formatTime(java.time.LocalDateTime dt) { return dt == null ? null : dt.toString(); }
}
