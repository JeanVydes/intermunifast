package com.example.api.dto;

import java.util.List;
import java.util.Optional;

import com.example.domain.enums.BusStatus;

public class BusDTOs {
    public record CreateBusRequest(String plate, Integer capacity, List<AmenityDTOs.AmenityResponse> amenities)
            implements java.io.Serializable {
    }

    public record UpdateBusRequest(
            Optional<String> plate,
            Optional<Integer> capacity,
            Optional<List<AmenityDTOs.AmenityResponse>> amenities,
            Optional<BusStatus> status) implements java.io.Serializable {
    }

    public record BusResponse(
            Long id,
            String plate,
            Integer capacity,
            List<AmenityDTOs.AmenityResponse> amenities,
            String status) implements java.io.Serializable {
    }
}
