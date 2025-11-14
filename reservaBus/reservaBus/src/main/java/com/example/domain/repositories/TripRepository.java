package com.example.domain.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.domain.entities.Trip;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByRoute_IdAndDate(Long routeId, LocalDate date);

    @Query("SELECT t FROM Trip t WHERE t.departureAt BETWEEN :now AND :futureTime")
    List<Trip> findStartingTripsInNextMinutes(@Param("now") LocalDateTime now,
            @Param("futureTime") LocalDateTime futureTime);
}
