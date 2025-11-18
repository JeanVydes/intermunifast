package com.example.services.watchers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.domain.entities.Parcel;
import com.example.domain.entities.Ticket;
import com.example.domain.entities.Trip;
import com.example.domain.entities.TripLog;
import com.example.domain.enums.ParcelStatus;
import com.example.domain.enums.TicketStatus;
import com.example.domain.enums.TripStatus;
import com.example.domain.repositories.ParcelRepository;
import com.example.domain.repositories.TripLogRepository;
import com.example.domain.repositories.TripRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripWatcher {
    private final TripRepository tripRepository;
    private final ParcelRepository parcelRepository;
    private final TripLogRepository tripLogRepository;

    @Scheduled(fixedRate = 60000) // every minute
    @Transactional
    public void tripMainWorker() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureTime = now.plusMinutes(60);
        List<Trip> startingTrips = tripRepository.findStartingTripsInNextMinutes(now, futureTime);

        for (Trip trip : startingTrips) {
            if (trip.getDepartureAt().isBefore(now.plusMinutes(5))) {
                List<Ticket> tickets = trip.getTickets();

                for (Ticket ticket : tickets) {
                    ticket.setStatus(TicketStatus.NO_SHOW);
                }
                // Starting in 30 minutes
            } else if (trip.getDepartureAt().isBefore(now.plusMinutes(30))) {
                trip.setStatus(TripStatus.BOARDING);
            } else if (trip.getDepartureAt().isBefore(now)) {
                trip.setStatus(TripStatus.DEPARTED);

                // Update parcels to IN_TRANSIT when trip departs
                List<Parcel> parcels = trip.getParcels();
                for (Parcel parcel : parcels) {
                    if (parcel.getStatus() == ParcelStatus.CREATED) {
                        parcel.setStatus(ParcelStatus.IN_TRANSIT);
                        parcelRepository.save(parcel);
                    }
                }
            }
        }

        // Check for arrived trips
        List<Trip> departedTrips = tripRepository.findByStatus(TripStatus.DEPARTED);
        for (Trip trip : departedTrips) {
            if (trip.getArrivalAt().isBefore(now)) {
                trip.setStatus(TripStatus.ARRIVED);
                tripRepository.save(trip);
            }
        }
    }

    /**
     * Check for completed trips and log their metrics
     */
    @Scheduled(fixedRate = 300000) // every 5 minutes
    @Transactional
    public void logCompletedTrips() {
        LocalDateTime now = LocalDateTime.now();
        // Find trips that have arrived
        List<Trip> completedTrips = tripRepository.findByStatus(TripStatus.ARRIVED);

        for (Trip trip : completedTrips) {
            // Check if trip has arrived and hasn't been logged yet
            if (trip.getArrivalAt().isBefore(now)) {
                // Check if this trip has already been logged
                if (tripLogRepository.findByTrip_Id(trip.getId()).isEmpty()) {
                    logTripCompletion(trip, now);
                }
            }
        }
    }

    private void logTripCompletion(Trip trip, LocalDateTime actualCompletion) {
        List<Ticket> tickets = trip.getTickets();
        int totalSeats = trip.getBus().getCapacity();

        // Count sold seats (CONFIRMED status)
        long soldSeats = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.CONFIRMED || t.getStatus() == TicketStatus.NO_SHOW)
                .count();

        // Count cancelled seats
        long cancelledSeats = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.CANCELLED)
                .count();

        // Calculate revenue from confirmed tickets
        double totalRevenue = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.CONFIRMED)
                .mapToDouble(Ticket::getPrice)
                .sum();

        // Calculate occupation rate
        double occupationRate = totalSeats > 0 ? (soldSeats * 100.0 / totalSeats) : 0.0;

        // Calculate punctuality (on time if departed within 15 minutes of schedule)
        LocalDateTime scheduledDeparture = trip.getDepartureAt();
        LocalDateTime actualDeparture = trip.getDepartureAt(); // We don't track actual departure currently
        long delayMinutes = java.time.Duration.between(scheduledDeparture, actualDeparture).toMinutes();
        boolean onTime = Math.abs(delayMinutes) <= 15;

        // Create trip log
        TripLog tripLog = TripLog.builder()
                .trip(trip)
                .scheduledDeparture(trip.getDepartureAt())
                .actualDeparture(actualDeparture)
                .scheduledArrival(trip.getArrivalAt())
                .actualArrival(actualCompletion)
                .finalStatus(trip.getStatus())
                .totalSeats(totalSeats)
                .soldSeats((int) soldSeats)
                .cancelledSeats((int) cancelledSeats)
                .occupationRate(Math.round(occupationRate * 100.0) / 100.0)
                .totalRevenue(Math.round(totalRevenue * 100.0) / 100.0)
                .delayMinutes((int) delayMinutes)
                .onTime(onTime)
                .build();

        tripLogRepository.save(tripLog);
    }
}
