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

import com.example.api.dto.IncidentDTOs;
import com.example.services.definitions.IncidentService;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    public ResponseEntity<IncidentDTOs.IncidentResponse> create(
            @Validated @RequestBody IncidentDTOs.CreateIncidentRequest req,
            UriComponentsBuilder uriBuilder) {
        IncidentDTOs.IncidentResponse createdIncident = incidentService.createIncident(req);
        return ResponseEntity.created(
                uriBuilder.path("/api/incidents/{id}").buildAndExpand(createdIncident.id()).toUri())
                .body(createdIncident);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentDTOs.IncidentResponse> getById(@PathVariable Long id) {
        IncidentDTOs.IncidentResponse incident = incidentService.getIncidentById(id);
        return ResponseEntity.ok(incident);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IncidentDTOs.IncidentResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody IncidentDTOs.UpdateIncidentRequest req) {
        IncidentDTOs.IncidentResponse updatedIncident = incidentService.updateIncident(id, req);
        return ResponseEntity.ok(updatedIncident);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }
}
