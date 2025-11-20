package com.example.metrics;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.api.dto.MetricsDTO;
import com.example.domain.repositories.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final TicketRepository ticketRepository;

    public MetricsDTO.DashboardMetrics getDashboardMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        long periodDays = java.time.Duration.between(startDate, endDate).toDays();
        LocalDateTime previousStart = startDate.minusDays(periodDays);
        LocalDateTime previousEnd = startDate;

        Double totalRevenue = ticketRepository.getTotalRevenue(startDate, endDate);
        Double previousRevenue = ticketRepository.getTotalRevenue(previousStart, previousEnd);
        Long totalTickets = ticketRepository.countConfirmedTickets(startDate, endDate);
        Long totalCancellations = ticketRepository.countCancelledTickets(startDate, endDate);
        Long previousTickets = ticketRepository.countConfirmedTickets(previousStart, previousEnd);
        Long previousCancellations = ticketRepository.countCancelledTickets(previousStart, previousEnd);

        totalRevenue = totalRevenue != null ? totalRevenue : 0.0;
        previousRevenue = previousRevenue != null ? previousRevenue : 0.0;
        totalTickets = totalTickets != null ? totalTickets : 0L;
        totalCancellations = totalCancellations != null ? totalCancellations : 0L;
        previousTickets = previousTickets != null ? previousTickets : 0L;
        previousCancellations = previousCancellations != null ? previousCancellations : 0L;

        double revenueChange = previousRevenue > 0 ? ((totalRevenue - previousRevenue) / previousRevenue * 100) : 0.0;
        double avgTicketPrice = totalTickets > 0 ? totalRevenue / totalTickets : 0.0;

        long totalSales = totalTickets + totalCancellations;
        long previousSales = previousTickets + previousCancellations;
        double cancellationRate = totalSales > 0 ? (totalCancellations * 100.0 / totalSales) : 0.0;
        double previousCancellationRate = previousSales > 0 ? (previousCancellations * 100.0 / previousSales) : 0.0;
        double cancellationChange = previousCancellationRate > 0
                ? ((cancellationRate - previousCancellationRate) / previousCancellationRate * 100)
                : 0.0;

        return MetricsDTO.DashboardMetrics.builder()
                .revenue(MetricsDTO.RevenueMetrics.builder()
                        .totalRevenue(Math.round(totalRevenue * 100.0) / 100.0)
                        .previousPeriodRevenue(Math.round(previousRevenue * 100.0) / 100.0)
                        .changePercentage(Math.round(revenueChange * 100.0) / 100.0)
                        .totalTicketsSold(totalTickets)
                        .averageTicketPrice(Math.round(avgTicketPrice * 100.0) / 100.0)
                        .build())
                .cancellations(MetricsDTO.CancellationMetrics.builder()
                        .totalCancellations(totalCancellations)
                        .cancellationRate(Math.round(cancellationRate * 100.0) / 100.0)
                        .previousPeriodRate(Math.round(previousCancellationRate * 100.0) / 100.0)
                        .changePercentage(Math.round(cancellationChange * 100.0) / 100.0)
                        .build())
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
