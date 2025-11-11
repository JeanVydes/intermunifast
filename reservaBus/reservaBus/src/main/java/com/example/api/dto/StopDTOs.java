package com.example.api.dto;

public class StopDTOs {
    public record CreateStopRequest(
            String name,
            Integer sequence,
            Double latitude,
            Double longitude,
            Long routeId
    ) implements java.io.Serializable {}   

    public record UpdateStopRequest(
            String name,
            Integer sequence,
            Double latitude,
            Double longitude,
            Long routeId
    ) implements java.io.Serializable {}

    public record StopResponse(
            Long id,
            String name,
            Integer sequence,
            Double latitude,
            Double longitude,
            Long routeId
    ) implements java.io.Serializable {}
}