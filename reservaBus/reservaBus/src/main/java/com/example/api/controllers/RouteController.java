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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.api.dto.RouteDTOs;
import com.example.api.dto.StopDTOs;
import com.example.services.definitions.RouteService;

@RestController
@RequestMapping("/api/routes")
public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PreAuthorize("hasAnyAuthority('CLERK', 'ADMIN')")
    @PostMapping
    public ResponseEntity<RouteDTOs.RouteResponse> create(
            @Validated @RequestBody RouteDTOs.CreateRouteRequest req,
            UriComponentsBuilder uriBuilder) {
        RouteDTOs.RouteResponse createdRoute = routeService.createRoute(req);
        return ResponseEntity.created(
                uriBuilder.path("/api/routes/{id}").buildAndExpand(createdRoute.id()).toUri())
                .body(createdRoute);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteDTOs.RouteResponse> getById(@PathVariable Long id) {
        RouteDTOs.RouteResponse route = routeService.getRouteById(id);
        return ResponseEntity.ok(route);
    }

    @PreAuthorize("hasAnyAuthority('CLERK', 'ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<RouteDTOs.RouteResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody RouteDTOs.UpdateRouteRequest req) {
        RouteDTOs.RouteResponse updatedRoute = routeService.updateRoute(id, req);
        return ResponseEntity.ok(updatedRoute);
    }

    @PreAuthorize("hasAnyAuthority('CLERK', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stops")
    public ResponseEntity<List<StopDTOs.StopResponse>> getStopsByRouteId(@PathVariable Long id) {
        List<StopDTOs.StopResponse> stops = routeService.getStopsByRouteId(id);
        return ResponseEntity.ok(stops);
    }
}
