package com.example.api.controllers;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.api.dto.BusDTOs;
import com.example.services.definitions.BusService;

@RestController
@RequestMapping("/api/buses")
public class BusController {
    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<BusDTOs.BusResponse> create(
            @Validated @RequestBody BusDTOs.CreateBusRequest req,
            UriComponentsBuilder uriBuilder) {
        BusDTOs.BusResponse createdBus = busService.createBus(req);
        return ResponseEntity.created(
                uriBuilder.path("/api/buses/{id}").buildAndExpand(createdBus.id()).toUri())
                .body(createdBus);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusDTOs.BusResponse> getById(@PathVariable Long id) {
        BusDTOs.BusResponse bus = busService.getBusById(id);
        return ResponseEntity.ok(bus);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<BusDTOs.BusResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody BusDTOs.UpdateBusRequest req) {
        BusDTOs.BusResponse updatedBus = busService.updateBus(id, req);
        return ResponseEntity.ok(updatedBus);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        busService.deleteBus(id);
        return ResponseEntity.noContent().build();
    }
}
