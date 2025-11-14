package com.example.domain.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.domain.entities.Trip;
import com.example.domain.enums.TripStatus;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByRoute_Id(Long routeId);

    List<Trip> findByBus_Id(Long busId);

    List<Trip> findByStatus(TripStatus status);

    @Query("SELECT t FROM Trip t WHERE t.route.id = :routeId AND DATE(t.departureAt) = DATE(:departureDate)")
    List<Trip> findByRouteIdAndDepartureDate(@Param("routeId") Long routeId,
            @Param("departureDate") LocalDateTime departureDate);

    @Query("SELECT t FROM Trip t WHERE t.departureAt BETWEEN :now AND :futureTime")
    List<Trip> findStartingTripsInNextMinutes(@Param("now") LocalDateTime now,
            @Param("futureTime") LocalDateTime futureTime);
}
