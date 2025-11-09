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

import com.example.api.dto.BaggageDTOs;
import com.example.services.definitions.BaggageService;

@RestController
@RequestMapping("/api/baggages")
public class BaggageController {
    private final BaggageService baggageService;

    public BaggageController(BaggageService baggageService) {
        this.baggageService = baggageService;
    }

    @PostMapping
    public ResponseEntity<BaggageDTOs.BaggageResponse> create(
            @Validated @RequestBody BaggageDTOs.CreateBaggageRequest req,
            UriComponentsBuilder uriBuilder) {
        BaggageDTOs.BaggageResponse createdBaggage = baggageService.createBaggage(req);
        return ResponseEntity.created(
                uriBuilder.path("/api/baggages/{id}").buildAndExpand(createdBaggage.id()).toUri())
                .body(createdBaggage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaggageDTOs.BaggageResponse> getById(@PathVariable Long id) {
        BaggageDTOs.BaggageResponse baggage = baggageService.getBaggageById(id);
        return ResponseEntity.ok(baggage);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BaggageDTOs.BaggageResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody BaggageDTOs.UpdateBaggageRequest req) {
        BaggageDTOs.BaggageResponse updatedBaggage = baggageService.updateBaggage(id, req);
        return ResponseEntity.ok(updatedBaggage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        baggageService.deleteBaggage(id);
        return ResponseEntity.noContent().build();
    }
}
