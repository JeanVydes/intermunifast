package com.example.services.watchers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.domain.entities.Ticket;
import com.example.domain.entities.Trip;
import com.example.domain.enums.TicketStatus;
import com.example.domain.enums.TripStatus;
import com.example.domain.repositories.TripRepository;

import jakarta.transaction.Transactional;

@Service
public class TripWatcher {
    @Autowired
    private TripRepository tripRepository;

    @Scheduled(fixedRate = 60000) // every minute
    @Transactional
    public void tripMainWorker() {
        List<Trip> startingTrips = tripRepository.findStartingTripsInNextMinutes(60);

        for (Trip trip : startingTrips) {
            if (trip.getDepartureAt().isBefore(java.time.LocalDateTime.now().plusMinutes(5))) {
                List<Ticket> tickets = trip.getTickets();

                for (Ticket ticket : tickets) {
                    ticket.setStatus(TicketStatus.NO_SHOW);
                }
                // Starting in 30 minutes
            } else if (trip.getDepartureAt().isBefore(java.time.LocalDateTime.now().plusMinutes(30))) {
                trip.setStatus(TripStatus.BOARDING);
            } else if (trip.getDepartureAt().isBefore(java.time.LocalDateTime.now())) {
                trip.setStatus(TripStatus.DEPARTED);
            }
        }
    }
}
