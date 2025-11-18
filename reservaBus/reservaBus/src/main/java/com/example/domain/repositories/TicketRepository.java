package com.example.domain.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.domain.entities.Ticket;
import com.example.domain.enums.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByTrip_IdAndStatus(Long tripId, TicketStatus status);

    List<Ticket> findByAccount_Id(Long accountId);

    List<Ticket> findByAccount_IdAndStatus(Long accountId, TicketStatus status);

    List<Ticket> findBySeatNumber(String seatNumber);

    List<Ticket> findByAccount_IdAndSeatNumber(Long accountId, String seatNumber);

    Ticket findByQrCode(String qrCode);

    @Query("SELECT t FROM Ticket t WHERE t.seatNumber IN :seatNumbers AND t.trip.id = :tripId")
    List<Ticket> findTicketsByListOfSeatNumbersFilteredByTripId(List<String> seatNumbers, Long tripId);

    Ticket findByTrip_IdAndSeatNumber(Long tripId, String seatNumber);

    /**
     * Encuentra tickets vendidos (SOLD) que solapen con el tramo especificado.
     * Solapamiento ocurre cuando:
     * - El nuevo tramo empieza dentro de un tramo existente, O
     * - El nuevo tramo termina dentro de un tramo existente, O
     * - El nuevo tramo contiene completamente a un tramo existente
     */
    @Query("""
                SELECT t FROM Ticket t
                WHERE t.trip.id = :tripId
                AND t.seatNumber = :seatNumber
                AND t.status = 'SOLD'
                AND (
                    (t.fromStop.sequence <= :newFromSeq AND t.toStop.sequence > :newFromSeq)
                    OR (t.fromStop.sequence < :newToSeq AND t.toStop.sequence >= :newToSeq)
                    OR (t.fromStop.sequence >= :newFromSeq AND t.toStop.sequence <= :newToSeq)
                )
            """)
    List<Ticket> findOverlappingSoldTickets(
            @Param("tripId") Long tripId,
            @Param("seatNumber") String seatNumber,
            @Param("newFromSeq") Integer newFromSequence,
            @Param("newToSeq") Integer newToSequence);

    @Query("SELECT t FROM Ticket t WHERE t.trip.id = :tripId AND t.fromStop.id = :fromStopId AND t.toStop.id = :toStopId")
    List<Ticket> findTicketsSameTripAndStops(
            @Param("tripId") Long tripId,
            @Param("fromStopId") Long fromStopId,
            @Param("toStopId") Long toStopId);

    // Real-time metrics queries - show ALL data without date filtering
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'CONFIRMED'")
    Long countConfirmedTickets(@Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT SUM(t.price) FROM Ticket t WHERE t.status = 'CONFIRMED'")
    Double getTotalRevenue(@Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'CANCELLED'")
    Long countCancelledTickets(@Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
}
