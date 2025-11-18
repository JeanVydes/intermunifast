package com.example.api.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MetricsDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricsRequest {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancellationMetrics {
        private Long totalCancellations;
        private Double cancellationRate; // percentage
        private Double previousPeriodRate;
        private Double changePercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueMetrics {
        private Double totalRevenue;
        private Double previousPeriodRevenue;
        private Double changePercentage;
        private Long totalTicketsSold;
        private Double averageTicketPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OccupationMetrics {
        private Double averageOccupation; // percentage
        private Double previousPeriodOccupation;
        private Double changePercentage;
        private Long totalTrips;
        private Long totalSeatsOffered;
        private Long totalSeatsSold;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PunctualityMetrics {
        private Double punctualityRate; // percentage
        private Long totalTripsCompleted;
        private Long totalTripsOnTime;
        private Long totalTripsDelayed;
        private Double previousPeriodRate;
        private Double changePercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardMetrics {
        private CancellationMetrics cancellations;
        private RevenueMetrics revenue;
        private OccupationMetrics occupation;
        private PunctualityMetrics punctuality;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }
}
