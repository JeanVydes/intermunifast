package com.example.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TripDTOs {
    public record CreateTripRequest(
            Long routeId,
            Long busId,
            LocalDateTime departureAt,
            LocalDateTime arrivalAt) implements java.io.Serializable {
    }

    public record UpdateTripRequest(
            Long routeId,
            Long busId,
            LocalDateTime departureAt,
            LocalDateTime arrivalAt) implements java.io.Serializable {
    }

    public record TripResponse(
            Long id,
            Long routeId,
            Long busId,
            LocalDateTime departureAt,
            LocalDateTime arrivalAt) implements java.io.Serializable {
    }

    public record TripSearchResponse(
            List<TripResponse> trips,
            List<RouteDTOs.RouteResponse> routes,
            List<StopDTOs.StopResponse> stops) implements java.io.Serializable {
    }
}
