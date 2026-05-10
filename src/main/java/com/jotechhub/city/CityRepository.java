package com.jotechhub.city;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    List<City> findByActiveTrueOrderByNameAsc();
}