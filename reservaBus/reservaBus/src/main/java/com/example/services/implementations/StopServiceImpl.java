package com.example.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.StopDTOs;
import com.example.domain.entities.Route;
import com.example.domain.entities.Stop;
import com.example.domain.repositories.RouteRepository;
import com.example.domain.repositories.StopRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.StopService;
import com.example.services.mappers.StopMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class StopServiceImpl implements StopService {

    private final StopRepository repo;
    private final StopMapper mapper;
    private final RouteRepository routeRepo;

    @Override
    public StopDTOs.StopResponse createStop(StopDTOs.CreateStopRequest req) {
        Route route = routeRepo.findById(req.routeId())
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(req.routeId())));
        Stop stop = Stop.builder().route(route)
                .name(req.name())
                .sequence(req.sequence())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .build();
        return mapper.toResponse(repo.save(stop));
    }

    @Override
    @Transactional(readOnly = true)
    public StopDTOs.StopResponse getStopById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(id)));
    }

    @Override
    public void deleteStop(Long id) {
        var stop = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(id)));
        repo.delete(stop);
    }

    @Override
    public StopDTOs.StopResponse updateStop(Long id, StopDTOs.UpdateStopRequest req) {
        var stop = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(id)));

        // Update only fields that are present
        req.name().ifPresent(stop::setName);
        req.sequence().ifPresent(stop::setSequence);
        req.latitude().ifPresent(stop::setLatitude);
        req.longitude().ifPresent(stop::setLongitude);
        req.routeId().ifPresent(routeId -> {
            Route route = routeRepo.findById(routeId)
                    .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(routeId)));
            stop.setRoute(route);
        });

        return mapper.toResponse(repo.save(stop));
    }

}
