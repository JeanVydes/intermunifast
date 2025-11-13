package com.example.api.dto;

import java.time.LocalDateTime;
import java.util.Optional;

public class SeatHoldDTOs {
        public record CreateSeatHoldRequest(
                        String seatNumber,
                        Long tripId,
                        Long fromStopId,
                        Long toStopId,
                        LocalDateTime expiresAt) implements java.io.Serializable {
        }

        public record UpdateSeatHoldRequest(
                        Optional<String> seatNumber,
                        Optional<Long> tripId,
                        Optional<Long> fromStopId,
                        Optional<Long> toStopId,
                        Optional<LocalDateTime> expiresAt) implements java.io.Serializable {
        }

        public record SeatHoldResponse(
                        Long id,
                        String seatNumber,
                        Long tripId,
                        Long fromStopId,
                        Long toStopId,
                        LocalDateTime expiresAt,
                        Long createdAt) implements java.io.Serializable {
        }
}
