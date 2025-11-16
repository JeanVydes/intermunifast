package com.example.api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.api.dto.AssignmentDTOs;
import com.example.api.dto.IncidentDTOs;
import com.example.api.dto.SeatDTOs;
import com.example.api.dto.TicketDTOs;
import com.example.api.dto.TripDTOs;
import com.example.domain.enums.TicketStatus;
import com.example.services.definitions.TripService;

@RestController
@RequestMapping("/api/trips")
public class TripController {
    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PreAuthorize("hasAnyAuthority('CLERK', 'ADMIN')")
    @PostMapping
    public ResponseEntity<TripDTOs.TripResponse> create(
            @Validated @RequestBody TripDTOs.CreateTripRequest req,
            UriComponentsBuilder uriBuilder) {
        TripDTOs.TripResponse createdTrip = tripService.createTrip(req);
        return ResponseEntity.created(
                uriBuilder.path("/api/trips/{id}").buildAndExpand(createdTrip.id()).toUri())
                .body(createdTrip);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripDTOs.TripResponse> getById(@PathVariable Long id,
            @RequestParam(required = false) String routeId, @RequestParam(required = false) String status) {

        TripDTOs.TripResponse trip = tripService.getTripById(id);
        return ResponseEntity.ok(trip);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TripDTOs.TripResponse>> getAll() {
        List<TripDTOs.TripResponse> trips = tripService.getAllTrips();
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/byRoute/{routeId}")
    public ResponseEntity<List<TripDTOs.TripResponse>> getByRouteId(@PathVariable Long routeId,
            @RequestParam(required = false) String status) {
        List<TripDTOs.TripResponse> trips = tripService.getTripsByRouteId(routeId);
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/search")
    public ResponseEntity<TripDTOs.TripSearchResponse> search(
            @RequestParam String origin,
            @RequestParam String destination,
            // optional
            @RequestParam(required = false) String departureDate) {

        // Validate required parameters
        if (origin == null || origin.isBlank()) {
            throw new IllegalArgumentException("Origin parameter is required");
        }
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Destination parameter is required");
        }

        // Parse departureDate only if provided and not empty
        java.util.Optional<java.time.LocalDateTime> departureTime = java.util.Optional.empty();
        if (departureDate != null && !departureDate.isBlank()) {
            try {
                departureTime = java.util.Optional.of(java.time.LocalDateTime.parse(departureDate));
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Invalid departureDate format. Expected ISO-8601 datetime (e.g., 2025-11-14T10:00:00)");
            }
        }

        TripDTOs.TripSearchResponse searchResponse = tripService.searchTrips(origin, destination, departureTime);
        return ResponseEntity.ok(searchResponse);
    }

    @PreAuthorize("hasAnyAuthority('CLERK', 'ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<TripDTOs.TripResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody TripDTOs.UpdateTripRequest req) {
        TripDTOs.TripResponse updatedTrip = tripService.updateTrip(id, req);
        return ResponseEntity.ok(updatedTrip);
    }

    @PreAuthorize("hasAnyAuthority('CLERK', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tickets")
    public ResponseEntity<List<TicketDTOs.TicketResponse>> getTicketsByTripId(@PathVariable Long id,
            @RequestParam(required = false) TicketStatus status) {
        List<TicketDTOs.TicketResponse> tickets = tripService.getTicketsByTripIdAndStatus(id, status);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatDTOs.SeatResponse>> getSeatsByTripId(
            @PathVariable Long id,
            @RequestParam(required = false) String status) {
        List<SeatDTOs.SeatResponse> seats = tripService.getSeatsByTripId(id, status);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/{id}/assignments")
    public ResponseEntity<List<AssignmentDTOs.AssignmentResponse>> getAssignmentsByTripId(@PathVariable Long id) {
        List<AssignmentDTOs.AssignmentResponse> assignments = tripService.getAssignmentsByTripId(id);
        return ResponseEntity.ok(assignments);
    }

    @PreAuthorize("hasAnyAuthority('CLERK', 'ADMIN')")
    @GetMapping("/{id}/incidents")
    public ResponseEntity<List<IncidentDTOs.IncidentResponse>> getIncidentsByTripId(@PathVariable Long id) {
        List<IncidentDTOs.IncidentResponse> incidents = tripService.getIncidentsByTripId(id);
        return ResponseEntity.ok(incidents);
    }
}
