package com.example.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.api.dto.SeatDTOs;
import com.example.services.definitions.SeatService;

@RestController
@RequestMapping("/api/seats")
public class SeatController {
    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping
    public ResponseEntity<SeatDTOs.SeatResponse> create(
            @Validated @RequestBody SeatDTOs.CreateSeatRequest req,
            UriComponentsBuilder uriBuilder) {
        SeatDTOs.SeatResponse createdSeat = seatService.createSeat(req);
        return ResponseEntity.created(
                uriBuilder.path("/api/seats/{id}").buildAndExpand(createdSeat.id()).toUri())
                .body(createdSeat);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatDTOs.SeatResponse> getById(@PathVariable Long id) {
        SeatDTOs.SeatResponse seat = seatService.getSeatById(id);
        return ResponseEntity.ok(seat);
    }

    @GetMapping("/bus/{busId}")
    public ResponseEntity<Iterable<SeatDTOs.SeatResponse>> getByBusId(@PathVariable Long busId) {
        Iterable<SeatDTOs.SeatResponse> seats = seatService.getSeatsByBusId(busId);
        return ResponseEntity.ok(seats);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SeatDTOs.SeatResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody SeatDTOs.UpdateSeatRequest req) {
        SeatDTOs.SeatResponse updatedSeat = seatService.updateSeat(id, req);
        return ResponseEntity.ok(updatedSeat);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }
}
