package com.example.services.definitions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.FareRuleDTOs;
import com.example.domain.entities.FareRule;
import com.example.domain.entities.Route;
import com.example.domain.entities.Stop;
import com.example.domain.repositories.FareRuleRepository;
import com.example.domain.repositories.RouteRepository;
import com.example.domain.repositories.StopRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.mappers.FareRuleMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class FareRuleServiceImpl implements FareRuleService {

    private final FareRuleRepository repo;
    private final FareRuleMapper mapper;
    private final StopRepository stopRepo;
    private final RouteRepository routeRepo;

    @Override
    public FareRuleDTOs.FareRuleResponse createFareRule(FareRuleDTOs.CreateFareRuleRequest req) {
        Stop fromStop = stopRepo.findById(req.fromStopId())
                .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.fromStopId())));
        Stop toStop = stopRepo.findById(req.toStopId())
                .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.toStopId())));
        Route route = routeRepo.findById(req.routeId())
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(req.routeId())));
        FareRule fareRule = FareRule.builder()
                .fromStop(fromStop)
                .toStop(toStop)
                .route(route)
                .build();
        return mapper.toResponse(repo.save(fareRule));
    }

    @Override
    @Transactional(readOnly = true)
    public FareRuleDTOs.FareRuleResponse getFareRuleById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("FareRule %d not found".formatted(id)));
    }

    @Override
    public void deleteFareRule(Long id) {
        var fareRule = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("FareRule %d not found".formatted(id)));
        repo.delete(fareRule);
    }

    @Override
    public FareRuleDTOs.FareRuleResponse updateFareRule(Long id, FareRuleDTOs.UpdateFareRuleRequest req) {
        if (req.fromStopId() != null) {
            stopRepo.findById(req.fromStopId())
                    .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.fromStopId())));

        }
        if (req.toStopId() != null) {
            stopRepo.findById(req.toStopId())
                    .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.toStopId())));

        }
        if (req.routeId() != null) {
            routeRepo.findById(req.routeId())
                    .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(req.routeId())));

        }
        var fareRule = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("FareRule %d not found".formatted(id)));
        mapper.patch(fareRule, req);
        return mapper.toResponse(repo.save(fareRule));
    }
}
