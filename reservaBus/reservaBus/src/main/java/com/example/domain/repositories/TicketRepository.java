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

    @Query("""
            SELECT t FROM Ticket t
            WHERE t.trip.id = :tripId
            AND t.seatNumber = :seatNumber
            AND t.status = 'CONFIRMED'
            AND (
                (COALESCE(t.fromStop.sequence, -2147483648) <= :newFromSeq AND COALESCE(t.toStop.sequence, 2147483647) > :newFromSeq)
                OR (COALESCE(t.fromStop.sequence, -2147483648) < :newToSeq AND COALESCE(t.toStop.sequence, 2147483647) >= :newToSeq)
                OR (COALESCE(t.fromStop.sequence, -2147483648) >= :newFromSeq AND COALESCE(t.toStop.sequence, 2147483647) <= :newToSeq)
            )
            """)
    List<Ticket> findOverlappingSoldTickets(
            @Param("tripId") Long tripId,
            @Param("seatNumber") String seatNumber,
            @Param("newFromSeq") Integer newFromSequence,
            @Param("newToSeq") Integer newToSequence);

    @Query("SELECT COALESCE(SUM(t.price), 0.0) FROM Ticket t WHERE t.status = ?1 AND t.createdAt >= ?2 AND t.createdAt <= ?3")
    Double getTotalRevenue(TicketStatus status, Long startDate, Long endDate);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = ?1 AND t.createdAt >= ?2 AND t.createdAt <= ?3")
    Long countConfirmedTickets(TicketStatus status, Long startDate, Long endDate);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = ?1 AND t.updatedAt >= ?2 AND t.updatedAt <= ?3")
    Long countCancelledTickets(TicketStatus status, Long startDate, Long endDate);
}
