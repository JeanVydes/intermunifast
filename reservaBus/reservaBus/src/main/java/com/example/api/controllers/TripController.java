package com.example.api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
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
import com.example.services.definitions.TripService;

@RestController
@RequestMapping("/api/trips")
public class TripController {
    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

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
        if (routeId != null || status != null) {
            TripDTOs.TripResponse trip = tripService.getTripByIdAndFilters(id, routeId, status);
            return ResponseEntity.ok(trip);
        }

        TripDTOs.TripResponse trip = tripService.getTripById(id);
        return ResponseEntity.ok(trip);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TripDTOs.TripResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody TripDTOs.UpdateTripRequest req) {
        TripDTOs.TripResponse updatedTrip = tripService.updateTrip(id, req);
        return ResponseEntity.ok(updatedTrip);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tickets")
    public ResponseEntity<List<TicketDTOs.TicketResponse>> getTicketsByTripId(@PathVariable Long id,
            @RequestParam(required = false) String status) {
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

    @GetMapping("/{id}/incidents")
    public ResponseEntity<List<IncidentDTOs.IncidentResponse>> getIncidentsByTripId(@PathVariable Long id) {
        List<IncidentDTOs.IncidentResponse> incidents = tripService.getIncidentsByTripId(id);
        return ResponseEntity.ok(incidents);
    }
}
