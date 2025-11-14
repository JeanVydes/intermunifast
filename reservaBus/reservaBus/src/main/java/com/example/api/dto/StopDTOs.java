package com.example.api.dto;

import java.util.Optional;

public class StopDTOs {
        public record CreateStopRequest(
                        String name,
                        Integer sequence,
                        Double latitude,
                        Double longitude,
                        Long routeId) implements java.io.Serializable {
        }

        public record UpdateStopRequest(
                        Optional<String> name,
                        Optional<Integer> sequence,
                        Optional<Double> latitude,
                        Optional<Double> longitude,
                        Optional<Long> routeId) implements java.io.Serializable {
        }

        public record StopResponse(
                        Long id,
                        String name,
                        Integer sequence,
                        Double latitude,
                        Double longitude,
                        Long routeId) implements java.io.Serializable {
        }
}