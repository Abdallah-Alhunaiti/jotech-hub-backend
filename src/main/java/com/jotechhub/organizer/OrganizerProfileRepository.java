package com.jotechhub.organizer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizerProfileRepository extends JpaRepository<OrganizerProfile, Long> {
    Optional<OrganizerProfile> findByUserId(Long userId);
}