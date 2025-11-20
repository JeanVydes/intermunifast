package com.example.services.watchers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.domain.entities.Parcel;
import com.example.domain.entities.Ticket;
import com.example.domain.entities.Trip;
import com.example.domain.enums.ParcelStatus;
import com.example.domain.enums.TicketStatus;
import com.example.domain.enums.TripStatus;
import com.example.domain.repositories.ParcelRepository;
import com.example.domain.repositories.TripRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripWatcher {
    private final TripRepository tripRepository;
    private final ParcelRepository parcelRepository;

    @Scheduled(fixedRate = 60000)
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
            } else if (trip.getDepartureAt().isBefore(now.plusMinutes(30))) {
                trip.setStatus(TripStatus.BOARDING);
            } else if (trip.getDepartureAt().isBefore(now)) {
                trip.setStatus(TripStatus.DEPARTED);

                List<Parcel> parcels = trip.getParcels();
                for (Parcel parcel : parcels) {
                    if (parcel.getStatus() == ParcelStatus.CREATED) {
                        parcel.setStatus(ParcelStatus.IN_TRANSIT);
                        parcelRepository.save(parcel);
                    }
                }
            }
        }

        List<Trip> departedTrips = tripRepository.findByStatus(TripStatus.DEPARTED);
        for (Trip trip : departedTrips) {
            if (trip.getArrivalAt().isBefore(now)) {
                trip.setStatus(TripStatus.ARRIVED);
                tripRepository.save(trip);
            }
        }
    }
}
