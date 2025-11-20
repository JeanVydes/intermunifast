package com.example.api.dto;

public class AmenityDTOs {
    public record CreateAmenityRequest(
            String name,
            String description) implements java.io.Serializable {
    }

    public record AmenityResponse(Long id, String name, String description) implements java.io.Serializable {
    }
}
