package com.jotechhub.university;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UniversityRepository extends JpaRepository<University, Long> {
    Optional<University> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    List<University> findByActiveTrueOrderByNameAsc();
}