package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.OAuthAccount;
import com.leo.pillpathbackend.entity.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    Optional<OAuthAccount> findByProviderAndProviderSubject(OAuthProvider provider, String providerSubject);
}