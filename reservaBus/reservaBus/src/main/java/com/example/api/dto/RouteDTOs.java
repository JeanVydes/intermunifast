package com.example.api.dto;

import java.io.Serializable;

public class RouteDTOs {
    public record CreateRouteRequest(
        String code,
        String name,
        String origin,
        String destination,
        Integer durationMinutes,
        Double distanceKm,
        Double pricePerKm
    ) implements Serializable {}

    public record UpdateRouteRequest(
        String code,
        String name,
        String origin,
        String destination,
        Integer durationMinutes,
        Double distanceKm,
        Double pricePerKm
    ) implements Serializable {}

    public record RouteResponse(
        Long id,
        String code,
        String name,
        String origin,
        String destination,
        Integer durationMinutes,
        Double distanceKm,
        Double pricePerKm
    ) implements Serializable {}
}
