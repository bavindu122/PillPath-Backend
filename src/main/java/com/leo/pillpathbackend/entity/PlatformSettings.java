package com.leo.pillpathbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "platform_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformSettings {
    @Id
    @Builder.Default
    private Long id = 1L; // singleton

    @Column(nullable = false, length = 8)
    @Builder.Default
    private String currency = "LKR";

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal commissionPercent = new BigDecimal("10.00");

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal convenienceFee = BigDecimal.ZERO;

    @Version
    private Long version;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

