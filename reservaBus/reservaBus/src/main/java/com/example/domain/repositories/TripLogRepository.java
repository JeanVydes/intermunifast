package com.example.domain.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.domain.entities.TripLog;

public interface TripLogRepository extends JpaRepository<TripLog, Long> {

        @Query("SELECT SUM(tl.totalRevenue) FROM TripLog tl")
        Double getTotalRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(tl) FROM TripLog tl WHERE tl.onTime = true")
        Long countOnTimeTrips(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(tl) FROM TripLog tl")
        Long countTotalTrips(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        Optional<TripLog> findByTrip_Id(Long tripId);
}
