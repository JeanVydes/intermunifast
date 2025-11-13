package com.example.domain.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.domain.entities.Trip;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByRoute_IdAndDate(Long routeId, LocalDate date);

}
