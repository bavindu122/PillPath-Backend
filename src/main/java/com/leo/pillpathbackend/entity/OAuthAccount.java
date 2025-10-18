package com.leo.pillpathbackend.entity;

import com.leo.pillpathbackend.entity.enums.OAuthProvider;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "oauth_account",
        uniqueConstraints = @UniqueConstraint(name = "uk_oauth_provider_sub", columnNames = {"provider", "provider_sub"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OAuthProvider provider;

    @Column(name = "provider_sub", nullable = false, length = 128)
    private String providerSubject; // Google "sub"

    @Column(nullable = false)
    private String email;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}