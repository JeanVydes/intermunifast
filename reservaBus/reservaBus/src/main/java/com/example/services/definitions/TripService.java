package com.example.services.definitions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.api.dto.AssignmentDTOs;
import com.example.api.dto.IncidentDTOs;
import com.example.api.dto.SeatDTOs;
import com.example.api.dto.TicketDTOs;
import com.example.api.dto.TripDTOs;
import com.example.domain.enums.TicketStatus;

public interface TripService {
    TripDTOs.TripResponse createTrip(TripDTOs.CreateTripRequest req);

    TripDTOs.TripResponse getTripById(Long id);

    TripDTOs.TripResponse updateTrip(Long id, TripDTOs.UpdateTripRequest req);

    void deleteTrip(Long id);

    List<TicketDTOs.TicketResponse> getTicketsByTripIdAndStatus(Long id, TicketStatus status);

    List<SeatDTOs.SeatResponse> getSeatsByTripId(Long id, String status);

    List<AssignmentDTOs.AssignmentResponse> getAssignmentsByTripId(Long id);

    List<IncidentDTOs.IncidentResponse> getIncidentsByTripId(Long id);

    List<TripDTOs.TripResponse> getTripsByRouteId(Long routeId);

    List<TripDTOs.TripResponse> getAllTrips();

    TripDTOs.TripSearchResponse searchTrips(String origin, String destination, Optional<LocalDateTime> departureDate);
}
