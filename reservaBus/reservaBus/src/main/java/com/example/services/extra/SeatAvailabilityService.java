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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatAvailabilityService {

        private final TicketRepository ticketRepository;
        private final SeatHoldRepository seatHoldRepository;

        public boolean isSeatAvailable(Long tripId, String seatNumber, Stop fromStop, Stop toStop) {
                return isSeatAvailable(tripId, seatNumber, fromStop, toStop, null);
        }

        public boolean isSeatAvailableExcludingHold(Long tripId, String seatNumber, Stop fromStop, Stop toStop,
                        Long excludeHoldId) {
                return isSeatAvailable(tripId, seatNumber, fromStop, toStop, excludeHoldId);
        }

        public String getAvailabilityConflictReason(Long tripId, String seatNumber, Stop fromStop, Stop toStop) {
                Integer fromSeq = fromStop != null ? fromStop.getSequence() : Integer.MIN_VALUE;
                Integer toSeq = toStop != null ? toStop.getSequence() : Integer.MAX_VALUE;

                if (fromStop != null && toStop != null && fromStop.getSequence() >= toStop.getSequence()) {
                        return "Invalid stop sequence";
                }

                List<Ticket> overlappingTickets = ticketRepository.findOverlappingSoldTickets(tripId, seatNumber,
                                fromSeq, toSeq);
                if (!overlappingTickets.isEmpty()) {
                        Ticket conflict = overlappingTickets.get(0);
                        String from = conflict.getFromStop() != null ? conflict.getFromStop().getName() : "origin";
                        String to = conflict.getToStop() != null ? conflict.getToStop().getName() : "destination";
                        return "Already sold: %s -> %s".formatted(from, to);
                }

                List<SeatHold> overlappingHolds = seatHoldRepository.findOverlappingActiveHolds(tripId, seatNumber,
                                fromSeq, toSeq, LocalDateTime.now());
                if (!overlappingHolds.isEmpty()) {
                        SeatHold conflict = overlappingHolds.get(0);
                        String from = conflict.getFromStop() != null ? conflict.getFromStop().getName() : "origin";
                        String to = conflict.getToStop() != null ? conflict.getToStop().getName() : "destination";
                        return "On hold: %s -> %s (expires %s)".formatted(from, to, conflict.getExpiresAt());
                }

                return null;
        }

        private boolean isSeatAvailable(Long tripId, String seatNumber, Stop fromStop, Stop toStop,
                        Long excludeHoldId) {
                Integer fromSeq = fromStop != null ? fromStop.getSequence() : Integer.MIN_VALUE;
                Integer toSeq = toStop != null ? toStop.getSequence() : Integer.MAX_VALUE;

                if (fromStop != null && toStop != null && fromStop.getSequence() >= toStop.getSequence()) {
                        throw new IllegalArgumentException("Invalid stop sequence: fromStop must be before toStop");
                }

                List<Ticket> overlappingTickets = ticketRepository.findOverlappingSoldTickets(tripId, seatNumber,
                                fromSeq, toSeq);
                if (!overlappingTickets.isEmpty()) {
                        return false;
                }

                List<SeatHold> overlappingHolds = seatHoldRepository.findOverlappingActiveHolds(tripId, seatNumber,
                                fromSeq, toSeq, LocalDateTime.now());

                if (excludeHoldId != null) {
                        overlappingHolds = overlappingHolds.stream()
                                        .filter(hold -> !hold.getId().equals(excludeHoldId))
                                        .toList();
                }

                return overlappingHolds.isEmpty();
        }
}
