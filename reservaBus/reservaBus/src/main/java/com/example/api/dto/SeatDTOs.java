package com.example.api.dto;

import java.time.LocalDateTime;
import java.util.Optional;

import com.example.domain.enums.SeatType;

public class SeatDTOs {
        public record CreateSeatRequest(
                        String number,
                        SeatType type,
                        Long busId) implements java.io.Serializable {
        }

        public record UpdateSeatRequest(
                        Optional<String> number,
                        Optional<SeatType> type,
                        Optional<Long> busId) implements java.io.Serializable {
        }

        public record SeatResponse(
                        Long id,
                        String number,
                        SeatType type,
                        Long busId) implements java.io.Serializable {
        }

        public record SeatReponseFull(
                        Long id,
                        String number,
                        SeatType type,
                        Long busId,

                        // Hold details
                        Optional<Long> seatHoldId,
                        Optional<LocalDateTime> holdExpiresAt,

                        // Ticket details
                        Optional<Long> ticketId,
                        Optional<StopDTOs.StopResponse> fromStop,
                        Optional<StopDTOs.StopResponse> toStop) implements java.io.Serializable {
        }
}
