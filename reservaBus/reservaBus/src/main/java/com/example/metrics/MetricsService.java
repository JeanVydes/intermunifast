package com.example.metrics;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

import com.example.api.dto.MetricsDTO;
import com.example.domain.enums.TicketStatus;
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

                // Convert LocalDateTime to epoch millis (timestamp format used in database)
                Long startMillis = startDate.toInstant(ZoneOffset.UTC).toEpochMilli();
                Long endMillis = endDate.toInstant(ZoneOffset.UTC).toEpochMilli();
                Long prevStartMillis = previousStart.toInstant(ZoneOffset.UTC).toEpochMilli();
                Long prevEndMillis = previousEnd.toInstant(ZoneOffset.UTC).toEpochMilli();

                Double totalRevenue = ticketRepository.getTotalRevenue(TicketStatus.CONFIRMED, startMillis, endMillis);
                Double previousRevenue = ticketRepository.getTotalRevenue(TicketStatus.CONFIRMED, prevStartMillis,
                                prevEndMillis);
                Long totalTickets = ticketRepository.countConfirmedTickets(TicketStatus.CONFIRMED, startMillis,
                                endMillis);
                Long totalCancellations = ticketRepository.countCancelledTickets(TicketStatus.CANCELLED, startMillis,
                                endMillis);
                Long previousTickets = ticketRepository.countConfirmedTickets(TicketStatus.CONFIRMED, prevStartMillis,
                                prevEndMillis);
                Long previousCancellations = ticketRepository.countCancelledTickets(TicketStatus.CANCELLED,
                                prevStartMillis,
                                prevEndMillis);

                totalRevenue = totalRevenue != null ? totalRevenue : 0.0;
                previousRevenue = previousRevenue != null ? previousRevenue : 0.0;
                totalTickets = totalTickets != null ? totalTickets : 0L;
                totalCancellations = totalCancellations != null ? totalCancellations : 0L;
                previousTickets = previousTickets != null ? previousTickets : 0L;
                previousCancellations = previousCancellations != null ? previousCancellations : 0L;

                double revenueChange = previousRevenue > 0 ? ((totalRevenue - previousRevenue) / previousRevenue * 100)
                                : 0.0;
                double avgTicketPrice = totalTickets > 0 ? totalRevenue / totalTickets : 0.0;

                long totalSales = totalTickets + totalCancellations;
                long previousSales = previousTickets + previousCancellations;
                double cancellationRate = totalSales > 0 ? (totalCancellations * 100.0 / totalSales) : 0.0;
                double previousCancellationRate = previousSales > 0 ? (previousCancellations * 100.0 / previousSales)
                                : 0.0;
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
                                                .previousPeriodRate(
                                                                Math.round(previousCancellationRate * 100.0) / 100.0)
                                                .changePercentage(Math.round(cancellationChange * 100.0) / 100.0)
                                                .build())
                                .occupation(MetricsDTO.OccupationMetrics.builder()
                                                .averageOccupation(0.0)
                                                .previousPeriodOccupation(0.0)
                                                .changePercentage(0.0)
                                                .totalTrips(0L)
                                                .totalSeatsOffered(0L)
                                                .totalSeatsSold(totalTickets)
                                                .build())
                                .punctuality(MetricsDTO.PunctualityMetrics.builder()
                                                .punctualityRate(0.0)
                                                .totalTripsCompleted(0L)
                                                .totalTripsOnTime(0L)
                                                .totalTripsDelayed(0L)
                                                .previousPeriodRate(0.0)
                                                .changePercentage(0.0)
                                                .build())
                                .startDate(startDate)
                                .endDate(endDate)
                                .build();
        }
}
