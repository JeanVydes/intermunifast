package com.example.api.dto;

import java.math.BigDecimal;

public class BaggageDTOs {
    public record CreateBaggageRequest(Integer weightKg, String tagCode, Long ticketId) implements java.io.Serializable {}
    public record UpdateBaggageRequest(Integer weightKg, String tagCode, Long ticketId) implements java.io.Serializable {}
    public record BaggageResponse(Long id, Integer weightKg, BigDecimal fee, String tagCode, Long ticketId) implements java.io.Serializable {}    
}
