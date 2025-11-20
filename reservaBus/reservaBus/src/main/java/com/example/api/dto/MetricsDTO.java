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
    public static class CancellationMetrics {
        private Long totalCancellations;
        private Double cancellationRate;
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
    public static class DashboardMetrics {
        private CancellationMetrics cancellations;
        private RevenueMetrics revenue;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }
}
