package com.example.domain.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.domain.entities.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByBusId(Long busId);
    List<Seat> findByTripIdAndStatus(Long tripId, String status);

    @Modifying
    @Query("DELETE FROM SeatHold h WHERE h.expiresAt < :now")
    void deleteExpiredHolds(@Param("now") LocalDateTime now);

}
