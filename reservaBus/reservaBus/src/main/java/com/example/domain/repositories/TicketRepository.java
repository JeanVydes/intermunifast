package com.example.domain.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.domain.entities.Ticket;
import com.example.domain.enums.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByTripIdAndStatus(Long tripId, TicketStatus status);

    List<Ticket> findByAccountId(Long accountId);

    List<Ticket> findBySeatNumber(String seatNumber);

    List<Ticket> findByAccountIdAndSeatNumber(Long accountId, String seatNumber);

    Ticket findByQrCode(String qrCode);

    List<Ticket> findByStatusIn(List<TicketStatus> statuses);

    List<Ticket> findByFromStopIdAndToStopIdAndStatus(Long fromStopId, Long toStopId, TicketStatus status);

    @Query("SELECT DISTINCT t.paymentMethod FROM Ticket t WHERE t.tripId = :tripId")
    List<String> findPaymentMethodsUsedInTrip(@Param("tripId") Long tripId);

}
