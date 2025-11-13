package com.example.services.definitions;

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

/**
 * Servicio para validar la disponibilidad de asientos considerando:
 * - Tickets vendidos (SOLD)
 * - Holds activos (no expirados)
 * - Solapamiento de tramos (paradas intermedias)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SeatAvailabilityService {

    private final TicketRepository ticketRepository;
    private final SeatHoldRepository seatHoldRepository;

    /**
     * Valida si un asiento está disponible para el tramo especificado.
     * 
     * @param tripId     ID del viaje
     * @param seatNumber Número de asiento
     * @param fromStop   Parada de origen
     * @param toStop     Parada de destino
     * @return true si está disponible, false si hay conflicto
     * @throws IllegalArgumentException si la secuencia de paradas es inválida
     */
    public boolean isSeatAvailable(
            Long tripId,
            String seatNumber,
            Stop fromStop,
            Stop toStop) {

        // 1. Validar que la secuencia de paradas sea válida
        if (fromStop.getSequence() >= toStop.getSequence()) {
            throw new IllegalArgumentException(
                    "Invalid stop sequence: fromStop (%d) must be before toStop (%d)"
                            .formatted(fromStop.getSequence(), toStop.getSequence()));
        }

        // 2. Verificar tickets vendidos que solapen con este tramo
        List<Ticket> overlappingTickets = ticketRepository.findOverlappingSoldTickets(
                tripId,
                seatNumber,
                fromStop.getSequence(),
                toStop.getSequence());

        if (!overlappingTickets.isEmpty()) {
            log.debug("Seat {} in trip {} is not available: {} overlapping sold tickets found",
                    seatNumber, tripId, overlappingTickets.size());
            return false;
        }

        // 3. Verificar holds activos que solapen con este tramo
        List<SeatHold> overlappingHolds = seatHoldRepository.findOverlappingActiveHolds(
                tripId,
                seatNumber,
                fromStop.getSequence(),
                toStop.getSequence(),
                LocalDateTime.now());

        if (!overlappingHolds.isEmpty()) {
            log.debug("Seat {} in trip {} is not available: {} overlapping active holds found",
                    seatNumber, tripId, overlappingHolds.size());
            return false;
        }

        // 4. Asiento disponible
        log.debug("Seat {} in trip {} is available for route segment {} -> {}",
                seatNumber, tripId, fromStop.getName(), toStop.getName());
        return true;
    }

    /**
     * Valida si un asiento está disponible, excluyendo un hold específico
     * (útil cuando se está actualizando un hold existente).
     * 
     * @param tripId        ID del viaje
     * @param seatNumber    Número de asiento
     * @param fromStop      Parada de origen
     * @param toStop        Parada de destino
     * @param excludeHoldId ID del hold a excluir de la validación
     * @return true si está disponible, false si hay conflicto
     */
    public boolean isSeatAvailableExcludingHold(
            Long tripId,
            String seatNumber,
            Stop fromStop,
            Stop toStop,
            Long excludeHoldId) {

        // Validar secuencia
        if (fromStop.getSequence() >= toStop.getSequence()) {
            throw new IllegalArgumentException(
                    "Invalid stop sequence: fromStop (%d) must be before toStop (%d)"
                            .formatted(fromStop.getSequence(), toStop.getSequence()));
        }

        // Verificar tickets vendidos
        List<Ticket> overlappingTickets = ticketRepository.findOverlappingSoldTickets(
                tripId,
                seatNumber,
                fromStop.getSequence(),
                toStop.getSequence());

        if (!overlappingTickets.isEmpty()) {
            return false;
        }

        // Verificar holds activos, excluyendo el especificado
        List<SeatHold> overlappingHolds = seatHoldRepository.findOverlappingActiveHolds(
                tripId,
                seatNumber,
                fromStop.getSequence(),
                toStop.getSequence(),
                LocalDateTime.now());

        // Filtrar el hold excluido
        boolean hasConflict = overlappingHolds.stream()
                .anyMatch(hold -> !hold.getId().equals(excludeHoldId));

        return !hasConflict;
    }

    /**
     * Obtiene información detallada sobre por qué un asiento no está disponible.
     * 
     * @param tripId     ID del viaje
     * @param seatNumber Número de asiento
     * @param fromStop   Parada de origen
     * @param toStop     Parada de destino
     * @return Mensaje descriptivo del conflicto, o null si está disponible
     */
    public String getAvailabilityConflictReason(
            Long tripId,
            String seatNumber,
            Stop fromStop,
            Stop toStop) {

        if (fromStop.getSequence() >= toStop.getSequence()) {
            return "Invalid stop sequence: origin must be before destination";
        }

        List<Ticket> overlappingTickets = ticketRepository.findOverlappingSoldTickets(
                tripId,
                seatNumber,
                fromStop.getSequence(),
                toStop.getSequence());

        if (!overlappingTickets.isEmpty()) {
            Ticket conflict = overlappingTickets.get(0);
            return "Seat already sold for overlapping route segment: %s -> %s"
                    .formatted(conflict.getFromStop().getName(), conflict.getToStop().getName());
        }

        List<SeatHold> overlappingHolds = seatHoldRepository.findOverlappingActiveHolds(
                tripId,
                seatNumber,
                fromStop.getSequence(),
                toStop.getSequence(),
                LocalDateTime.now());

        if (!overlappingHolds.isEmpty()) {
            SeatHold conflict = overlappingHolds.get(0);
            return "Seat currently on hold for overlapping route segment: %s -> %s (expires at %s)"
                    .formatted(
                            conflict.getFromStop().getName(),
                            conflict.getToStop().getName(),
                            conflict.getExpiresAt());
        }

        return null; // Disponible
    }
}
