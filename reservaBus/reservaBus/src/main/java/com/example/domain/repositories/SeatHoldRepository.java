package com.example.domain.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.SeatHold;
import com.example.domain.enums.SeatHoldStatus;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {
    List<SeatHold> findByTripIdAndStatus(Long tripId, SeatHoldStatus status);

    Optional<SeatHold> findByTripIdAndSeatNumber(Long tripId, int seatNumber);

    List<SeatHold> findByUserId(Long userId);

    List<SeatHold> findByExpirationTimeBefore(LocalDateTime now);

}
