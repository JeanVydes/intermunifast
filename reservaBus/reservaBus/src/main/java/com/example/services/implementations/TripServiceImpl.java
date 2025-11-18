package com.example.services.implementations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.AssignmentDTOs;
import com.example.api.dto.IncidentDTOs;
import com.example.api.dto.RouteDTOs;
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
import com.example.domain.repositories.StopRepository;
import com.example.domain.repositories.TicketRepository;
import com.example.domain.repositories.TripRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.TripService;
import com.example.services.mappers.AssignmentMapper;
import com.example.services.mappers.IncidentMapper;
import com.example.services.mappers.RouteMapper;
import com.example.services.mappers.SeatMapper;
import com.example.services.mappers.StopMapper;
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
        private final RouteMapper routeMapper;
        private final StopMapper stopMapper;
        private final SeatMapper seatMapper;
        private final SeatRepository seatRepo;
        private final AssignmentMapper assignmentMapper;
        private final AssignmentRepository assignmentRepo;
        private final IncidentMapper incidentMapper;
        private final IncidentRepository incidentRepo;
        private final RouteRepository routeRepo;
        private final BusRepository busRepo;
        private final StopRepository stopRepo;

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

        @Override
        @Transactional(readOnly = true)
        public TripDTOs.TripSearchResponse searchTrips(String origin, String destination,
                        Optional<LocalDateTime> departureDate) {
                LocalDateTime startOfDay;
                LocalDateTime endOfDay;

                if (departureDate.isPresent()) {
                        // Si se proporciona una fecha específica, buscar desde esa fecha hasta 3 meses
                        // después
                        startOfDay = departureDate.get().toLocalDate().atStartOfDay();
                        endOfDay = startOfDay.plusMonths(3);
                } else {
                        // Si NO se proporciona fecha, buscar desde ahora hasta 3 meses en el futuro
                        startOfDay = LocalDateTime.now();
                        endOfDay = LocalDateTime.now().plusMonths(3);
                }

                var trips = repo.searchAvailableTrips(origin, destination, startOfDay, endOfDay);

                var tripResponses = trips.stream()
                                .map(mapper::toResponse)
                                .toList();

                // Get unique routes from the trips found
                var routes = trips.stream()
                                .map(Trip::getRoute)
                                .distinct()
                                .map(routeMapper::toResponse)
                                .toList();

                // Get all stops for these routes, ordered by sequence
                var stops = trips.stream()
                                .map(Trip::getRoute)
                                .distinct()
                                .flatMap(route -> stopRepo.findByRoute_IdOrderBySequenceAsc(route.getId()).stream())
                                .distinct()
                                .map(stopMapper::toResponse)
                                .toList();

                return new TripDTOs.TripSearchResponse(tripResponses, routes, stops);
        }
}
