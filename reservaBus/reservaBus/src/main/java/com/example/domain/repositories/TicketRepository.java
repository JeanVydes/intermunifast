package com.example.domain.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.domain.entities.Ticket;
import com.example.domain.enums.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByTrip_IdAndStatus(Long tripId, TicketStatus status);

    List<Ticket> findByAccount_Id(Long accountId);

    List<Ticket> findBySeatNumber(String seatNumber);

    List<Ticket> findByAccount_IdAndSeatNumber(Long accountId, String seatNumber);

    Ticket findByQrCode(String qrCode);

    List<Ticket> findByStatusIn(List<TicketStatus> statuses);

    List<Ticket> findByFromStop_IdAndToStop_IdAndStatus(Long fromStopId, Long toStopId, TicketStatus status);

    @Query("SELECT DISTINCT t.paymentMethod FROM Ticket t WHERE t.trip.id = :tripId")
    List<String> findPaymentMethodsUsedInTrip(@Param("tripId") Long tripId);

    @Query("SELECT t FROM Ticket t WHERE t.seatNumber IN :seatNumbers AND t.trip.id = :tripId")
    List<Ticket> findTicketsByListOfSeatNumbersFilteredByTripId(List<String> seatNumbers, Long tripId);

    Ticket findByTrip_IdAndSeatNumber(Long tripId, String seatNumber);

    @Query("SELECT t FROM Ticket t WHERE t.trip.id = :tripId AND t.seatNumber = :seatNumber AND t.fromStop.id = :fromStopId AND t.toStop.id = :toStopId")
    Ticket findByTripIdAndSeatNumberAndFromStopIdAndToStopId(Long tripId, String seatNumber, Long fromStopId,
            Long toStopId);

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
}
