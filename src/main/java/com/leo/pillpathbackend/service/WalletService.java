package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.entity.*;
import com.leo.pillpathbackend.entity.enums.WalletOwnerType;
import com.leo.pillpathbackend.entity.enums.WalletTransactionType;
import com.leo.pillpathbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletSettingsService settingsService;

    private static final RoundingMode RM = RoundingMode.HALF_UP;

    public Wallet getOrCreatePlatformWallet() {
        return walletRepository.findByOwnerTypeAndOwnerIdIsNull(WalletOwnerType.PLATFORM)
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .ownerType(WalletOwnerType.PLATFORM)
                        .ownerId(null)
                        .currency(settingsService.getSettings().getCurrency())
                        .balance(BigDecimal.ZERO)
                        .build()));
    }

    public Wallet getOrCreatePharmacyWallet(Long pharmacyId) {
        return walletRepository.findByOwnerTypeAndOwnerId(WalletOwnerType.PHARMACY, pharmacyId)
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .ownerType(WalletOwnerType.PHARMACY)
                        .ownerId(pharmacyId)
                        .currency(settingsService.getSettings().getCurrency())
                        .balance(BigDecimal.ZERO)
                        .build()));
    }

    public BigDecimal resolveCommissionPercent(Long pharmacyId) {
        return settingsService.resolveCommissionPercent(pharmacyId);
    }

    public void postCustomerCardCaptured(String orderCode, Long pharmacyOrderId, Long prescriptionId, Long pharmacyId,
                                         BigDecimal amount, String paymentId, String externalKey) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("amount > 0 required");
        if (externalKey != null && walletTransactionRepository.findByExternalKey(externalKey).isPresent()) {
            return; // idempotent
        }
        Wallet platform = getOrCreatePlatformWallet();
        Wallet pharmacy = getOrCreatePharmacyWallet(pharmacyId);

        BigDecimal commissionPercent = resolveCommissionPercent(pharmacyId);
        BigDecimal commission = amount.multiply(commissionPercent).divide(new BigDecimal("100"), 2, RM);
        BigDecimal pharmacyAccrual = amount.subtract(commission).setScale(2, RM);

        // Platform += amount
        addTransaction(platform, WalletTransactionType.CUSTOMER_CARD_CAPTURE, amount, orderCode, pharmacyOrderId, prescriptionId, paymentId, externalKey,
                "Card capture for order " + orderCode);
        // Pharmacy += amount - commission
        addTransaction(pharmacy, WalletTransactionType.PHARMACY_LIABILITY_ACCRUAL, pharmacyAccrual, orderCode, pharmacyOrderId, prescriptionId, paymentId, null,
                "Pharmacy accrual net of commission for order " + orderCode);
    }

    public void postCustomerCashCollected(String orderCode, Long pharmacyOrderId, Long prescriptionId, Long pharmacyId,
                                          BigDecimal amount, String externalKey) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("amount > 0 required");
        if (externalKey != null && walletTransactionRepository.findByExternalKey(externalKey).isPresent()) {
            return; // idempotent
        }
        Wallet pharmacy = getOrCreatePharmacyWallet(pharmacyId);
        BigDecimal commissionPercent = resolveCommissionPercent(pharmacyId);
        BigDecimal commission = amount.multiply(commissionPercent).divide(new BigDecimal("100"), 2, RM);
        // Pharmacy owes commission: reduce wallet by commission
        addTransaction(pharmacy, WalletTransactionType.CASH_COMMISSION_ACCRUAL, commission.negate(), orderCode, pharmacyOrderId, prescriptionId, null, externalKey,
                "Cash order commission accrual for order " + orderCode);
    }

    public void postCommissionCardCaptured(Long pharmacyId, String orderCode, BigDecimal commissionAmount, BigDecimal convenienceFee,
                                           String paymentId, String externalKey) {
        if (commissionAmount == null || commissionAmount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("commissionAmount > 0 required");
        if (convenienceFee == null) convenienceFee = BigDecimal.ZERO;
        if (externalKey != null && walletTransactionRepository.findByExternalKey(externalKey).isPresent()) {
            return; // idempotent
        }
        Wallet platform = getOrCreatePlatformWallet();
        Wallet pharmacy = getOrCreatePharmacyWallet(pharmacyId);
        // Platform += commission
        addTransaction(platform, WalletTransactionType.COMMISSION_CARD_CAPTURE, commissionAmount, orderCode, null, null, paymentId, externalKey,
                "Commission card capture for " + orderCode);
        // Platform += convenience fee if any
        if (convenienceFee.compareTo(BigDecimal.ZERO) > 0) {
            addTransaction(platform, WalletTransactionType.CONVENIENCE_FEE_INCOME, convenienceFee, orderCode, null, null, paymentId, null,
                    "Convenience fee income for commission capture");
        }
        // Pharmacy commission owed reduced (i.e., increase by positive commission)
        addTransaction(pharmacy, WalletTransactionType.COMMISSION_CARD_CAPTURE, commissionAmount, orderCode, null, null, paymentId, null,
                "Commission settled by card for " + orderCode);
    }

    public void postPayout(Long pharmacyId, BigDecimal amount, String reference, String externalKey) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("amount > 0 required");
        if (externalKey != null && walletTransactionRepository.findByExternalKey(externalKey).isPresent()) {
            return; // idempotent
        }
        Wallet platform = getOrCreatePlatformWallet();
        Wallet pharmacy = getOrCreatePharmacyWallet(pharmacyId);
        // Validate balances: cannot payout more than pharmacy positive balance
        if (pharmacy.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient pharmacy wallet balance for payout");
        }
        // Platform -= amount; Pharmacy -= amount
        addTransaction(platform, WalletTransactionType.PHARMACY_PAYOUT, amount.negate(), null, null, null, null, externalKey,
                "Manual payout: " + (reference != null ? reference : ""));
        addTransaction(pharmacy, WalletTransactionType.PHARMACY_PAYOUT, amount.negate(), null, null, null, null, null,
                "Manual payout: " + (reference != null ? reference : ""));
    }

    public void postRefundFull(String orderCode, Long pharmacyOrderId, Long pharmacyId, BigDecimal amount, String externalKey) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("amount > 0 required");
        if (externalKey != null && walletTransactionRepository.findByExternalKey(externalKey).isPresent()) {
            return; // idempotent
        }
        Wallet platform = getOrCreatePlatformWallet();
        Wallet pharmacy = getOrCreatePharmacyWallet(pharmacyId);
        // Reverse platform card capture and pharmacy accrual
        addTransaction(platform, WalletTransactionType.REFUND_FULL, amount.negate(), orderCode, pharmacyOrderId, null, null, externalKey,
                "Full refund for order " + orderCode);
        addTransaction(pharmacy, WalletTransactionType.REFUND_FULL, amount.negate(), orderCode, pharmacyOrderId, null, null, null,
                "Full refund for order " + orderCode);
    }

    private void addTransaction(Wallet wallet, WalletTransactionType type, BigDecimal delta, String orderCode,
                                Long pharmacyOrderId, Long prescriptionId, String paymentId, String externalKey, String note) {
        int attempts = 0;
        while (true) {
            try {
                BigDecimal newBalance = wallet.getBalance().add(delta).setScale(2, RM);
                wallet.setBalance(newBalance);
                wallet = walletRepository.saveAndFlush(wallet);
                WalletTransaction txn = WalletTransaction.builder()
                        .wallet(wallet)
                        .type(type)
                        .amount(delta.setScale(2, RM))
                        .balanceAfter(newBalance)
                        .currency(wallet.getCurrency())
                        .orderCode(orderCode)
                        .pharmacyOrderId(pharmacyOrderId)
                        .prescriptionId(prescriptionId)
                        .paymentId(paymentId)
                        .externalKey(externalKey)
                        .note(note)
                        .build();
                walletTransactionRepository.save(txn);
                return;
            } catch (OptimisticLockingFailureException e) {
                if (++attempts >= 2) throw e;
                // Reload and retry once
                wallet = walletRepository.findById(wallet.getId()).orElseThrow();
            }
        }
    }

    @Transactional(readOnly = true)
    public List<WalletTransaction> listRecentTransactionsForPlatform(int pageSize) {
        Wallet platform = getOrCreatePlatformWallet();
        Pageable pageable = PageRequest.of(0, Math.max(pageSize, 1));
        Page<WalletTransaction> page = walletTransactionRepository.findByWallet_IdOrderByCreatedAtDesc(platform.getId(), pageable);
        return page.getContent();
    }

    @Transactional(readOnly = true)
    public List<WalletTransaction> listRecentTransactionsForPharmacy(Long pharmacyId, int pageSize) {
        Wallet wallet = getOrCreatePharmacyWallet(pharmacyId);
        Pageable pageable = PageRequest.of(0, Math.max(pageSize, 1));
        Page<WalletTransaction> page = walletTransactionRepository.findByWallet_IdOrderByCreatedAtDesc(wallet.getId(), pageable);
        return page.getContent();
    }

    @Transactional(readOnly = true)
    public Page<WalletTransaction> pageTransactionsForPharmacy(Long pharmacyId, int page, int size) {
        Wallet wallet = getOrCreatePharmacyWallet(pharmacyId);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return walletTransactionRepository.findByWallet_IdOrderByCreatedAtDesc(wallet.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<WalletTransaction> pageTransactionsForPlatform(int page, int size) {
        Wallet platform = getOrCreatePlatformWallet();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return walletTransactionRepository.findByWallet_IdOrderByCreatedAtDesc(platform.getId(), pageable);
    }
}
