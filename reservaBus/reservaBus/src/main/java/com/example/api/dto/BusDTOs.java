package com.example.api.dto;

import java.util.List;

public class BusDTOs {
    public record CreateBusRequest(String plate, Integer capacity, List<AmenityDTOs.AmenityResponse> amenities)
            implements java.io.Serializable {
    }

    public record UpdateBusRequest(
        String plate,
        Integer capacity,
        List<AmenityDTOs.AmenityResponse> amenities,
        String status
    ) implements java.io.Serializable {}

    public record BusResponse(
        Long id,
        String plate,
        Integer capacity,
        List<AmenityDTOs.AmenityResponse> amenities,
        String status
    ) implements java.io.Serializable {}
}
