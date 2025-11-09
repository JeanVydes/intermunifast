package com.example.api.dto;

public class SeatHoldDTOs {
    public record CreateSeatHoldRequest(
            String seatNumber,
            Long tripId
    ) implements java.io.Serializable {}

    public record SeatHoldResponse(
            Long id,
            String seatNumber,
            Long tripId
    ) implements java.io.Serializable {}
}
