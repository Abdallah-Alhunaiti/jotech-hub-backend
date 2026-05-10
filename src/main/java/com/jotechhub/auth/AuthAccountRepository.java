package com.jotechhub.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthAccountRepository extends JpaRepository<AuthAccount, Long> {
    Optional<AuthAccount> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    Optional<AuthAccount> findByUserIdAndProvider(Long userId, AuthProvider provider);
}