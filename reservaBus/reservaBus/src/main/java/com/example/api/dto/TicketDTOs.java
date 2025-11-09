package com.example.api.dto;

import com.example.domain.enums.PaymentMethod;

public class TicketDTOs {
    public record CreateTicketRequest(
            String seatNumber,
            Long tripId,
            Long fromStopId,
            Long toStopId,
            PaymentMethod paymentMethod,
            // stripe payment intent id to be associated with the ticket
            String paymentIntentId
    ) implements java.io.Serializable {}

    public record UpdateTicketRequest(
            String seatNumber,
            Long tripId,
            Long fromStopId,
            Long toStopId,
            PaymentMethod paymentMethod,
            String paymentIntentId
    ) implements java.io.Serializable {}

    public record TicketResponse(
            Long id,
            String seatNumber,
            Long tripId,
            Long fromStopId,
            Long toStopId,
            PaymentMethod paymentMethod,
            String paymentIntentId
    ) implements java.io.Serializable {}
}
