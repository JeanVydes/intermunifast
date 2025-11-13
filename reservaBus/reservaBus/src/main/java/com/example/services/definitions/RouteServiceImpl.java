package com.example.services.definitions;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.RouteDTOs;
import com.example.api.dto.StopDTOs;
import com.example.domain.entities.Route;
import com.example.domain.repositories.RouteRepository;
import com.example.domain.repositories.StopRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.mappers.RouteMapper;
import com.example.services.mappers.StopMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class RouteServiceImpl implements RouteService {

    private final RouteRepository repo;
    private final StopRepository stopRepo;
    private final RouteMapper mapper;
    private final StopMapper stopMapper;

    @Override
    public RouteDTOs.RouteResponse createRoute(RouteDTOs.CreateRouteRequest req) {
        var route = mapper.toEntity(req);
        return mapper.toResponse(repo.save(route));
    }

    @Override
    @Transactional(readOnly = true)
    public RouteDTOs.RouteResponse getRouteById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(id)));
    }

    @Override
    public void deleteRoute(Long id) {
        var route = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(id)));
        repo.delete(route);
    }

    @Override
    public RouteDTOs.RouteResponse updateRoute(Long id, RouteDTOs.UpdateRouteRequest req) {
        var route = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(id)));

        // Update only fields that are present
        req.code().ifPresent(route::setCode);
        req.name().ifPresent(route::setName);
        req.origin().ifPresent(route::setOrigin);
        req.destination().ifPresent(route::setDestination);
        req.durationMinutes().ifPresent(route::setDurationMinutes);
        req.distanceKm().ifPresent(route::setDistanceKm);
        req.pricePerKm().ifPresent(route::setPricePerKm);

        return mapper.toResponse(repo.save(route));
    }

    @Override
    public List<StopDTOs.StopResponse> getStopsByRouteId(Long id) {
        Route route = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(id)));
        return stopRepo.findByRoute_IdOrderBySequenceAsc(route.getId()).stream()
                .map(stopMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteDTOs.RouteResponse> findByOrigin(String origin) {
        var routes = repo.findByOrigin(origin);
        return routes.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteDTOs.RouteResponse> findByDestination(String destination) {
        var routes = repo.findByDestination(destination);
        return routes.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteDTOs.RouteResponse> findByOriginAndDestination(String origin, String destination) {
        var routes = repo.findByOriginAndDestination(origin, destination);
        return routes.stream()
                .map(mapper::toResponse)
                .toList();
    }
}
