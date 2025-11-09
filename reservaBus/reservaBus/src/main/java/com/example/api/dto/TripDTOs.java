package com.example.api.dto;

public class TripDTOs {
    public record CreateTripRequest(
            Long routeId,
            Long busId
    ) implements java.io.Serializable {}

    public record UpdateTripRequest(
            Long routeId,
            Long busId
    ) implements java.io.Serializable {}

    public record TripResponse(
            Long id,
            Long routeId,
            Long busId
    ) implements java.io.Serializable {}
}
