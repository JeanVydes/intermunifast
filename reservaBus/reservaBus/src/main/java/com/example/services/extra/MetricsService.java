package com.example.services.extra;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.api.dto.MetricsDTO;
import com.example.domain.entities.Ticket;
import com.example.domain.entities.Trip;
import com.example.domain.enums.TicketStatus;
import com.example.domain.repositories.TicketRepository;
import com.example.domain.repositories.TripLogRepository;
import com.example.domain.repositories.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final TripLogRepository tripLogRepository;
    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;

    public MetricsDTO.DashboardMetrics getDashboardMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        // Calculate previous period for comparison
        long periodLength = java.time.Duration.between(startDate, endDate).toDays();
        LocalDateTime previousStart = startDate.minusDays(periodLength);
        LocalDateTime previousEnd = startDate;

        return MetricsDTO.DashboardMetrics.builder()
                .cancellations(getCancellationMetrics(startDate, endDate, previousStart, previousEnd))
                .revenue(getRevenueMetrics(startDate, endDate, previousStart, previousEnd))
                .occupation(getOccupationMetrics(startDate, endDate, previousStart, previousEnd))
                .punctuality(getPunctualityMetrics(startDate, endDate, previousStart, previousEnd))
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    private MetricsDTO.CancellationMetrics getCancellationMetrics(LocalDateTime startDate, LocalDateTime endDate,
            LocalDateTime previousStart, LocalDateTime previousEnd) {
        // Use real-time ticket data
        Long totalCancellations = ticketRepository.countCancelledTickets(startDate, endDate);
        Long totalSeatsSold = ticketRepository.countConfirmedTickets(startDate, endDate);

        Long previousCancellations = ticketRepository.countCancelledTickets(previousStart, previousEnd);
        Long previousSeatsSold = ticketRepository.countConfirmedTickets(previousStart, previousEnd);

        if (totalCancellations == null)
            totalCancellations = 0L;
        if (totalSeatsSold == null)
            totalSeatsSold = 0L;
        if (previousCancellations == null)
            previousCancellations = 0L;
        if (previousSeatsSold == null)
            previousSeatsSold = 0L;

        double cancellationRate = (totalSeatsSold + totalCancellations) > 0
                ? (totalCancellations * 100.0 / (totalSeatsSold + totalCancellations))
                : 0.0;
        double previousRate = (previousSeatsSold + previousCancellations) > 0
                ? (previousCancellations * 100.0 / (previousSeatsSold + previousCancellations))
                : 0.0;
        double changePercentage = previousRate > 0 ? ((cancellationRate - previousRate) / previousRate * 100) : 0.0;

        return MetricsDTO.CancellationMetrics.builder()
                .totalCancellations(totalCancellations)
                .cancellationRate(Math.round(cancellationRate * 100.0) / 100.0)
                .previousPeriodRate(Math.round(previousRate * 100.0) / 100.0)
                .changePercentage(Math.round(changePercentage * 100.0) / 100.0)
                .build();
    }

    private MetricsDTO.RevenueMetrics getRevenueMetrics(LocalDateTime startDate, LocalDateTime endDate,
            LocalDateTime previousStart, LocalDateTime previousEnd) {
        // Use real-time ticket data
        Double totalRevenue = ticketRepository.getTotalRevenue(startDate, endDate);
        Double previousRevenue = ticketRepository.getTotalRevenue(previousStart, previousEnd);
        Long totalTickets = ticketRepository.countConfirmedTickets(startDate, endDate);

        if (totalRevenue == null)
            totalRevenue = 0.0;
        if (previousRevenue == null)
            previousRevenue = 0.0;
        if (totalTickets == null)
            totalTickets = 0L;

        double changePercentage = previousRevenue > 0 ? ((totalRevenue - previousRevenue) / previousRevenue * 100)
                : 0.0;
        double avgPrice = totalTickets > 0 ? totalRevenue / totalTickets : 0.0;

        return MetricsDTO.RevenueMetrics.builder()
                .totalRevenue(Math.round(totalRevenue * 100.0) / 100.0)
                .previousPeriodRevenue(Math.round(previousRevenue * 100.0) / 100.0)
                .changePercentage(Math.round(changePercentage * 100.0) / 100.0)
                .totalTicketsSold(totalTickets)
                .averageTicketPrice(Math.round(avgPrice * 100.0) / 100.0)
                .build();
    }

    private MetricsDTO.OccupationMetrics getOccupationMetrics(LocalDateTime startDate, LocalDateTime endDate,
            LocalDateTime previousStart, LocalDateTime previousEnd) {
        // Get trips in the period
        List<Trip> trips = tripRepository.findTripsByPeriod(startDate, endDate);
        List<Trip> previousTrips = tripRepository.findTripsByPeriod(previousStart, previousEnd);

        // Calculate occupation from real-time ticket data
        long totalSeatsOffered = 0;
        long totalSeatsSold = 0;
        for (Trip trip : trips) {
            totalSeatsOffered += trip.getBus().getCapacity();
            long soldSeats = trip.getTickets().stream()
                    .filter(t -> t.getStatus() == TicketStatus.CONFIRMED
                            || t.getStatus() == TicketStatus.PENDING_APPROVAL)
                    .count();
            totalSeatsSold += soldSeats;
        }

        // Previous period
        long prevSeatsOffered = 0;
        long prevSeatsSold = 0;
        for (Trip trip : previousTrips) {
            prevSeatsOffered += trip.getBus().getCapacity();
            long soldSeats = trip.getTickets().stream()
                    .filter(t -> t.getStatus() == TicketStatus.CONFIRMED
                            || t.getStatus() == TicketStatus.PENDING_APPROVAL)
                    .count();
            prevSeatsSold += soldSeats;
        }

        double avgOccupation = totalSeatsOffered > 0 ? (totalSeatsSold * 100.0 / totalSeatsOffered) : 0.0;
        double previousOccupation = prevSeatsOffered > 0 ? (prevSeatsSold * 100.0 / prevSeatsOffered) : 0.0;
        double changePercentage = previousOccupation > 0
                ? ((avgOccupation - previousOccupation) / previousOccupation * 100)
                : 0.0;

        return MetricsDTO.OccupationMetrics.builder()
                .averageOccupation(Math.round(avgOccupation * 100.0) / 100.0)
                .previousPeriodOccupation(Math.round(previousOccupation * 100.0) / 100.0)
                .changePercentage(Math.round(changePercentage * 100.0) / 100.0)
                .totalTrips((long) trips.size())
                .totalSeatsOffered(totalSeatsOffered)
                .totalSeatsSold(totalSeatsSold)
                .build();
    }

    private MetricsDTO.PunctualityMetrics getPunctualityMetrics(LocalDateTime startDate, LocalDateTime endDate,
            LocalDateTime previousStart, LocalDateTime previousEnd) {
        // Punctuality can only be calculated from TripLogs (when trips complete)
        // If no TripLogs exist, show default values
        Long totalTrips = tripLogRepository.countTotalTrips(startDate, endDate);
        Long onTimeTrips = tripLogRepository.countOnTimeTrips(startDate, endDate);
        Long previousTotal = tripLogRepository.countTotalTrips(previousStart, previousEnd);
        Long previousOnTime = tripLogRepository.countOnTimeTrips(previousStart, previousEnd);

        if (totalTrips == null)
            totalTrips = 0L;
        if (onTimeTrips == null)
            onTimeTrips = 0L;
        if (previousTotal == null)
            previousTotal = 0L;
        if (previousOnTime == null)
            previousOnTime = 0L;

        // If no TripLogs exist, use scheduled trips count
        if (totalTrips == 0) {
            List<Trip> trips = tripRepository.findTripsByPeriod(startDate, endDate);
            totalTrips = (long) trips.size();
            onTimeTrips = totalTrips; // Assume all scheduled trips are on time
        }

        long delayedTrips = totalTrips - onTimeTrips;
        double punctualityRate = totalTrips > 0 ? (onTimeTrips * 100.0 / totalTrips) : 0.0;
        double previousRate = previousTotal > 0 ? (previousOnTime * 100.0 / previousTotal) : 0.0;
        double changePercentage = previousRate > 0 ? ((punctualityRate - previousRate) / previousRate * 100) : 0.0;

        return MetricsDTO.PunctualityMetrics.builder()
                .punctualityRate(Math.round(punctualityRate * 100.0) / 100.0)
                .totalTripsCompleted(totalTrips)
                .totalTripsOnTime(onTimeTrips)
                .totalTripsDelayed(delayedTrips)
                .previousPeriodRate(Math.round(previousRate * 100.0) / 100.0)
                .changePercentage(Math.round(changePercentage * 100.0) / 100.0)
                .build();
    }
}
