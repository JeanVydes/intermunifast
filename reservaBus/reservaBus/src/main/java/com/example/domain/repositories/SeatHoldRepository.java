package com.example.domain.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.domain.entities.SeatHold;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {
    List<SeatHold> findByTrip_Id(Long tripId);

    List<SeatHold> findBySeatNumber(String seatNumber);

    Optional<SeatHold> findByTrip_IdAndSeatNumber(Long tripId, String seatNumber);

    List<SeatHold> findByAccount_Id(Long accountId);

    @Query("SELECT h FROM SeatHold h WHERE h.seatNumber IN :seatNumbers AND h.expiresAt > :now AND h.trip.id = :tripId")
    List<SeatHold> findActiveHoldsByListOfSeatNumbersAndCurrentTimeAndTripId(List<String> seatNumbers,
            LocalDateTime now, Long tripId);

    /**
     * Encuentra holds activos (no expirados) que solapen con el tramo especificado.
     * Solapamiento ocurre cuando:
     * - El nuevo tramo empieza dentro de un tramo existente, O
     * - El nuevo tramo termina dentro de un tramo existente, O
     * - El nuevo tramo contiene completamente a un tramo existente
     */
    @Query("""
                SELECT h FROM SeatHold h
                WHERE h.trip.id = :tripId
                AND h.seatNumber = :seatNumber
                AND h.expiresAt > :now
                AND (
                    (h.fromStop.sequence <= :newFromSeq AND h.toStop.sequence > :newFromSeq)
                    OR (h.fromStop.sequence < :newToSeq AND h.toStop.sequence >= :newToSeq)
                    OR (h.fromStop.sequence >= :newFromSeq AND h.toStop.sequence <= :newToSeq)
                )
            """)
    List<SeatHold> findOverlappingActiveHolds(
            @Param("tripId") Long tripId,
            @Param("seatNumber") String seatNumber,
            @Param("newFromSeq") Integer newFromSequence,
            @Param("newToSeq") Integer newToSequence,
            @Param("now") LocalDateTime now);
}
