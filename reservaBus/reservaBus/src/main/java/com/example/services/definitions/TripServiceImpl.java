package com.example.services.definitions;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.AssignmentDTOs;
import com.example.api.dto.IncidentDTOs;
import com.example.api.dto.SeatDTOs;
import com.example.api.dto.TicketDTOs;
import com.example.api.dto.TripDTOs;
import com.example.domain.entities.Bus;
import com.example.domain.entities.Route;
import com.example.domain.entities.Trip;
import com.example.domain.enums.TicketStatus;
import com.example.domain.enums.TripStatus;
import com.example.domain.repositories.AssignmentRepository;
import com.example.domain.repositories.BusRepository;
import com.example.domain.repositories.IncidentRepository;
import com.example.domain.repositories.RouteRepository;
import com.example.domain.repositories.SeatRepository;
import com.example.domain.repositories.TicketRepository;
import com.example.domain.repositories.TripRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.mappers.AssignmentMapper;
import com.example.services.mappers.IncidentMapper;
import com.example.services.mappers.SeatMapper;
import com.example.services.mappers.TicketMapper;
import com.example.services.mappers.TripMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class TripServiceImpl implements TripService {

    private final TripRepository repo;
    private final TripMapper mapper;
    private final TicketMapper ticketMapper;
    private final TicketRepository ticketRepo;
    private final SeatMapper seatMapper;
    private final SeatRepository seatRepo;
    private final AssignmentMapper assignmentMapper;
    private final AssignmentRepository assignmentRepo;
    private final IncidentMapper incidentMapper;
    private final IncidentRepository incidentRepo;
    private final RouteRepository routeRepo;
    private final BusRepository busRepo;

    @Override
    public TripDTOs.TripResponse createTrip(TripDTOs.CreateTripRequest req) {
        Route route = routeRepo.findById(req.routeId())
                .orElseThrow(() -> new NotFoundException(
                        "Route %d not found".formatted(req.routeId())));
        Bus bus = busRepo.findById(req.busId())
                .orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(req.busId())));
        Trip trip = Trip.builder()
                .route(route)
                .bus(bus)
                .departureAt(req.departureAt())
                .arrivalAt(req.arrivalAt())
                .status(TripStatus.SCHEDULED)
                .build();
        return mapper.toResponse(repo.save(trip));
    }

    @Override
    @Transactional(readOnly = true)
    public TripDTOs.TripResponse getTripById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(id)));
    }

    @Override
    public void deleteTrip(Long id) {
        var trip = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(id)));
        repo.delete(trip);
    }

    @Override
    public TripDTOs.TripResponse updateTrip(Long id, TripDTOs.UpdateTripRequest req) {
        var trip = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(id)));

        if (req.routeId() != null) {
            Route route = routeRepo.findById(req.routeId())
                    .orElseThrow(() -> new NotFoundException(
                            "Route %d not found".formatted(req.routeId())));
            trip.setRoute(route);
        }
        if (req.busId() != null) {
            Bus bus = busRepo.findById(req.busId())
                    .orElseThrow(() -> new NotFoundException(
                            "Bus %d not found".formatted(req.busId())));
            trip.setBus(bus);
        }
        if (req.departureAt() != null) {
            trip.setDepartureAt(req.departureAt());
        }
        if (req.arrivalAt() != null) {
            trip.setArrivalAt(req.arrivalAt());
        }

        return mapper.toResponse(repo.save(trip));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTOs.TicketResponse> getTicketsByTripIdAndStatus(Long id, TicketStatus status) {
        var trip = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(id)));
        return ticketRepo.findByTrip_IdAndStatus(trip.getId(), status).stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatDTOs.SeatResponse> getSeatsByTripId(Long id, String status) {
        var trip = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(id)));
        return seatRepo.findByBus_Id(trip.getBus().getId()).stream()
                .map(seatMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentDTOs.AssignmentResponse> getAssignmentsByTripId(Long id) {
        var trip = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(id)));
        return assignmentRepo.findByTrip_Id(trip.getId()).stream()
                .map(assignmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentDTOs.IncidentResponse> getIncidentsByTripId(Long id) {
        var trip = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(id)));
        return incidentRepo.findByTripId(trip.getId()).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDTOs.TripResponse> getTripsByRouteId(Long routeId) {
        var route = routeRepo.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(routeId)));
        return repo.findByRoute_Id(route.getId()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDTOs.TripResponse> getAllTrips() {
        var trips = repo.findAll();
        return trips.stream()
                .map(mapper::toResponse)
                .toList();
    }
}
