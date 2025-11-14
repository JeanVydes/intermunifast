package com.example.api.dto;

import java.io.Serializable;
import java.util.Optional;

public class RouteDTOs {
    public record CreateRouteRequest(
            String code,
            String name,
            String origin,
            String destination,
            Integer durationMinutes,
            Double distanceKm,
            Double pricePerKm) implements Serializable {
    }

    public record UpdateRouteRequest(
            Optional<String> code,
            Optional<String> name,
            Optional<String> origin,
            Optional<String> destination,
            Optional<Integer> durationMinutes,
            Optional<Double> distanceKm,
            Optional<Double> pricePerKm) implements Serializable {
    }

    public record RouteResponse(
            Long id,
            String code,
            String name,
            String origin,
            String destination,
            Integer durationMinutes,
            Double distanceKm,
            Double pricePerKm) implements Serializable {
    }
}
