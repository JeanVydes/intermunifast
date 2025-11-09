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

import com.example.api.dto.SeatHoldDTOs;
import com.example.services.definitions.SeatHoldService;

@RestController
@RequestMapping("/api/seat-holds")
public class SeatHoldController {
    private final SeatHoldService seatHoldService;

    public SeatHoldController(SeatHoldService seatHoldService) {
        this.seatHoldService = seatHoldService;
    }

    @PostMapping
    public ResponseEntity<SeatHoldDTOs.SeatHoldResponse> create(
            @Validated @RequestBody SeatHoldDTOs.CreateSeatHoldRequest req,
            UriComponentsBuilder uriBuilder) {
        SeatHoldDTOs.SeatHoldResponse createdSeatHold = seatHoldService.createSeatHold(req);
        return ResponseEntity.created(
                uriBuilder.path("/api/seat-holds/{id}").buildAndExpand(createdSeatHold.id()).toUri())
                .body(createdSeatHold);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatHoldDTOs.SeatHoldResponse> getById(@PathVariable Long id) {
        SeatHoldDTOs.SeatHoldResponse seatHold = seatHoldService.getSeatHoldById(id);
        return ResponseEntity.ok(seatHold);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SeatHoldDTOs.SeatHoldResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody SeatHoldDTOs.UpdateSeatHoldRequest req) {
        return ResponseEntity.ok(seatHoldService.updateSeatHold(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        seatHoldService.deleteSeatHold(id);
        return ResponseEntity.noContent().build();
    }
}
