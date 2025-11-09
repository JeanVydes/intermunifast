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

import com.example.api.dto.StopDTOs;
import com.example.services.definitions.StopService;

@RestController
@RequestMapping("/api/stops")
public class StopController {
    private final StopService stopService;

    public StopController(StopService stopService) {
        this.stopService = stopService;
    }

    @PostMapping
    public ResponseEntity<StopDTOs.StopResponse> create(
            @Validated @RequestBody StopDTOs.CreateStopRequest req,
            UriComponentsBuilder uriBuilder) {
        StopDTOs.StopResponse createdStop = stopService.createStop(req);
        return ResponseEntity.created(
                uriBuilder.path("/api/stops/{id}").buildAndExpand(createdStop.id()).toUri())
                .body(createdStop);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StopDTOs.StopResponse> getById(@PathVariable Long id) {
        StopDTOs.StopResponse stop = stopService.getStopById(id);
        return ResponseEntity.ok(stop);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<StopDTOs.StopResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody StopDTOs.UpdateStopRequest req) {
        StopDTOs.StopResponse updatedStop = stopService.updateStop(id, req);
        return ResponseEntity.ok(updatedStop);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stopService.deleteStop(id);
        return ResponseEntity.noContent().build();
    }
}
