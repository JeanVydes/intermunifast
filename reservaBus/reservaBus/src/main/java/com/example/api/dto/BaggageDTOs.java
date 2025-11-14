package com.example.api.dto;

import java.math.BigDecimal;

public class BaggageDTOs {
    public record CreateBaggageRequest(Double weightKg, String tagCode, Long ticketId) implements java.io.Serializable {
    }

    public record UpdateBaggageRequest(Double weightKg, String tagCode, Long ticketId) implements java.io.Serializable {
    }

    public record BaggageResponse(Long id, Double weightKg, BigDecimal fee, String tagCode, Long ticketId)
            implements java.io.Serializable {
    }
}
