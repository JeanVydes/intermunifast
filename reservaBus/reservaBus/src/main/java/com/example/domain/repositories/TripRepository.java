package com.example.domain.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.domain.entities.Trip;
import com.example.domain.enums.TripStatus;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByRouteIdAndDate(Long routeId, LocalDate date);
    Optional<Trip> findById(Long id);
    List<Trip> findByStatus(TripStatus status);
    Optional<Trip> findByIdAndFilters(Long id, String routeId, String status);
}
    
