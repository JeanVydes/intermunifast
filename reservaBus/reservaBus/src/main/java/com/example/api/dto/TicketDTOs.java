package com.example.api.dto;

import java.util.Optional;

import com.example.domain.enums.FareRulePassengerType;
import com.example.domain.enums.PaymentMethod;

public class TicketDTOs {
        public record CreateTicketRequest(
                        String seatNumber,
                        Long tripId,
                        Optional<Long> fromStopId,
                        Optional<Long> toStopId,
                        PaymentMethod paymentMethod,
                        // stripe payment intent id to be associated with the ticket
                        String paymentIntentId,
                        FareRulePassengerType passengerType) implements java.io.Serializable {
        }

        public record UpdateTicketRequest(
                        String seatNumber,
                        Long tripId,
                        Long fromStopId,
                        Long toStopId,
                        FareRulePassengerType passengerType) implements java.io.Serializable {
        }

        public record TicketResponse(
                        Long id,
                        String seatNumber,
                        Long tripId,
                        Optional<Long> fromStopId,
                        Optional<Long> toStopId,
                        PaymentMethod paymentMethod,
                        String paymentIntentId,
                        FareRulePassengerType passengerType,
                        String status, // CONFIRMED, PENDING_APPROVAL, CANCELLED, NO_SHOW
                        String paymentStatus, // PENDING, COMPLETED, FAILED
                        Double price, // ticket price
                        String qrCode // QR code for validation
        ) implements java.io.Serializable {
        }
}
