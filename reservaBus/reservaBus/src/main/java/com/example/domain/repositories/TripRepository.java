package com.example.domain.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.domain.entities.Trip;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByRoute_IdAndDate(Long routeId, LocalDate date);

    @Query("SELECT t FROM Trip t WHERE t.date BETWEEN CURRENT_TIMESTAMP AND CURRENT_TIMESTAMP + INTERVAL :minutes MINUTE")
    List<Trip> findStartingTripsInNextMinutes(int minutes);
}
