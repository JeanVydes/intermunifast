package com.example.services.extra;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.domain.entities.SeatHold;
import com.example.domain.entities.Stop;
import com.example.domain.entities.Ticket;
import com.example.domain.repositories.SeatHoldRepository;
import com.example.domain.repositories.TicketRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SeatAvailabilityService {

    private final TicketRepository ticketRepository;
    private final SeatHoldRepository seatHoldRepository;

    private static final String ORIGIN_PLACEHOLDER = "origin";
    private static final String DESTINATION_PLACEHOLDER = "destination";

    public boolean isSeatAvailable(
            Long tripId,
            String seatNumber,
            Stop fromStop,
            Stop toStop) {

        // Handle null stops: use MIN/MAX sequence for full trip availability check
        Integer fromSequence = fromStop != null ? fromStop.getSequence() : Integer.MIN_VALUE;
        Integer toSequence = toStop != null ? toStop.getSequence() : Integer.MAX_VALUE;

        // 1. Validar que la secuencia de paradas sea válida (solo si ambos están
        // presentes)
        if (fromStop != null && toStop != null && fromStop.getSequence() >= toStop.getSequence()) {
            throw new IllegalArgumentException(
                    "Invalid stop sequence: fromStop (%d) must be before toStop (%d)"
                            .formatted(fromStop.getSequence(), toStop.getSequence()));
        }

        // 2. Verificar tickets vendidos que solapen con este tramo
        List<Ticket> overlappingTickets = ticketRepository.findOverlappingSoldTickets(
                tripId,
                seatNumber,
                fromSequence,
                toSequence);

        if (!overlappingTickets.isEmpty()) {
            log.debug("Seat {} in trip {} is not available: {} overlapping sold tickets found",
                    seatNumber, tripId, overlappingTickets.size());
            return false;
        }

        // 3. Verificar holds activos que solapen con este tramo
        List<SeatHold> overlappingHolds = seatHoldRepository.findOverlappingActiveHolds(
                tripId,
                seatNumber,
                fromSequence,
                toSequence,
                LocalDateTime.now());

        if (!overlappingHolds.isEmpty()) {
            log.debug("Seat {} in trip {} is not available: {} overlapping active holds found",
                    seatNumber, tripId, overlappingHolds.size());
            return false;
        }

        // 4. Asiento disponible
        String fromStopName = fromStop != null ? fromStop.getName() : ORIGIN_PLACEHOLDER;
        String toStopName = toStop != null ? toStop.getName() : DESTINATION_PLACEHOLDER;
        log.debug("Seat {} in trip {} is available for route segment {} -> {}",
                seatNumber, tripId, fromStopName, toStopName);
        return true;
    }

    public boolean isSeatAvailableExcludingHold(
            Long tripId,
            String seatNumber,
            Stop fromStop,
            Stop toStop,
            Long excludeHoldId) {

        // Handle null stops: use MIN/MAX sequence for full trip availability check
        Integer fromSequence = fromStop != null ? fromStop.getSequence() : Integer.MIN_VALUE;
        Integer toSequence = toStop != null ? toStop.getSequence() : Integer.MAX_VALUE;

        // Validar secuencia (solo si ambos están presentes)
        if (fromStop != null && toStop != null && fromStop.getSequence() >= toStop.getSequence()) {
            throw new IllegalArgumentException(
                    "Invalid stop sequence: fromStop (%d) must be before toStop (%d)"
                            .formatted(fromStop.getSequence(), toStop.getSequence()));
        }

        // Verificar tickets vendidos
        List<Ticket> overlappingTickets = ticketRepository.findOverlappingSoldTickets(
                tripId,
                seatNumber,
                fromSequence,
                toSequence);

        if (!overlappingTickets.isEmpty()) {
            return false;
        }

        // Verificar holds activos, excluyendo el especificado
        List<SeatHold> overlappingHolds = seatHoldRepository.findOverlappingActiveHolds(
                tripId,
                seatNumber,
                fromSequence,
                toSequence,
                LocalDateTime.now());

        // Filtrar el hold excluido
        boolean hasConflict = overlappingHolds.stream()
                .anyMatch(hold -> !hold.getId().equals(excludeHoldId));

        return !hasConflict;
    }

    public String getAvailabilityConflictReason(
            Long tripId,
            String seatNumber,
            Stop fromStop,
            Stop toStop) {

        // Handle null stops: use MIN/MAX sequence for full trip availability check
        Integer fromSequence = fromStop != null ? fromStop.getSequence() : Integer.MIN_VALUE;
        Integer toSequence = toStop != null ? toStop.getSequence() : Integer.MAX_VALUE;

        if (fromStop != null && toStop != null && fromStop.getSequence() >= toStop.getSequence()) {
            return "Invalid stop sequence: origin must be before destination";
        }

        List<Ticket> overlappingTickets = ticketRepository.findOverlappingSoldTickets(
                tripId,
                seatNumber,
                fromSequence,
                toSequence);

        if (!overlappingTickets.isEmpty()) {
            Ticket conflict = overlappingTickets.get(0);
            String fromName = conflict.getFromStop() != null ? conflict.getFromStop().getName()
                    : ORIGIN_PLACEHOLDER;
            String toName = conflict.getToStop() != null ? conflict.getToStop().getName()
                    : DESTINATION_PLACEHOLDER;
            return "Seat already sold for overlapping route segment: %s -> %s"
                    .formatted(fromName, toName);
        }

        List<SeatHold> overlappingHolds = seatHoldRepository.findOverlappingActiveHolds(
                tripId,
                seatNumber,
                fromSequence,
                toSequence,
                LocalDateTime.now());

        if (!overlappingHolds.isEmpty()) {
            SeatHold conflict = overlappingHolds.get(0);
            String fromName = conflict.getFromStop() != null ? conflict.getFromStop().getName()
                    : ORIGIN_PLACEHOLDER;
            String toName = conflict.getToStop() != null ? conflict.getToStop().getName()
                    : DESTINATION_PLACEHOLDER;
            return "Seat currently on hold for overlapping route segment: %s -> %s (expires at %s)"
                    .formatted(fromName, toName, conflict.getExpiresAt());
        }

        return null; // Disponible
    }
}
