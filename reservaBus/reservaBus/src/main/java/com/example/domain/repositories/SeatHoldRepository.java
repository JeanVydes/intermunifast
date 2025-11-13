package com.example.domain.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.domain.entities.SeatHold;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {
    List<SeatHold> findByTrip_Id(Long tripId);

    List<SeatHold> findBySeatNumber(String seatNumber);

    List<SeatHold> findBySeatNumberIn(List<String> seatNumbers);

    Optional<SeatHold> findByTrip_IdAndSeatNumber(Long tripId, String seatNumber);

    List<SeatHold> findByAccount_Id(Long accountId);

    List<SeatHold> findByExpiresAtBefore(LocalDateTime now);

    @Query("SELECT h FROM SeatHold h WHERE h.seatNumber IN :seatNumbers AND h.expiresAt > :now AND h.trip.id = :tripId")
    List<SeatHold> findActiveHoldsByListOfSeatNumbersAndCurrentTimeAndTripId(List<String> seatNumbers,
            LocalDateTime now, Long tripId);
}
