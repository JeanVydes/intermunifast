package com.example.domain.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.domain.entities.TripLog;

public interface TripLogRepository extends JpaRepository<TripLog, Long> {

        @Query("SELECT tl FROM TripLog tl WHERE tl.scheduledDeparture BETWEEN :startDate AND :endDate")
        List<TripLog> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT AVG(tl.occupationRate) FROM TripLog tl")
        Double getAverageOccupationRate(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT SUM(tl.totalRevenue) FROM TripLog tl")
        Double getTotalRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(tl) FROM TripLog tl WHERE tl.onTime = true")
        Long countOnTimeTrips(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(tl) FROM TripLog tl")
        Long countTotalTrips(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT SUM(tl.soldSeats) FROM TripLog tl")
        Long getTotalSeatsSold(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT SUM(tl.totalSeats) FROM TripLog tl")
        Long getTotalSeatsOffered(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT SUM(tl.cancelledSeats) FROM TripLog tl")
        Long getTotalCancellations(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Check if a trip has already been logged
        Optional<TripLog> findByTrip_Id(Long tripId);
}
