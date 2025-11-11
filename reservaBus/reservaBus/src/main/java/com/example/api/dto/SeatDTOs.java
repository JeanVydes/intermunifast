package com.example.api.dto;

import com.example.domain.enums.SeatType;

public class SeatDTOs {
    public record CreateSeatRequest(
            String number,
            SeatType type,
            Long busId) implements java.io.Serializable {
    }

    public record UpdateSeatRequest(
            String number,
            SeatType type,
            Long busId) implements java.io.Serializable {
    }

    public record SeatResponse(
            Long id,
            String number,
            SeatType type,
            Long busId) implements java.io.Serializable {
    }
}
