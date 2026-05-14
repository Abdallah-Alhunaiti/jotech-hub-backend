package com.jotechhub.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingGoogleSignupRepository extends JpaRepository<PendingGoogleSignup, Long> {

    Optional<PendingGoogleSignup> findByToken(String token);

    void deleteByToken(String token);
}