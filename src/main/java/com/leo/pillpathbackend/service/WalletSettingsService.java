package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.entity.CommissionRule;
import com.leo.pillpathbackend.entity.Pharmacy;
import com.leo.pillpathbackend.entity.PlatformSettings;
import com.leo.pillpathbackend.repository.CommissionRuleRepository;
import com.leo.pillpathbackend.repository.PlatformSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletSettingsService {
    private final PlatformSettingsRepository platformSettingsRepository;
    private final CommissionRuleRepository commissionRuleRepository;

    public PlatformSettings getSettings() {
        return platformSettingsRepository.findById(1L)
                .orElseGet(() -> platformSettingsRepository.save(PlatformSettings.builder().id(1L).build()));
    }

    public PlatformSettings updateSettings(String currency, BigDecimal commissionPercent, BigDecimal convenienceFee, Long version) {
        PlatformSettings s = getSettings();
        if (version != null && s.getVersion() != null && !s.getVersion().equals(version)) {
            throw new IllegalStateException("Stale settings version");
        }
        if (currency != null && !currency.isBlank()) s.setCurrency(currency);
        if (commissionPercent != null) s.setCommissionPercent(commissionPercent);
        if (convenienceFee != null) s.setConvenienceFee(convenienceFee);
        return platformSettingsRepository.save(s);
    }

    public CommissionRule getCommissionRule(Long pharmacyId) {
        return commissionRuleRepository.findByPharmacy_Id(pharmacyId).orElse(null);
    }

    public CommissionRule upsertCommissionRule(Long pharmacyId, BigDecimal commissionPercent, Long version) {
        CommissionRule rule = commissionRuleRepository.findByPharmacy_Id(pharmacyId).orElse(null);
        if (rule == null) {
            Pharmacy p = new Pharmacy();
            p.setId(pharmacyId);
            rule = CommissionRule.builder()
                    .pharmacy(p)
                    .commissionPercent(commissionPercent)
                    .build();
        } else {
            if (version != null && rule.getVersion() != null && !rule.getVersion().equals(version)) {
                throw new IllegalStateException("Stale commission rule version");
            }
            rule.setCommissionPercent(commissionPercent);
        }
        return commissionRuleRepository.save(rule);
    }

    public void deleteCommissionRule(Long pharmacyId) {
        commissionRuleRepository.deleteByPharmacy_Id(pharmacyId);
    }

    public BigDecimal resolveCommissionPercent(Long pharmacyId) {
        CommissionRule rule = getCommissionRule(pharmacyId);
        if (rule != null && rule.getCommissionPercent() != null) return rule.getCommissionPercent();
        return getSettings().getCommissionPercent();
    }
}
